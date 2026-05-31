Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-Checked {
    param(
        [Parameter(Mandatory=$true)][string]$FilePath,
        [string[]]$Arguments
    )
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$Sdk = $env:ANDROID_HOME
if (-not $Sdk) { $Sdk = "$env:LOCALAPPDATA\Android\Sdk" }
if (-not (Test-Path $Sdk)) { throw "Android SDK was not found. Install Android Studio SDK, then set ANDROID_HOME." }

$Platform = Join-Path $Sdk "platforms\android-33\android.jar"
if (-not (Test-Path $Platform)) { throw "android-33 platform was not found under $Sdk\platforms." }

$BuildToolsRoot = Join-Path $Sdk "build-tools"
$BuildTools = Get-ChildItem $BuildToolsRoot -Directory | Sort-Object Name -Descending | Select-Object -First 1
if (-not $BuildTools) { throw "Android build-tools were not found under $BuildToolsRoot." }

$Aapt2 = Join-Path $BuildTools.FullName "aapt2.exe"
$D8 = Join-Path $BuildTools.FullName "d8.bat"
$Zipalign = Join-Path $BuildTools.FullName "zipalign.exe"
$Apksigner = Join-Path $BuildTools.FullName "apksigner.bat"
foreach ($tool in @($Aapt2, $D8, $Zipalign, $Apksigner)) {
    if (-not (Test-Path $tool)) { throw "Required Android tool not found: $tool" }
}

$Javac = $null
if ($env:JAVA_HOME) {
    $Candidate = Join-Path $env:JAVA_HOME "bin\javac.exe"
    if (Test-Path $Candidate) { $Javac = $Candidate }
}
if (-not $Javac) {
    $StudioJavac = Get-ChildItem "C:\Program Files\Android" -Recurse -Filter javac.exe -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($StudioJavac) { $Javac = $StudioJavac.FullName }
}
if (-not $Javac) { throw "javac was not found. Install JDK or open Android Studio once to install its bundled JDK." }
$JdkBin = Split-Path -Parent $Javac
$env:JAVA_HOME = Split-Path -Parent $JdkBin
$env:Path = "$JdkBin;$env:Path"

$Build = Join-Path $ProjectRoot "build"
$Compiled = Join-Path $Build "compiled"
$Gen = Join-Path $Build "gen"
$Classes = Join-Path $Build "classes"
$Dex = Join-Path $Build "dex"
$Dist = Join-Path $ProjectRoot "dist"
Remove-Item $Build, $Dist -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $Compiled, $Gen, $Classes, $Dex, $Dist | Out-Null

Invoke-Checked $Aapt2 @("compile", "--dir", (Join-Path $ProjectRoot "res"), "-o", (Join-Path $Compiled "resources.zip"))
Invoke-Checked $Aapt2 @(
    "link",
    "-I", $Platform,
    "--manifest", (Join-Path $ProjectRoot "AndroidManifest.xml"),
    "--java", $Gen,
    "--auto-add-overlay",
    "-o", (Join-Path $Build "unsigned.apk"),
    (Join-Path $Compiled "resources.zip")
)

$Sources = Get-ChildItem (Join-Path $ProjectRoot "src") -Recurse -Filter *.java
$Generated = Get-ChildItem $Gen -Recurse -Filter *.java
$JavaFiles = @($Sources | ForEach-Object { $_.FullName }) + @($Generated | ForEach-Object { $_.FullName })
$JavacArgs = @("-source", "1.8", "-target", "1.8", "-encoding", "UTF-8", "-classpath", $Platform, "-d", $Classes) + $JavaFiles
Invoke-Checked $Javac $JavacArgs

$ClassFiles = Get-ChildItem $Classes -Recurse -Filter *.class | ForEach-Object { $_.FullName }
$D8Args = @("--min-api", "23", "--lib", $Platform, "--output", $Dex) + $ClassFiles
Invoke-Checked $D8 $D8Args

Add-Type -AssemblyName System.IO.Compression
Add-Type -AssemblyName System.IO.Compression.FileSystem
$Unsigned = Join-Path $Build "unsigned.apk"
$WithDex = Join-Path $Build "with-dex.apk"
Copy-Item $Unsigned $WithDex
$Zip = [System.IO.Compression.ZipFile]::Open($WithDex, [System.IO.Compression.ZipArchiveMode]::Update)
try {
    [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($Zip, (Join-Path $Dex "classes.dex"), "classes.dex") | Out-Null
} finally {
    $Zip.Dispose()
}

$Aligned = Join-Path $Build "aligned.apk"
Invoke-Checked $Zipalign @("-f", "4", $WithDex, $Aligned)

$Keystore = Join-Path $ProjectRoot "debug.keystore"
if (-not (Test-Path $Keystore)) {
    Invoke-Checked "keytool" @("-genkeypair", "-keystore", $Keystore, "-storepass", "android", "-keypass", "android", "-alias", "androiddebugkey", "-keyalg", "RSA", "-keysize", "2048", "-validity", "10000", "-dname", "CN=Android Debug,O=Android,C=US")
}

$Apk = Join-Path $Dist "subscription-review-debug.apk"
Invoke-Checked $Apksigner @("sign", "--ks", $Keystore, "--ks-pass", "pass:android", "--key-pass", "pass:android", "--out", $Apk, $Aligned)
Invoke-Checked $Apksigner @("verify", $Apk)
Write-Host "Built APK: $Apk"

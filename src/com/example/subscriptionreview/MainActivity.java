package com.example.subscriptionreview;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int INK = 0xff252127;
    private static final int PAPER = 0xfff7f3ea;
    private static final int SOFT = 0xffefe7da;
    private static final int LINE = 0xffd8cfc0;
    private static final int MUTED = 0xff70685d;
    private static final int RUST = 0xff9a7158;
    private static final int SAGE = 0xff607a73;

    private final List<Subscription> subscriptions = new ArrayList<>();
    private final List<Service> services = new ArrayList<>();
    private final NumberFormat yen = NumberFormat.getCurrencyInstance(Locale.JAPAN);
    private LinearLayout root;
    private Screen screen = Screen.HOME;
    private Service selectedService;
    private Plan selectedPlan;
    private Subscription selectedSubscription;

    enum Screen { HOME, ADD_SERVICE, PLAN, DETAIL, REVIEW, SETTINGS }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        seedData();
        show(Screen.HOME);
    }

    private void seedData() {
        services.add(new Service("Netflix", "N", 0xff171417, 0xffe50914, R.drawable.ic_netflix, "動画・エンタメ", new Plan[] {
                new Plan("広告つきスタンダード", 890, "月額"),
                new Plan("スタンダード", 1590, "月額"),
                new Plan("プレミアム 年額", 22900, "年額")
        }));
        services.add(new Service("Spotify", "S", 0xff171417, 0xff1db954, R.drawable.ic_spotify, "音楽", new Plan[] {
                new Plan("Individual", 980, "月額"),
                new Plan("Duo", 1280, "月額")
        }));
        services.add(new Service("Disney+", "D+", 0xff113ccf, Color.WHITE, 0, "動画・エンタメ", new Plan[] {
                new Plan("Standard", 990, "月額"),
                new Plan("Premium", 1320, "月額")
        }));
        services.add(new Service("YouTube", "YT", 0xffff0033, Color.WHITE, R.drawable.ic_youtube_play, "動画・エンタメ", new Plan[] {
                new Plan("Premium", 1280, "月額")
        }));
        services.add(new Service("Adobe", "AD", 0xfffa0f00, Color.WHITE, R.drawable.ic_adobe, "クリエイティブ", new Plan[] {
                new Plan("Creative Cloud", 3280, "月額")
        }));
        services.add(new Service("iCloud", "iC", 0xff2775d1, Color.WHITE, R.drawable.ic_icloud, "クラウド", new Plan[] {
                new Plan("2TB", 1500, "月額")
        }));
        services.add(new Service("Canva", "CA", 0xff00c4cc, Color.WHITE, R.drawable.ic_canva, "デザイン", new Plan[] {
                new Plan("Pro", 1180, "月額")
        }));

        addSubscription(services.get(4), services.get(4).plans[0], 3280);
        addSubscription(services.get(0), services.get(0).plans[1], 1590);
        addSubscription(services.get(5), services.get(5).plans[0], 1500);
        addSubscription(services.get(3), services.get(3).plans[0], 1280);
    }

    private void show(Screen next) {
        screen = next;
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAPER);
        setContentView(root);
        if (next == Screen.HOME) renderHome();
        if (next == Screen.ADD_SERVICE) renderServiceSelect();
        if (next == Screen.PLAN) renderPlanSelect();
        if (next == Screen.DETAIL) renderDetail();
        if (next == Screen.REVIEW) renderReview();
        if (next == Screen.SETTINGS) renderSettings();
    }

    private void renderHome() {
        LinearLayout body = page("Cost Review", "May recurring spend", "+", v -> show(Screen.ADD_SERVICE));
        body.addView(hero(yen.format(monthlyTotal()).replace("￥", "") , "今月のサブスク支出"));
        body.addView(insight("見直し候補 2件", "動画カテゴリの重複を整理すると、年間 23,760 円の削減余地があります。"));
        body.addView(label("Largest commitments"));
        for (Subscription s : subscriptions) body.addView(subscriptionRow(s));
        root.addView(nav());
    }

    private void renderServiceSelect() {
        if (selectedService == null) selectedService = services.get(0);
        LinearLayout body = page("サブスクを追加", "サービスを選択", "x", v -> show(Screen.HOME));
        body.addView(searchBox("Netflix, Spotify, Adobe..."));
        body.addView(label("よく使われるサービス"));
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setPadding(0, dp(2), 0, dp(8));
        for (Service service : services) grid.addView(serviceTile(service));
        grid.addView(otherServiceTile());
        body.addView(grid);
        root.addView(primary(selectedService.name + " のプランへ", v -> {
            selectedPlan = selectedService.plans[0];
            show(Screen.PLAN);
        }));
    }

    private void renderPlanSelect() {
        if (selectedService == null) selectedService = services.get(0);
        LinearLayout body = page(selectedService.name, "価格確認日: 2026/05/31", "<", v -> show(Screen.ADD_SERVICE));
        body.addView(label("プラン"));
        for (Plan plan : selectedService.plans) body.addView(planRow(plan));
        if (selectedPlan == null) selectedPlan = selectedService.plans[0];
        body.addView(summaryCard("選択中", selectedPlan.name, "金額編集", yen.format(selectedPlan.price), "月あたり", yen.format(monthlyPrice(selectedPlan))));
        root.addView(primary("このプランを設定する", v -> {
            selectedSubscription = addSubscription(selectedService, selectedPlan, selectedPlan.price);
            show(Screen.HOME);
        }));
    }

    private void renderDetail() {
        if (selectedSubscription == null && !subscriptions.isEmpty()) selectedSubscription = subscriptions.get(0);
        Subscription s = selectedSubscription;
        LinearLayout body = page(s.service.name, s.service.category, "<", v -> show(Screen.HOME));
        body.addView(hero(String.valueOf(s.effectivePrice), s.plan.period + " / 月あたり " + yen.format(monthlyPrice(s.plan))));
        body.addView(insight("カタログ価格が変更された可能性", "現在の登録価格を継続するか、カタログ価格を適用できます。"));
        body.addView(setting("価格情報源", s.overridden ? "ユーザー修正あり" : "カタログ価格", "詳細"));
        body.addView(setting("支払方法", "Visa ending 2431", "編集"));
        body.addView(summaryCard("カタログ価格", yen.format(s.plan.price), "登録価格", yen.format(s.effectivePrice), "月あたり", yen.format(monthlyPrice(s.effectivePrice, s.plan.period))));
        root.addView(primary("金額を編集", v -> editPrice(s)));
    }

    private void renderReview() {
        LinearLayout body = page("Review", "Savings candidates", "<", v -> show(Screen.HOME));
        body.addView(chart());
        body.addView(insight("削減見込み", "動画カテゴリに重複があります。1件停止で年間 23,760 円を削減できます。"));
        body.addView(label("見直し候補"));
        for (Subscription s : subscriptions) {
            if ("動画・エンタメ".equals(s.service.category)) body.addView(subscriptionRow(s));
        }
        root.addView(nav());
    }

    private void renderSettings() {
        LinearLayout body = page("Settings", "App preferences", "<", v -> show(Screen.HOME));
        body.addView(setting("通貨", "日本円", "JPY"));
        body.addView(setting("通知初期値", "請求日の3日前", "編集"));
        body.addView(setting("カタログ更新日", "2026/05/31", "確認"));
        body.addView(setting("バックアップ", "端末内エクスポート", "実行"));
        root.addView(nav());
    }

    private LinearLayout page(String title, String subtitle, String action, View.OnClickListener listener) {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(14), dp(18), dp(8));
        header.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.addView(text(title, 13, INK, true));
        titles.addView(text(subtitle, 10, MUTED, false));
        header.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView button = actionButton(action);
        button.setOnClickListener(listener);
        header.addView(button);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(18), dp(6), dp(18), dp(12));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        return body;
    }

    private View subscriptionRow(Subscription s) {
        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(40), dp(40));
        iconLp.setMargins(0, 0, dp(12), 0);
        row.addView(serviceIcon(s.service, 40), iconLp);
        TextView name = text(s.service.name + "\n" + s.service.category + " / 次回請求 6月2日", 12, INK, true);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text(yen.format(s.effectivePrice), 12, INK, true));
        row.setOnClickListener(v -> {
            selectedSubscription = s;
            show(Screen.DETAIL);
        });
        return row;
    }

    private View serviceTile(Service service) {
        boolean active = selectedService == service;
        LinearLayout tile = new LinearLayout(this);
        tile.setGravity(Gravity.CENTER);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setPadding(dp(4), dp(8), dp(4), dp(7));
        tile.setMinimumHeight(dp(72));
        tile.setBackground(rounded(active ? INK : SOFT, 8, active ? INK : LINE));
        View badge = serviceIcon(service, 30);
        TextView name = text(service.name, 13, INK, true);
        name.setTextSize(9);
        name.setGravity(Gravity.CENTER);
        name.setSingleLine(true);
        name.setTextColor(active ? PAPER : 0xff5d574e);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(dp(30), dp(30));
        badgeLp.setMargins(0, 0, 0, dp(5));
        badge.setLayoutParams(badgeLp);
        tile.addView(badge);
        tile.addView(name, new LinearLayout.LayoutParams(-1, -2));
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = dp(72);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(dp(4), dp(4), dp(4), dp(4));
        tile.setLayoutParams(lp);
        tile.setOnClickListener(v -> {
            selectedService = service;
            show(Screen.ADD_SERVICE);
        });
        return tile;
    }

    private View otherServiceTile() {
        Service other = new Service("その他", "+", INK, Color.WHITE, 0, "その他", new Plan[] { new Plan("手入力", 1000, "月額") });
        LinearLayout tile = (LinearLayout) serviceTile(other);
        tile.setOnClickListener(v -> {
            selectedService = new Service("Custom Service", "+", INK, Color.WHITE, 0, "その他", new Plan[] { new Plan("手入力", 1000, "月額") });
            selectedPlan = selectedService.plans[0];
            show(Screen.PLAN);
        });
        return tile;
    }

    private View planRow(Plan plan) {
        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(42), dp(42));
        iconLp.setMargins(0, 0, dp(12), 0);
        row.addView(serviceIcon(selectedService, 42), iconLp);
        row.addView(text(plan.name + "\n" + plan.period + " / " + selectedService.category, 12, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text(yen.format(plan.price) + "\n月 " + yen.format(monthlyPrice(plan)), 11, INK, true));
        row.setOnClickListener(v -> {
            selectedPlan = plan;
            show(Screen.PLAN);
        });
        return row;
    }

    private View summaryCard(String a, String av, String b, String bv, String c, String cv) {
        LinearLayout box = card();
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(rounded(INK, 8, 0));
        box.addView(text(a + ": " + av, 11, PAPER, true));
        box.addView(text(b + ": " + bv, 11, PAPER, true));
        box.addView(text(c + ": " + cv, 11, PAPER, true));
        return box;
    }

    private View insight(String title, String body) {
        LinearLayout box = card();
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(text(title, 12, INK, true));
        box.addView(text(body, 10, MUTED, false));
        return box;
    }

    private View setting(String title, String detail, String tag) {
        LinearLayout row = card();
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(text(title + "\n" + detail, 12, INK, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(pill(tag, SOFT, INK));
        return row;
    }

    private View hero(String amount, String caption) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, dp(8), 0, dp(6));
        box.addView(text(amount, 40, INK, true));
        box.addView(text(caption, 10, MUTED, false));
        return box;
    }

    private View chart() {
        LinearLayout chart = new LinearLayout(this);
        chart.setGravity(Gravity.BOTTOM);
        chart.setPadding(0, dp(18), 0, dp(8));
        int[] heights = {72, 104, 132};
        int[] colors = {SAGE, RUST, INK};
        for (int i = 0; i < 3; i++) {
            TextView bar = new TextView(this);
            bar.setBackgroundColor(colors[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(heights[i]), 1);
            lp.setMargins(dp(6), 0, dp(6), 0);
            chart.addView(bar, lp);
        }
        return chart;
    }

    private View searchBox(String hint) {
        TextView search = text("検索  " + hint, 12, MUTED, false);
        search.setPadding(dp(12), dp(10), dp(12), dp(10));
        search.setBackground(rounded(SOFT, 8, LINE));
        return search;
    }

    private TextView label(String value) {
        TextView label = text(value, 11, INK, true);
        label.setPadding(0, dp(14), 0, dp(6));
        return label;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(rounded(0xfffbf6eb, 8, LINE));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(6), 0, dp(6));
        card.setLayoutParams(lp);
        return card;
    }

    private TextView primary(String label, View.OnClickListener listener) {
        TextView button = text(label, 13, PAPER, true);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(46));
        button.setPadding(dp(18), dp(8), dp(18), dp(8));
        button.setBackground(rounded(INK, 8, 0));
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout nav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(8), dp(8), dp(8), dp(8));
        nav.setBackground(rounded(INK, 16, 0));
        nav.addView(navItem("Home", Screen.HOME), new LinearLayout.LayoutParams(0, -2, 1));
        nav.addView(navItem("Spend", Screen.HOME), new LinearLayout.LayoutParams(0, -2, 1));
        nav.addView(navItem("Review", Screen.REVIEW), new LinearLayout.LayoutParams(0, -2, 1));
        nav.addView(navItem("Me", Screen.SETTINGS), new LinearLayout.LayoutParams(0, -2, 1));
        return nav;
    }

    private TextView navItem(String label, Screen target) {
        TextView item = text(label, 10, PAPER, true);
        item.setGravity(Gravity.CENTER);
        item.setOnClickListener(v -> show(target));
        return item;
    }

    private TextView actionButton(String label) {
        String visible = label;
        if ("x".equals(label)) visible = "×";
        if ("<".equals(label)) visible = "‹";
        TextView button = text(visible, 16, INK, true);
        button.setGravity(Gravity.CENTER);
        button.setMinWidth(dp(30));
        button.setMinHeight(dp(30));
        button.setIncludeFontPadding(false);
        button.setBackground(rounded(SOFT, 15, LINE));
        return button;
    }

    private TextView pill(String label, int bg, int fg) {
        TextView pill = text(label, 13, fg, true);
        pill.setGravity(Gravity.CENTER);
        pill.setMinWidth(dp(36));
        pill.setMinHeight(dp(36));
        pill.setPadding(dp(10), dp(6), dp(10), dp(6));
        pill.setBackground(rounded(bg, 18, 0));
        return pill;
    }

    private View serviceIcon(Service service, int sizeDp) {
        if (service.iconRes != 0) {
            ImageView icon = new ImageView(this);
            icon.setBackground(rounded(service.color, 8, darker(service.color)));
            icon.setImageResource(service.iconRes);
            icon.setColorFilter(service.iconTint);
            int padding = Math.max(5, Math.round(sizeDp * 0.18f));
            icon.setPadding(dp(padding), dp(padding), dp(padding), dp(padding));
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            icon.setMinimumWidth(dp(sizeDp));
            icon.setMinimumHeight(dp(sizeDp));
            return icon;
        }

        TextView icon = text(service.shortName, sizeDp >= 50 ? 20 : 16, service.textColor, true);
        icon.setGravity(Gravity.CENTER);
        icon.setIncludeFontPadding(false);
        icon.setMinWidth(dp(sizeDp));
        icon.setMinHeight(dp(sizeDp));
        icon.setBackground(rounded(service.color, 8, darker(service.color)));
        return icon;
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeColor != 0) drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private int darker(int color) {
        int r = Math.max(0, (int) (Color.red(color) * 0.76f));
        int g = Math.max(0, (int) (Color.green(color) * 0.76f));
        int b = Math.max(0, (int) (Color.blue(color) * 0.76f));
        return Color.rgb(r, g, b);
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setLineSpacing(dp(2), 1.0f);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private void editPrice(Subscription s) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(s.effectivePrice));
        new AlertDialog.Builder(this)
                .setTitle("金額を編集")
                .setMessage("実際の請求額を優先して保存します。")
                .setView(input)
                .setNegativeButton("キャンセル", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    try {
                        s.effectivePrice = Integer.parseInt(input.getText().toString());
                        s.overridden = s.effectivePrice != s.plan.price;
                    } catch (NumberFormatException ignored) {
                    }
                    show(Screen.DETAIL);
                })
                .show();
    }

    private Subscription addSubscription(Service service, Plan plan, int price) {
        Subscription s = new Subscription(service, plan, price);
        subscriptions.add(s);
        return s;
    }

    private int monthlyTotal() {
        int total = 0;
        for (Subscription s : subscriptions) total += monthlyPrice(s.effectivePrice, s.plan.period);
        return total;
    }

    private int monthlyPrice(Plan plan) {
        return monthlyPrice(plan.price, plan.period);
    }

    private int monthlyPrice(int price, String period) {
        if ("年額".equals(period)) return Math.round(price / 12f);
        if ("週額".equals(period)) return Math.round(price * 52f / 12f);
        return price;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    static class Service {
        final String name;
        final String shortName;
        final int color;
        final int iconTint;
        final int iconRes;
        final int textColor;
        final String category;
        final Plan[] plans;

        Service(String name, String shortName, int color, int iconTint, int iconRes, String category, Plan[] plans) {
            this.name = name;
            this.shortName = shortName;
            this.color = color;
            this.iconTint = iconTint;
            this.iconRes = iconRes;
            this.textColor = contrastText(color);
            this.category = category;
            this.plans = plans;
        }
    }

    private static int contrastText(int color) {
        int luminance = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000;
        return luminance > 150 ? INK : Color.WHITE;
    }

    static class Plan {
        final String name;
        final int price;
        final String period;

        Plan(String name, int price, String period) {
            this.name = name;
            this.price = price;
            this.period = period;
        }
    }

    static class Subscription {
        final Service service;
        final Plan plan;
        int effectivePrice;
        boolean overridden;

        Subscription(Service service, Plan plan, int effectivePrice) {
            this.service = service;
            this.plan = plan;
            this.effectivePrice = effectivePrice;
        }
    }
}

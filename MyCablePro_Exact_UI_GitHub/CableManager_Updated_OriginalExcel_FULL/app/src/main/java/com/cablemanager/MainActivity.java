package com.cablemanager;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.*;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.*;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_EXCEL = 1001;
    private static final int CREATE_EXCEL = 1002;
    private static final int CREATE_BACKUP = 1003;
    private static final int RESTORE_BACKUP = 1004;

    private final ArrayList<Customer> customers = new ArrayList<>();
    private final ArrayList<JSONObject> payments = new ArrayList<>();
    private final ArrayList<String> billedMonths = new ArrayList<>();

    private LinearLayout root;
    private LinearLayout content;
    private FrameLayout contentHost;
    private LinearLayout bottom;
    private TextView title;

    private Customer selected;

    private final int GREEN = Color.rgb(0, 170, 115);
    private final int DARK_GREEN = Color.rgb(0, 135, 95);
    private final int BLUE = Color.rgb(30, 105, 220);
    private final int PURPLE = Color.rgb(105, 80, 210);
    private final int BG = Color.rgb(245, 247, 249);
    private final int TEXT = Color.rgb(28, 40, 52);
    private final int SUBTEXT = Color.rgb(95, 105, 115);

    private final NumberFormat money =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(8, 37, 55));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        load();

        // Automatically generate the current month's bill once.
        generateCurrentMonth();

        showLogin();
    }

    // =========================================================
    // BASE SCREEN
    // =========================================================

    private void base(String screenTitle, boolean nav, boolean scroll) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        // TOP BAR
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(10), 0, dp(6), 0);
        bar.setBackgroundColor(GREEN);

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(38);
        back.setGravity(Gravity.CENTER);
        back.setPadding(0, 0, 0, dp(4));

        if (screenTitle.equals("MyCable Pro")) {
            back.setVisibility(View.INVISIBLE);
        } else {
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(v -> showDashboard());
        }

        bar.addView(
                back,
                new LinearLayout.LayoutParams(dp(48), dp(58))
        );

        title = new TextView(this);
        title.setText(screenTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);

        bar.addView(
                title,
                new LinearLayout.LayoutParams(0, dp(58), 1)
        );

        TextView more = new TextView(this);
        more.setText("⋮");
        more.setTextColor(Color.WHITE);
        more.setTextSize(30);
        more.setGravity(Gravity.CENTER);

        more.setOnClickListener(v -> showMenu());

        bar.addView(
                more,
                new LinearLayout.LayoutParams(dp(48), dp(58))
        );

        root.addView(bar);

        // CONTENT HOST
        contentHost = new FrameLayout(this);

        root.addView(
                contentHost,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(14)
        );

        if (scroll) {

            ScrollView sv = new ScrollView(this);
            sv.setFillViewport(true);
            sv.setClipToPadding(false);

            sv.addView(
                    content,
                    new ScrollView.LayoutParams(
                            -1,
                            -2
                    )
            );

            contentHost.addView(
                    sv,
                    new FrameLayout.LayoutParams(
                            -1,
                            -1
                    )
            );

        } else {

            contentHost.addView(
                    content,
                    new FrameLayout.LayoutParams(
                            -1,
                            -1
                    )
            );
        }

        if (nav) {
            buildBottom();
        }
    }

    // =========================================================
    // BOTTOM NAVIGATION
    // =========================================================

    private void buildBottom() {

        bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(Color.WHITE);
        bottom.setElevation(dp(5));

        String[] names = {
                "⌂\nHome",
                "♙\nCustomers",
                "▣\nBilling",
                "▤\nReports"
        };

        for (String n : names) {

            TextView t = new TextView(this);

            t.setText(n);
            t.setTextSize(11);
            t.setTextColor(SUBTEXT);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(7), 0, dp(5));

            t.setOnClickListener(v -> {

                String s = ((TextView) v).getText().toString();

                if (s.contains("Home")) {
                    showDashboard();

                } else if (s.contains("Customers")) {
                    showCustomers();

                } else if (s.contains("Billing")) {
                    showPayments();

                } else {
                    showReports();
                }
            });

            bottom.addView(
                    t,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(60),
                            1
                    )
            );
        }

        root.addView(bottom);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    private void showLogin() {

        LinearLayout page = new LinearLayout(this);

        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(
                dp(22),
                dp(20),
                dp(22),
                dp(20)
        );

        page.setBackground(
                gradient(
                        Color.rgb(0, 155, 145),
                        Color.rgb(4, 75, 125)
                )
        );

        TextView logo = label(
                "📺\nMyCable Pro",
                34,
                Color.WHITE,
                true
        );

        logo.setGravity(Gravity.CENTER);

        page.addView(
                logo,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(150)
                )
        );

        TextView sub = label(
                "Manage Customers\nCollect Payments\nGrow Your Business",
                14,
                Color.WHITE,
                false
        );

        sub.setGravity(Gravity.CENTER);

        page.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                )
        );

        EditText user = field("User Name");
        page.addView(
                user,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                )
        );

        EditText pass = field("Password");
        pass.setInputType(0x81);

        page.addView(
                pass,
                lp(12, 0)
        );

        Button login = button("LOGIN", GREEN);

        page.addView(
                login,
                lp(14, 0)
        );

        TextView hint = label(
                "Default Password : 123456",
                11,
                Color.WHITE,
                false
        );

        hint.setGravity(Gravity.CENTER);

        page.addView(
                hint,
                lp(8, 0)
        );

        login.setOnClickListener(v -> {

            String password = pass.getText().toString();

            if (password.isEmpty() ||
                    password.equals("123456")) {

                showDashboard();

            } else {

                toast("Use password 123456");
            }
        });

        setContentView(page);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        base("MyCable Pro", true, true);

        TextView hello = label(
                "Vanakkam,\nAdmin",
                17,
                TEXT,
                true
        );

        content.addView(
                hello,
                lp(0, 8)
        );

        double due = 0;
        double advance = 0;
        int active = 0;

        for (Customer c : customers) {

            due += Math.max(0, c.dueAmount);
            advance += Math.max(0, c.advanceAmount);

            if (!"Inactive".equalsIgnoreCase(c.status)) {
                active++;
            }
        }

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);

        addStatRow(
                stats,
                new String[]{
                        "👥  Total Customers\n" + customers.size(),
                        "✓  Active\n" + active
                },
                new int[]{
                        Color.rgb(220, 238, 255),
                        Color.rgb(220, 248, 228)
                }
        );

        addStatRow(
                stats,
                new String[]{
                        "⊗  Due\n" + money.format(due),
                        "₹  Advance\n" + money.format(advance)
                },
                new int[]{
                        Color.rgb(255, 229, 229),
                        Color.rgb(255, 240, 205)
                }
        );

        content.addView(
                stats,
                lp(0, 10)
        );

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        String[][] items = {
                {"👥\nCustomers", "▣\nBilling", "₹\nCollection"},
                {"▤\nPackages", "📍\nZones", "▥\nReports"}
        };

        for (String[] row : items) {

            LinearLayout r = new LinearLayout(this);

            for (String x : row) {

                TextView t = card(x, 15);

                LinearLayout.LayoutParams p =
                        new LinearLayout.LayoutParams(
                                0,
                                dp(84),
                                1
                        );

                p.setMargins(dp(3), dp(3), dp(3), dp(3));

                r.addView(t, p);

                if (x.contains("Customers")) {
                    t.setOnClickListener(v -> showCustomers());

                } else if (x.contains("Billing")) {
                    t.setOnClickListener(v -> showPayments());

                } else if (x.contains("Collection")) {
                    t.setOnClickListener(v -> showCollect(false));

                } else if (x.contains("Packages")) {
                    t.setOnClickListener(v -> showPackages());

                } else if (x.contains("Zones")) {
                    t.setOnClickListener(v -> showZones());

                } else {
                    t.setOnClickListener(v -> showReports());
                }
            }

            grid.addView(r);
        }

        content.addView(
                grid,
                lp(0, 10)
        );

        LinearLayout exportRow = new LinearLayout(this);

        Button imp = button(
                "Import Excel",
                BLUE
        );

        Button exp = button(
                "Export Excel",
                GREEN
        );

        exportRow.addView(
                imp,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        exportRow.addView(
                exp,
                lpw(8)
        );

        content.addView(
                exportRow,
                lp(0, 10)
        );

        imp.setOnClickListener(v -> pickExcel());
        exp.setOnClickListener(v -> createExport());

        Button monthly = button(
                "GENERATE MONTHLY BILL",
                PURPLE
        );

        content.addView(
                monthly,
                lp(0, 8)
        );

        monthly.setOnClickListener(v -> showMonthly());
    }

    private void addStatRow(
            LinearLayout parent,
            String[] values,
            int[] colors) {

        LinearLayout r = new LinearLayout(this);

        for (int i = 0; i < values.length; i++) {

            TextView t = card(
                    values[i],
                    14
            );

            t.setBackground(
                    round(colors[i], 14)
            );

            t.setTextColor(TEXT);
            t.setGravity(Gravity.CENTER);

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(76),
                            1
                    );

            p.setMargins(dp(3), 0, dp(3), 0);

            r.addView(t, p);
        }

        parent.addView(
                r,
                lp(0, 6)
        );
    }

    // =========================================================
    // CUSTOMERS
    // =========================================================

    private void showCustomers() {

        base("Customers", true, false);

        LinearLayout searchRow = new LinearLayout(this);

        EditText search =
                field("Search name, mobile, box...");

        searchRow.addView(
                search,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        Button filterButton =
                smallButton("☷");

        searchRow.addView(
                filterButton,
                lpw(6)
        );

        content.addView(
                searchRow,
                lp(0, 7)
        );

        LinearLayout chips = new LinearLayout(this);

        String[] chipNames = {
                "All",
                "Due",
                "Advance",
                "Deactive"
        };

        for (String x : chipNames) {

            TextView chip = chip(x);

            chips.addView(
                    chip,
                    chipParams()
            );

            chip.setOnClickListener(
                    v -> {

                        String mode =
                                ((TextView) v).getText().toString();

                        refreshCustomerList(
                                search.getText().toString(),
                                mode
                        );
                    }
            );
        }

        content.addView(chips);

        FrameLayout listHost = new FrameLayout(this);

        content.addView(
                listHost,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        ListView list = new ListView(this);

        list.setDivider(null);
        list.setPadding(0, dp(5), 0, 0);
        list.setBackgroundColor(BG);
        list.setClipToPadding(false);

        listHost.addView(
                list,
                new FrameLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        filterButton.setOnClickListener(
                v -> showFilter()
        );

        search.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        refreshCustomerList(
                                s.toString(),
                                "All"
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {
                    }
                }
        );

        renderCustomers(
                list,
                getFilteredCustomers(
                        "",
                        "All"
                )
        );
    }

    private void refreshCustomerList(
            String query,
            String mode) {

        if (content == null) return;

        ListView list = findCustomerList();

        if (list == null) return;

        renderCustomers(
                list,
                getFilteredCustomers(
                        query,
                        mode
                )
        );
    }

    private ListView findCustomerList() {

        if (contentHost == null) return null;

        return findListView(contentHost);
    }

    private ListView findListView(ViewGroup parent) {

        for (int i = 0; i < parent.getChildCount(); i++) {

            View child = parent.getChildAt(i);

            if (child instanceof ListView) {
                return (ListView) child;
            }

            if (child instanceof ViewGroup) {

                ListView result =
                        findListView((ViewGroup) child);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    private List<Customer> getFilteredCustomers(
            String query,
            String mode) {

        ArrayList<Customer> result =
                new ArrayList<>();

        String q =
                query == null
                        ? ""
                        : query.trim().toLowerCase(Locale.US);

        for (Customer c : customers) {

            String hay =
                    (c.boxId + " " +
                            c.name + " " +
                            c.phone + " " +
                            c.address + " " +
                            c.zone + " " +
                     

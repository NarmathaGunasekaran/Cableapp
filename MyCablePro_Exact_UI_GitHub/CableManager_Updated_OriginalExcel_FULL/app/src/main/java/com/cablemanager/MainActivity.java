package com.cablemanager;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
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

    private final NumberFormat money =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    private LinearLayout root;
    private LinearLayout content;
    private LinearLayout bottom;
    private TextView title;

    private final int GREEN = Color.rgb(0, 184, 124);
    private final int DARK = Color.rgb(0, 155, 110);
    private final int BLUE = Color.rgb(23, 105, 220);
    private final int BG = Color.rgb(246, 248, 250);
    private final int TEXT = Color.rgb(30, 42, 55);

    private Customer selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(12, 31, 50));
        getWindow().setNavigationBarColor(Color.WHITE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        load();
        showLogin();
    }

    // =========================================================
    // BASE SCREEN
    // =========================================================

    private void base(String screenTitle, boolean nav) {
        base(screenTitle, nav, true);
    }

    private void base(String screenTitle, boolean nav, boolean showBack) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        // TOP BAR
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(8), dp(5), dp(8), dp(5));
        bar.setBackgroundColor(Color.rgb(0, 157, 113));

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(38);
        back.setGravity(Gravity.CENTER);
        back.setVisibility(showBack ? View.VISIBLE : View.INVISIBLE);

        back.setOnClickListener(v -> showDashboard());

        bar.addView(
                back,
                new LinearLayout.LayoutParams(dp(46), dp(54))
        );

        title = new TextView(this);
        title.setText(screenTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);

        bar.addView(
                title,
                new LinearLayout.LayoutParams(0, dp(54), 1)
        );

        TextView more = new TextView(this);
        more.setText("⋮");
        more.setTextColor(Color.WHITE);
        more.setTextSize(30);
        more.setGravity(Gravity.CENTER);

        more.setOnClickListener(v -> showMenu());

        bar.addView(
                more,
                new LinearLayout.LayoutParams(dp(42), dp(54))
        );

        root.addView(bar);

        // CONTENT
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(nav ? 12 : 20)
        );

        scroll.addView(
                content,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

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
            t.setGravity(Gravity.CENTER);
            t.setTextColor(Color.DKGRAY);
            t.setPadding(0, dp(6), 0, dp(5));

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
                dp(24),
                dp(20),
                dp(24),
                dp(20)
        );

        page.setBackground(
                gradient(
                        Color.rgb(0, 151, 145),
                        Color.rgb(4, 74, 125)
                )
        );

        TextView tv = label(
                "📺\nMyCable Pro",
                34,
                Color.WHITE,
                true
        );

        tv.setGravity(Gravity.CENTER);

        page.addView(
                tv,
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

        EditText user = field("Username");
        page.addView(
                user,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                )
        );

        EditText pass = field("Password");
        pass.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        page.addView(pass, lp(12));

        Button login = button("LOGIN", GREEN);

        page.addView(login, lp(14));

        TextView hint = label(
                "Username: Admin\nPassword: 123456",
                12,
                Color.WHITE,
                false
        );

        hint.setGravity(Gravity.CENTER);

        page.addView(hint, lp(10));

        login.setOnClickListener(v -> {

            String username =
                    user.getText().toString().trim();

            String password =
                    pass.getText().toString();

            if (username.isEmpty()) {
                toast("Enter username");
                return;
            }

            if (!password.equals("123456")) {
                toast("Incorrect password");
                return;
            }

            showDashboard();
        });

        setContentView(page);
    }

    // =========================================================
    // DASHBOARD
    // =========================================================

    private void showDashboard() {

        base("MyCable Pro", true, false);

        TextView hello = label(
                "Vanakkam,\nAdmin",
                17,
                TEXT,
                true
        );

        content.addView(
                hello,
                lp(2)
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

        // STATISTICS
        addStatRow(
                new String[]{
                        "👥  Total Customers\n" + customers.size(),
                        "✓  Active\n" + active
                },
                new int[]{
                        Color.rgb(215, 235, 255),
                        Color.rgb(218, 250, 226)
                }
        );

        addStatRow(
                new String[]{
                        "⊗  Due\n" + money.format(due),
                        "₹  Advance\n" + money.format(advance)
                },
                new int[]{
                        Color.rgb(255, 224, 224),
                        Color.rgb(255, 238, 201)
                }
        );

        // MAIN MENU GRID
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        addDashboardRow(
                grid,
                new String[]{
                        "👥\nCustomers",
                        "▣\nBilling",
                        "₹\nCollection"
                }
        );

        addDashboardRow(
                grid,
                new String[]{
                        "▤\nPackages",
                        "📍\nZones",
                        "▥\nReports"
                }
        );

        content.addView(grid, lp(8));

        // IMPORT / EXPORT
        LinearLayout q = new LinearLayout(this);
        q.setOrientation(LinearLayout.HORIZONTAL);

        Button imp = button("Import Excel", BLUE);
        Button exp = button("Export Excel", GREEN);

        q.addView(
                imp,
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                )
        );

        LinearLayout.LayoutParams expLp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                );

        expLp.setMargins(dp(8), 0, 0, 0);

        q.addView(exp, expLp);

        content.addView(q, lp(10));

        imp.setOnClickListener(v -> pickExcel());
        exp.setOnClickListener(v -> createExport());
    }

    private void addStatRow(String[] values, int[] colors) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < values.length; i++) {

            TextView t = card(
                    values[i],
                    15,
                    TEXT
            );

            t.setBackground(
                    round(colors[i], 14)
            );

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(82),
                            1
                    );

            if (i > 0) {
                p.setMargins(dp(6), 0, 0, 0);
            }

            row.addView(t, p);
        }

        content.addView(row, lp(7));
    }

    private void addDashboardRow(
            LinearLayout parent,
            String[] items
    ) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (String item : items) {

            TextView t = card(
                    item,
                    15,
                    TEXT
            );

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(88),
                            1
                    );

            p.setMargins(
                    dp(2),
                    dp(3),
                    dp(2),
                    dp(3)
            );

            row.addView(t, p);

            if (item.contains("Customers")) {
                t.setOnClickListener(v -> showCustomers());
            } else if (item.contains("Billing")) {
                t.setOnClickListener(v -> showPayments());
            } else if (item.contains("Collection")) {
                t.setOnClickListener(v -> showCollect(false));
            } else if (item.contains("Packages")) {
                t.setOnClickListener(v -> showPackages());
            } else if (item.contains("Zones")) {
                t.setOnClickListener(v -> showZones());
            } else if (item.contains("Reports")) {
                t.setOnClickListener(v -> showReports());
            }
        }

        parent.addView(row);
    }

    // =========================================================
    // CUSTOMERS
    // =========================================================

    private void showCustomers() {

        base("Customers", true, true);

        LinearLayout searchRow =
                new LinearLayout(this);

        searchRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

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

        LinearLayout.LayoutParams fp =
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(50)
                );

        fp.setMargins(dp(6), 0, 0, 0);

        searchRow.addView(
                filterButton,
                fp
        );

        content.addView(
                searchRow,
                lp(0)
        );

        // FILTER CHIPS
        LinearLayout chips =
                new LinearLayout(this);

        chips.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] chipNames = {
                "All",
                "Due",
                "Advance",
                "Inactive"
        };

        for (String x : chipNames) {

            TextView chip = chip(x);

            LinearLayout.LayoutParams cp =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(40),
                            1
                    );

            cp.setMargins(
                    dp(2),
                    0,
                    dp(2),
                    0
            );

            chips.addView(chip, cp);

            chip.setOnClickListener(
                    v -> renderCustomers(
                            search,
                            ((TextView) v)
                                    .getText()
                                    .toString()
                    )
            );
        }

        content.addView(chips, lp(8));

        // CUSTOMER LIST
        ListView list = new ListView(this);

        list.setDivider(null);
        list.setDividerHeight(0);
        list.setBackgroundColor(BG);
        list.setPadding(0, dp(5), 0, dp(5));

        content.addView(
                list,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(500)
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
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        renderCustomers(
                                list,
                                s.toString(),
                                "All"
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );

        renderCustomers(
                list,
                "",
                "All"
        );
    }

    private void renderCustomers(
            EditText search,
            String mode
    ) {

        if (content == null) {
            return;
        }

        String q =
                search.getText()
                        .toString();

        // Rebuild customer screen with current filter
        showCustomersFiltered(q, mode);
    }

    private void showCustomersFiltered(
            String query,
            String mode
    ) {

        // Prevent recursive rebuild from search
        base("Customers", true, true);

        LinearLayout searchRow =
                new LinearLayout(this);

        searchRow.setOrientation(
                LinearLayout.HORIZONTAL
        );

        EditText search =
                field("Search name, mobile, box...");

        search.setText(query);
        search.setSelection(
                search.length()
        );

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

        LinearLayout.LayoutParams fp =
                new LinearLayout.LayoutParams(
                        dp(54),
                        dp(50)
                );

        fp.setMargins(dp(6), 0, 0, 0);

        searchRow.addView(filterButton, fp);

        content.addView(searchRow);

        LinearLayout chips =
                new LinearLayout(this);

        chips.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] chipNames = {
                "All",
                "Due",
                "Advance",
             

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
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        getWindow().setStatusBarColor(Color.rgb(12, 31, 50));
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        load();
        showLogin();
    }

    // ============================================================
    // BASE SCREEN
    // ============================================================

    private void base(String screenTitle, boolean nav) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(6)
        );
        bar.setBackgroundColor(Color.rgb(0, 157, 113));

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(34);
        back.setGravity(Gravity.CENTER);
        back.setVisibility(
                screenTitle.equals("MyCable Pro")
                        ? View.GONE
                        : View.VISIBLE
        );

        back.setOnClickListener(v -> showDashboard());

        bar.addView(
                back,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(54)
                )
        );

        title = new TextView(this);
        title.setText(screenTitle);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(null, 1);
        title.setGravity(Gravity.CENTER_VERTICAL);

        bar.addView(
                title,
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                )
        );

        TextView more = new TextView(this);
        more.setText("⋮");
        more.setTextColor(Color.WHITE);
        more.setTextSize(30);
        more.setGravity(Gravity.CENTER);

        more.setOnClickListener(v -> showMenu());

        bar.addView(
                more,
                new LinearLayout.LayoutParams(
                        dp(42),
                        dp(54)
                )
        );

        root.addView(bar);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(nav ? 8 : 14)
        );

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);

        scroll.addView(
                content,
                new ScrollView.LayoutParams(
                        -1,
                        -1
                )
        );

        root.addView(
                scroll,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        if (nav) {
            buildBottom();
        }
    }

    // ============================================================
    // BOTTOM NAVIGATION
    // ============================================================

    private void buildBottom() {

        bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.HORIZONTAL);
        bottom.setGravity(Gravity.CENTER);
        bottom.setBackgroundColor(Color.WHITE);
        bottom.setPadding(0, dp(2), 0, dp(2));

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
                            dp(58),
                            1
                    )
            );
        }

        root.addView(
                bottom,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(62)
                )
        );
    }

    // ============================================================
    // LOGIN
    // ============================================================

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

        EditText user = field("User Name");
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

        page.addView(pass, lp(0, 12));

        Button login = button("LOGIN", GREEN);

        page.addView(
                login,
                lp(0, 14)
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
                lp(0, 8)
        );

        login.setOnClickListener(v -> {

            String password = pass.getText().toString();

            if (password.isEmpty() || password.equals("123456")) {
                showDashboard();
            } else {
                toast("Use password 123456");
            }
        });

        setContentView(page);
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    private void showDashboard() {

        base("MyCable Pro", true);

        TextView hello = label(
                "Vanakkam,\nAdmin",
                16,
                TEXT,
                true
        );

        content.addView(
                hello,
                lp(0, 2)
        );

        double due = 0;
        double advance = 0;
        int active = 0;

        for (Customer c : customers) {

            if (c == null) continue;

            due += Math.max(0, c.dueAmount);
            advance += Math.max(0, c.advanceAmount);

            if (!"Inactive".equalsIgnoreCase(safe(c.status))) {
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
                        Color.rgb(215, 235, 255),
                        Color.rgb(218, 250, 226)
                }
        );

        addStatRow(
                stats,
                new String[]{
                        "⊗  Due\n" + money.format(due),
                        "₹  Advance\n" + money.format(advance)
                },
                new int[]{
                        Color.rgb(255, 224, 224),
                        Color.rgb(255, 238, 201)
                }
        );

        content.addView(
                stats,
                lp(0, 10)
        );

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        String[][] cards = {
                {"👥\nCustomers", "▣\nBilling", "₹\nCollection"},
                {"▤\nPackages", "📍\nZones", "▥\nReports"}
        };

        for (String[] row : cards) {

            LinearLayout r = new LinearLayout(this);
            r.setOrientation(LinearLayout.HORIZONTAL);

            for (String x : row) {

                TextView t = card(x, 15);

                r.addView(
                        t,
                        new LinearLayout.LayoutParams(
                                0,
                                dp(82),
                                1
                        )
                );

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

            grid.addView(
                    r,
                    lp(0, 6)
            );
        }

        content.addView(
                grid,
                lp(0, 8)
        );

        LinearLayout q = new LinearLayout(this);
        q.setOrientation(LinearLayout.HORIZONTAL);

        Button imp = button("Import Excel", BLUE);
        Button exp = button("Export Excel", GREEN);

        q.addView(
                imp,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        q.addView(
                exp,
                lpw(8)
        );

        content.addView(
                q,
                lp(0, 10)
        );

        imp.setOnClickListener(v -> pickExcel());
        exp.setOnClickListener(v -> createExport());
    }

    private void addStatRow(
            LinearLayout parent,
            String[] vals,
            int[] colors
    ) {

        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < 2; i++) {

            TextView t = card(vals[i], 15);

            t.setBackground(
                    round(colors[i], 14)
            );

            t.setTextColor(TEXT);

            r.addView(
                    t,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(76),
                            1
                    )
            );
        }

        parent.addView(
                r,
                lp(0, 6)
        );
    }

    // ============================================================
    // CUSTOMERS
    // ============================================================

    private void showCustomers() {

        base("Customers", true);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);

        EditText search = field(
                "Search name, mobile, box..."
        );

        searchRow.addView(
                search,
                new LinearLayout.LayoutParams(
                        0,
                        dp(50),
                        1
                )
        );

        Button filterButton = smallButton("☷");

        searchRow.addView(
                filterButton,
                new LinearLayout.LayoutParams(
                        dp(52),
                        dp(50)
                )
        );

        content.addView(
                searchRow,
                lp(0, 8)
        );

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);

        String[] chipNames = {
                "All",
                "Due",
                "Advance",
                "Deactive"
        };

        for (String x : chipNames) {

            TextView c = chip(x);

            chips.addView(
                    c,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(40),
                            1
                    )
            );

            c.setOnClickListener(
                    v -> filterCustomers(
                            search,
                            ((TextView) v).getText().toString()
                    )
            );
        }

        content.addView(
                chips,
                lp(0, 8)
        );

        ListView list = new ListView(this);

        list.setDivider(null);
        list.setDividerHeight(0);
        list.setBackgroundColor(BG);
        list.setPadding(0, dp(4), 0, dp(4));

        content.addView(
                list,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(420)
                )
        );

        filterButton.setOnClickListener(
                v -> showFilter()
        );

        renderCustomers(
                list,
                new ArrayList<>(customers)
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
                                filter(
                                        s == null
                                                ? ""
                                                : s.toString(),
                                        "All"
                                )
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }

    private ArrayList<Customer> filter(
            String q,
            String mode
    ) {

        ArrayList<Customer> out =
                new ArrayList<>();

        String query = safe(q)
                .trim()
                .toLowerCase(Locale.US);

        for (Customer c : customers) {

            if (c == null) continue;

            String hay =
                    safe(c.name) + " " +
                    safe(c.phone) + " " +
                    safe(c.boxId) + " " +
                    safe(c.address) + " " +
                    safe(c.zone);

            boolean matches =
                    query.isEmpty() ||
                            hay.toLowerCase(Locale.US)
                                    .contains(query);

            if (!matches) continue;

            if ("Due".equalsIgnoreCase(mode)
                    && c.dueAmount <= 0) {
                continue;
            }

            if ("Advance".equalsIgnoreCase(mode)
                    && c.advanceAmount <= 0) {
                continue;
            }

            if ("Deactive".equalsIgnoreCase(mode)
                    && !"Inactive".equalsIgnoreCase(
                    safe(c.status))) {
                continue;
            }

            out.add(c);
        }

        return out;
    }

    private void filterCustomers(
            EditText search,
            String mode
    ) {

        String q = search == null
                ? ""
                : search.getText().toString();

        ArrayList<Customer> result =
                filter(q, mode);

        toast(
                mode + ": " + result.size()
                        + " customer(s)"
        );
    }

    // ============================================================
    // CUSTOMER LIST - FIXED
    // ============================================================

    private void renderCustomers(
            ListView list,
            List<Customer> data
    ) {

        ArrayList<Customer> safeData =
                new ArrayList<>();

        if (data != null) {

            for (Customer c : data) {

                if (c != null) {
                    safeData.add(c);
                }
            }
        }

        ArrayAdapter<Customer> adapter =
                new ArrayAdapter<Customer>(
                        this,
                        android.R.layout.simple_list_item_2,
                        safeData
                ) {

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent
                    ) {

                        View view = super.getView(
                                position,
                                convertView,
                                parent
                        );

                        Customer c =
                                getItem(position);

                        if (c == null) {
                            return view;
                        }

                        TextView first =
                                view.findViewById(
                                        android.R.id.text1
                                );

                        TextView second =
                                view.findViewById(
                                        android.R.id.text2
                                );

                        String name =
                                safe(c.name);

                        String box =
                                safe(c.boxId);

                        String phone =
                                safe(c.phone);

                        String zone =
                                safe(c.zone);

                        String status;

                        if (c.dueAmount > 0) {
                            status = "Due";
                        } else if (c.advanceAmount > 0) {
                            status = "Advance";
                        } else {
                            status = "OK";
                        }

                        first.setText(
                                name.isEmpty()
                                        ? "Unnamed customer"
                                        : name
                        );

                        first.setTextSize(16);
                        first.setTextColor(TEXT);
                        first.setTypeface(
                                null,
                                1
                        );

                        second.setText(
                                "BOX ID: "
                                        + (box.isEmpty()
                                        ? "-"
                                        : box)
                                        + "   |   Phone: "
                                        + (phone.isEmpty()
                                        ? "-"
                                        : phone)
                                        + "\n"
                                        + (zone.isEmpty()
                                        ? "-"
                                        : zone)
                                        + "   •   "
                                        + status
                                        + "   •   Due ₹"
                                        + fmt(
                                        Math.max(
                                                0,
                                                c.dueAmount
                                        )
                                )
                        );

                        second.setTextSize(12);
                        second.setTextColor(
                                Color.DKGRAY
                        );

                        view.setPadding(
                                dp(8),
                                dp(8),
                                dp(8),
                                dp(8)
                        );

                        GradientDrawable bg =
                                round(
                                        Color.WHITE,
                                        12
                                );

                        view.setBackground(bg);

                        return view;
                    }
                };

        list.setAdapter(adapter);

        list.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Customer c =
                            adapter.getItem(position);

                    if (c == null) {
                        toast(
                                "Customer data unavailable"
                        );
                        return;
                    }

                    selected = c;

                    showDetails(c);
                }
        );
    }

    // ============================================================
    // CUSTOMER DETAILS
    // ============================================================

    private void showDetails(Customer c) {

        if (c == null) {
            toast("Customer not found");
            showCustomers();
            return;
        }

        selected = c;

        base(
                "Customer Details",
                true
        );

        String name = safe(c.name);
        String phone = safe(c.phone);
        String boxId = safe(c.boxId);
        String zone = safe(c.zone);
        String packageName = safe(c.packageName);
        String status = safe(c.status);
        String address = safe(c.address);
        String cardNo = safe(c.cardNo);

        String avatarText = "C";

        if (!name.isEmpty()) {
            avatarText = name
                    .substring(0, 1)
                    .toUpperCase(Locale.US);
        }

        LinearLayout head =
                new LinearLayout(this);

        head.setOrientation(
                LinearLayout.HORIZONTAL
        );

        head.setGravity(
                Gravity.CENTER_VERTICAL
        );

        TextView avatar =
                card(
                        avatarText,
                        22,
                        BLUE
                );

        head.addView(
                avatar,
                new LinearLayout.LayoutParams(
                        dp(62),
                        dp(62)
                )
        );

        TextView h = label(
                (name.isEmpty()
                        ? "Unnamed customer"
                        : name)
                        + "\n"
                        + (phone.isEmpty()
                        ? "-"
                        : phone)
                        + "\n"
                        + (boxId.isEmpty()
                        ? "-"
                        : boxId),
                15,
                TEXT,
                true
        );

        head.addView(
                h,
                new LinearLayout.LayoutParams(
                        0,
                        dp(70),
                        1
                )
        );

        content.addView(
                head,
                lp(0, 8)
        );

        content.addView(
                info(
                        "Zone",
                        zone.isEmpty()
                                ? "-"
                                : zone
                ),
                lp(0, 5)
        );

        content.addView(
                info(
                        "Package",
                        (packageName.isEmpty()
                                ? "-"
                                : packageName)
                                + "  •  ₹"
                                + fmt(c.packageCost)
                ),
                lp(0, 5)
        );

        content.addView(
                info(
                        "Status",
                        status.isEmpty()
                                ? "-"
                                : status
                ),
                lp(0, 5)
        );

        LinearLayout balances =
                new LinearLayout(this);

        balances.setOrientation(
                LinearLayout.HORIZONTAL
        );

        balances.addView(
                balance(
                        "Total Bill",
                        c.packageCost
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(72),
                        1
                )
        );

        balances.addView(
                balance(
                        "Paid",
                        Math.max(
                                0,
                                c.packageCost
                                        - c.dueAmount
                        )
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(72),
                        1
                )
        );

        balances.addView(
                balance(
                        "Due",
                        Math.max(
                                0,
                                c.dueAmount
                        )
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(72),
                        1
                )
        );

        balances.addView(
                balance(
                        "Advance",
                        Math.max(
                                0,
                                c.advanceAmount
                        )
                ),
                new LinearLayout.LayoutParams(
                        0,
                        dp(72),
                        1
                )
        );

        content.addView(
                balances,
                lp(0, 10)
        );

        LinearLayout actions =
                new LinearLayout(this);

        actions.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button collect =
                button(
                        "Collect Payment",
                        GREEN
                );

        Button history =
                button(
                        "Payment History",
                        BLUE
                );

        Button edit =
                button(
                        "Edit",
                        Color.rgb(
                                90,
                                90,
                                220
                        )
                );

        actions.addView(
                collect,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        actions.addView(
                history,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        actions.addView(
                edit,
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                )
        );

        content.addView(
                actions,
                lp(0, 8)
        );

        collect.setOnClickListener(v -> {
            selected = c;
            showCollect(false);
        });

        history.setOnClickListener(v -> {
            selected = c;
            showPaymentHistory(c);
        });

        edit.setOnClickListener(v -> {
            selected = c;
            showAdd(c);
        });

        content.addView(
                info(
                        "Address",
                        address.isEmpty()
                                ? "-"
                                : address
                ),
                lp(0, 5)
        );

        content.addView(
                info(
                        "Card / Smart Card",
                        cardNo.isEmpty()
                                ? "-"
                                : cardNo
                ),
                lp(0, 5)
        );
    }

    // ============================================================
    // ADD / EDIT CUSTOMER
    // ============================================================

    private void showAdd(Customer edit) {

        base(
                edit == null
                        ? "Add Customer"
                        : "Edit Customer",
                true
        );

        EditText name =
                field("Customer Name *");

        EditText phone =
                field("Mobile Number *");

        EditText box =
                field("Box / Smart Card No *");

        EditText zone =
                field("Zone *");

        EditText pkg =
                field("Package *");

        EditText amount =
                field("Monthly Amount *");

        amount.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        EditText address =
                field("Address");

        EditText[] fields = {
                name,
                phone,
                box,
                zone,
                pkg,
                amount,
                address
        };

        for (EditText e : fields) {
            content.addView(
                    e,
                    lp(0, 7)
            );
        }

        if (edit != null) {

            name.setText(safe(edit.name));
            phone.setText(safe(edit.phone));
            box.setText(safe(edit.boxId));
            zone.setText(safe(edit.zone));
            pkg.setText(safe(edit.packageName));
            amount.setText(
                    String.valueOf(
                            edit.packageCost
                    )
            );
            address.setText(
                    safe(edit.address)
            );
        }

        RadioGroup rg =
                new RadioGroup(this);

        RadioButton active =
                new RadioButton(this);

        active.setText("Active");

        active.setChecked(
                edit == null ||
                        !"Inactive".equalsIgnoreCase(
                                safe(edit.status)
                        )
        );

        RadioButton inactive =
                new RadioButton(this);

        inactive.setText("Inactive");

        rg.addView(active);
        rg.addView(inactive);

        content.addView(
                label(
                        "Status",
                        13,
                        TEXT,
                        true
                )
        );

        content.addView(rg);

        Button saveButton =
                button(
                        "SAVE",
                        GREEN
                );

        content.addView(
                saveButton,
                lp(0, 10)
        );

        saveButton.setOnClickListener(v -> {

            String boxValue =
                    safe(box.getText().toString())
                            .trim();

            if (boxValue.isEmpty()) {
                toast("Box ID required");
                return;
            }

            Customer c =
                    edit == null
                            ? new Customer()
                            : edit;

            c.name =
                    name.getText().toString().trim();

            c.phone =
                    phone.getText().toString().trim();

            c.boxId =
                    boxValue;

            c.zone =
                    zone.getText().toString().trim();

            c.packageName =
                    pkg.getText().toString().trim();

            c.packageCost =
                    num(amount);

            c.address =
                    address.getText().toString().trim();

            c.status =
                    active.isChecked()
                            ? "Active"
                            : "Inactive";

            if (edit == null) {
                customers.add(c);
            }

            save();

            selected = c;

            toast("Customer saved");

            showDetails(c);
        });
    }

    // ============================================================
    // PAYMENT
    // ============================================================

    private void showCollect(
            boolean advanceOnly
    ) {

        if (selected == null &&
                !customers.isEmpty()) {

            selected = customers.get(0);
        }

        if (selected == null) {
            showAdd(null);
            return;
        }

        Customer c = selected;

        base(
                advanceOnly
                        ? "Advance Payment"
                        : "Collect Payment",
                true
        );

        content.addView(
                info(
                        "Customer",
                        safe(c.name)
                                + "\n"
                                + safe(c.boxId)
                ),
                lp(0, 5)
        );

        content.addView(
                info(
                        "Current Due",
                        money.format(
                                Math.max(
                                        0,
                                        c.dueAmount
                                )
                        )
                ),
                lp(0, 5)
        );

        content.addView(
                info(
                        "Advance Balance",
                        money.format(
                                Math.max(
                                        0,
                                        c.advanceAmount
                                )
                        )
                ),
                lp(0, 8)
        );

        RadioGroup type =
                new RadioGroup(this);

        RadioButton normal =
                new RadioButton(this);

        normal.setText(
                "Normal Payment"
        );

        normal.setChecked(
                !advanceOnly
        );

        RadioButton advance =
                new RadioButton(this);

        advance.setText(
                "Advance Payment"
        );

        advance.setChecked(
                advanceOnly
        );

        type.addView(normal);
        type.addView(advance);

        content.addView(
                label(
                        "Payment Type",
                        13,
                        TEXT,
                        true
                )
        );

        content.addView(type);

        EditText amount =
                field("Amount Paid");

        amount.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        content.addView(
                amount,
                lp(0, 8)
        );

        Spinner mode =
                new Spinner(this);

        mode.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        new String[]{
                                "Cash",
                                "Cheque",
                                "UPI/Other"
                        }
                )
        );

        content.addView(
                mode,
                lp(0, 8)
        );

        EditText notes =
                field("Notes (optional)");

        content.addView(
                notes,
                lp(0, 8)
        );

        Button saveButton =
                button(
                        "SAVE PAYMENT",
                        GREEN
                );

        content.addView(
                saveButton,
                lp(0, 10)
        );

        saveButton.setOnClickListener(v -> {

            double paid =
                    num(amount);

            if (paid <= 0) {
                toast("Enter amount");
                return;
            }

            boolean isAdvance =
                    advance.isChecked();

            if (isAdvance) {

                c.advanceAmount += paid;

            } else {

                double used =
                        Math.min(
                                paid,
                                Math.max(
                                        0,
                                        c.dueAmount
                                )
                        );

                c.dueAmount -= used;

                double extra =
                        paid - used;

                if (extra > 0) {
                    c.advanceAmount += extra;
                }
            }

            JSONObject payment =
                    new JSONObject();

            try {

                payment.put(
                        "boxId",
                        safe(c.boxId)
                );

                payment.put(
                        "name",
                        safe(c.name)
                );

                payment.put(
                        "amount",
                        paid
                );

                payment.put(
                        "type",
                        isAdvance
                                ? "Advance"
                                : "Normal"
                );

                payment.put(
                        "mode",
                        mode.getSelectedItem()
                                .toString()
                );

                payment.put(
                        "date",
                        date()
                );

                payment.put(
                        "notes",
                        notes.getText()
                                .toString()
                );

                payments.add(payment);

            } catch (Exception ignored) {
            }

            save();

            toast("Payment saved");

            showDetails(c);
        });
    }

    // ============================================================
    // PAYMENT HISTORY
    // ============================================================

    private void showPaymentHistory(
            Customer c
    ) {

        base(
                "Payment History",
                true
        );

        content.addView(
                info(
                        "Customer",
                        safe(c.name)
                                + "\n"
                                + safe(c.boxId)
                ),
                lp(0, 8)
        );

        LinearLayout head =
                new LinearLayout(this);

        head.setOrientation(
                LinearLayout.HORIZONTAL
        );

        String[] headers = {
                "Month",
                "Bill",
                "Paid",
                "Date",
                "Type"
        };

        for (String s : headers) {

            head.addView(
                    label(
                            s,
                            10,
                            TEXT,
                            true
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(40),
                            1
                    )
            );
        }

        content.addView(head);

        for (JSONObject p : payments) {

            if (!safe(
                    p.optString("boxId")
            ).equals(
                    safe(c.boxId)
            )) {
                continue;
            }

            LinearLayout row =
                    new LinearLayout(this);

            row.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            String paymentDate =
                    p.optString("date");

            String[] values = {
                    paymentDate.length() > 7
                            ? paymentDate.substring(3)
                            : paymentDate,
                    "₹" + fmt(c.packageCost),
                    "₹" + fmt(
                            p.optDouble("amount")
                    ),
                    paymentDate,
                    p.optString("type")
            };

            for (String x : values) {

                row.addView(
                        label(
                                x,
                                10,
                                Color.DKGRAY,
                                false
                        ),
                        new LinearLayout.LayoutParams(
                                0,
                                dp(44),
                                1
                        )
                );
            }

            content.addView(
                    row,
                    lp(0, 2)
            );
        }
    }

    // ============================================================
    // BILLING
    // ============================================================

    private void showPayments() {

        base(
                "Billing",
                true
        );

        content.addView(
                label(
                        "Payment History",
                        20,
                        TEXT,
                        true
                )
        );

        Button normal =
                button(
                        "Normal Collection",
                        GREEN
                );

        Button advance =
                button(
                        "Advance Payment",
                        BLUE
                );

        Button monthly =
                button(
                        "Monthly Billing",
                        Color.rgb(120, 70, 220)
                );

        content.addView(
                normal,
                lp(0, 8)
        );

        content.addView(
                advance,
                lp(0, 8)
        );

        content.addView(
                monthly,
                lp(0, 8)
        );

        normal.setOnClickListener(
                v -> showCollect(false)
        );

        advance.setOnClickListener(
                v -> showCollect(true)
        );

        monthly.setOnClickListener(
                v -> showMonthly()
        );

        TextView total =
                label(
                        "\nTotal Due: "
                                + money.format(totalDue())
                                + "\nTotal Advance: "
                                + money.format(
                                totalAdvance()
                        ),
                        18,
                        TEXT,
                        true
                );

        content.addView(total);
    }

    // ============================================================
    // REPORTS
    // ============================================================

    private void showReports() {

        base(
                "Reports",
                true
        );

        content.addView(
                label(
                        "Summary",
                        20,
                        TEXT,
                        true
                )
        );

        content.addView(
                card(
                        "₹ "
                                + fmt(totalPaid())
                                + "\nCollected",
                        16
                ),
                lp(0, 8)
        );

        content.addView(
                card(
                        "₹ "
                                + fmt(totalDue())
                                + "\nPending",
                        16
                ),
                lp(0, 8)
        );

        content.addView(
                card(
                        "₹ "
                                + fmt(totalAdvance())
                                + "\nAdvance",
                        16
                ),
                lp(0, 8)
        );

        Button day =
                button(
                        "Day Collection Report",
                        BLUE
                );

        Button export =
                button(
                        "Export Report",
                        GREEN
                );

        content.addView(
                day,
                lp(0, 8)
        );

        content.addView(
                export,
                lp(0, 8)
        );

        day.setOnClickListener(
                v -> showDayReport()
        );

        export.setOnClickListener(
                v -> createExport()
        );
    }

    private void showDayReport() {

        base(
                "Day Collection Report",
                false
        );

        content.addView(
                label(
                        "Date : "
                                + date(),
                        18,
                        TEXT,
                        true
                )
        );

        String[] heads = {
                "Date",
                "Opening",
                "Collected",
                "Closing",
                "Received"
        };

        LinearLayout hr =
                new LinearLayout(this);

        for (String x : heads) {

            hr.addView(
                    label(
                            x,
                            10,
                            TEXT,
                            true
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(42),
                            1
                    )
            );
        }

        content.addView(hr);

        double collected = 0;

        for (JSONObject p : payments) {

            if (safe(
                    p.optString("date")
            ).equals(date())) {

                collected +=
                        p.optDouble("amount");
            }
        }

        content.addView(
                row(
                        new String[]{
                                date(),
                                fmt(
                                        totalDue()
                                                + collected
                                ),
                                fmt(collected),
                                fmt(totalDue()),
                                "0"
                        }
                )
        );

        content.addView(
                label(
                        "Total Collected   ₹"
                                + fmt(collected),
                        16,
                        TEXT,
                        true
                ),
                lp(0, 12)
        );
    }

    // ============================================================
    // FILTER
    // ============================================================

    private void showFilter() {

        base(
                "Search / Filter",
                true
        );

        content.addView(
                label(
                        "Status",
                        14,
                        TEXT,
                        true
                )
        );

        String[] statuses = {
                "☑ Due",
                "☐ Deactive",
                "☐ Free",
                "☐ No Payments",
                "☑ Advance Payments"
        };

        for (String x : statuses) {

            content.addView(
                    label(
                            x,
                            15,
                            TEXT,
                            false
                    ),
                    lp(0, 7)
            );
        }

        content.addView(
                label(
                        "Payment Type",
                        14,
                        TEXT,
                        true
                ),
                lp(0, 14)
        );

        String[] types = {
                "☑ Monthly",
                "☐ Quarterly",
                "☐ Half Yearly",
                "☐ Yearly"
        };

        for (String x : types) {

            content.addView(
                    label(
                            x,
                            15,
                            TEXT,
                            false
                    ),
                    lp(0, 7)
            );
        }

        Spinner zone =
                new Spinner(this);

        zone.setAdapter(
                new ArrayAdapter<String>(
                        this,
                        android.R.layout
                                .simple_spinner_dropdown_item,
                        new String[]{
                                "Select Zone",
                                "AM PERUMAL KOVIL ST",
                                "AM MUTHUMARIYAMMAN KOVIL AP"
                        }
                )
        );

        content.addView(
                zone,
                lp(0, 12)
        );

        Button apply =
                button(
                        "APPLY",
                        GREEN
                );

        content.addView(
                apply,
                lp(0, 10)
        );

        apply.setOnClickListener(
                v -> showCustomers()
        );
    }

    // ============================================================
    // PACKAGES
    // ============================================================

    private void showPackages() {

        base(
                "Packages",
                true
        );

        String[] packages = {
                "MONTHLY 200    ₹ 200.0",
                "MONTHLY 280    ₹ 280.0",
                "MONTHLY 300    ₹ 300.0",
                "BASIC    ₹ 150.0",
                "PREMIUM    ₹ 500.0"
        };

        for (String x : packages) {

            TextView t =
                    label(
                            "●  " + x
                                    + "                 ✎  🗑",
                            15,
                            TEXT,
                            false
                    );

            t.setBackground(
                    round(
                            Color.WHITE,
                            12
                    )
            );

            content.addView(
                    t,
                    lp(0, 5)
            );
        }
    }

    // ============================================================
    // ZONES
    // ============================================================

    private void showZones() {

        base(
                "Zones",
                true
        );

        String[] zones = {
                "AM PERUMAL KOVIL ST",
                "AM MUTHUMARIYAMMAN KOVIL AP",
                "AM MAIN ROAD",
                "AM FLAT",
                "AM MUTAMIL NAGAR"
        };

        for (String x : zones) {

            TextView t =
                    label(
                            x
                                    + "                         ✎  🗑",
                            14,
                            TEXT,
                            false
                    );

            t.setBackground(
                    round(
                            Color.WHITE,
                            12
                    )
            );

            content.addView(
                    t,
                    lp(0, 5)
            );
        }
    }

    // ============================================================
    // MENU
    // ============================================================

    private void showMenu() {

        base(
                "MyCable Pro",
                false
        );

        String[] menu = {
                "⚙  Dashboard",
                "♙  Customers",
                "▣  Billing",
                "₹  Collection",
                "▤  Packages",
                "📍  Zones",
                "▥  Reports",
                "⇄  Export / Import",
                "▣  Monthly Billing",
                "⚙  Settings",
                "♙  Profile",
                "↪  Logout"
        };

        for (String x : menu) {

            TextView t =
                    label(
                            x,
                            16,
                            TEXT,
                            false
                    );

            t.setPadding(
                    dp(10),
                    dp(12),
                    0,
                    dp(12)
            );

            content.addView(t);

            if (x.contains("Dashboard")) {

                t.setOnClickListener(
                        v -> showDashboard()
                );

            } else if (x.contains("Customers")) {

                t.setOnClickListener(
                        v -> showCustomers()
                );

            } else if (x.equals("▣  Billing")) {

                t.setOnClickListener(
                        v -> showPayments()
                );

            } else if (x.contains("Collection")) {

                t.setOnClickListener(
                        v -> showCollect(false)
                );

            } else if (x.contains("Packages")) {

                t.setOnClickListener(
                        v -> showPackages()
                );

            } else if (x.contains("Zones")) {

                t.setOnClickListener(
                        v -> showZones()
                );

            } else if (x.contains("Reports")) {

                t.setOnClickListener(
                        v -> showReports()
                );

            } else if (x.contains("Export")) {

                t.setOnClickListener(
                        v -> showExportImport()
                );

            } else if (x.contains("Monthly Billing")) {

                t.setOnClickListener(
                        v -> showMonthly()
                );

            } else if (x.contains("Logout")) {

                t.setOnClickListener(
                        v -> showLogin()
                );
            }
        }
    }

    // ============================================================
    // EXPORT / IMPORT
    // ============================================================

    private void showExportImport() {

        base(
                "Export / Import",
                true
        );

        content.addView(
                card(
                        "📗  Export Data\n"
                                + "Export all customers and payments as an Excel file.",
                        16
                ),
                lp(0, 8)
        );

        Button export =
                button(
                        "EXPORT",
                        BLUE
                );

        content.addView(
                export,
                lp(0, 8)
        );

        content.addView(
                card(
                        "📕  Import Data\n"
                                + "Import from a previously exported backup file.",
                        16
                ),
                lp(0, 14)
        );

        Button importButton =
                button(
                        "IMPORT",
                        Color.rgb(120, 70, 220)
                );

        content.addView(
                importButton,
                lp(0, 8)
        );

        content.addView(
                card(
                        "💾  Backup / Restore\n"
                                + "Create or restore a complete app backup.",
                        16
                ),
                lp(0, 14)
        );

        Button backup =
                button(
                        "CREATE BACKUP",
                        BLUE
                );

        Button restore =
                button(
                        "RESTORE BACKUP",
                        Color.DKGRAY
                );

        content.addView(
                backup,
                lp(0, 8)
        );

        content.addView(
                restore,
                lp(0, 8)
        );

        export.setOnClickListener(
                v -> createExport()
        );

        importButton.setOnClickListener(
                v -> pickExcel()
        );

        backup.setOnClickListener(
                v -> createBackup()
        );

        restore.setOnClickListener(
                v -> restoreBackup()
        );
    }

    // ============================================================
    // MONTHLY BILLING
    // ============================================================

    private void showMonthly() {

        base(
                "Monthly Billing",
                true
        );

        content.addView(
                label(
                        "Generate monthly bills for all active customers.",
                        14,
                        TEXT,
                        false
                )
        );

        EditText month =
                field(
                        "Month (e.g. October 2026)"
                );

        month.setText(
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.US
                ).format(new Date())
        );

        content.addView(
                month,
                lp(0, 10)
        );

        content.addView(
                card(
                        "▣  Package Billing\n"
                                + "Automatically create bills based on customer package amount.",
                        15
                ),
                lp(0, 10)
        );

        Button generate =
                button(
                        "GENERATE BILLS",
                        GREEN
                );

        content.addView(
                generate,
                lp(0, 10)
        );

        generate.setOnClickListener(v -> {

            String monthName =
                    month.getText()
                            .toString()
                            .trim();

            if (monthName.isEmpty()) {
                toast("Enter month");
                return;
            }

            generateBills(monthName);

            toast(
                    "Monthly bills generated"
            );

            showDashboard();
        });
    }

    private void generateBills(
            String month
    ) {

        if (billedMonths.contains(month)) {
            toast(
                    month
                            + " bills already generated"
            );
            return;
        }

        for (Customer c : customers) {

            if (c == null) continue;

            if ("Inactive".equalsIgnoreCase(
                    safe(c.status)
            )) {
                continue;
            }

            double bill =
                    Math.max(
                            0,
                            c.packageCost
                    );

            if (c.advanceAmount > 0) {

                double used =
                        Math.min(
                                c.advanceAmount,
                                bill
                        );

                c.advanceAmount -= used;
                bill -= used;
            }

            c.dueAmount += bill;
        }

        billedMonths.add(month);

        save();
    }

    // ============================================================
    // EXCEL
    // ============================================================

    private void pickExcel() {

        Intent i =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        startActivityForResult(
                i,
                PICK_EXCEL
        );
    }

    private void createExport() {

        Intent i =
                new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        i.putExtra(
                Intent.EXTRA_TITLE,
                "MyCablePro_Export.xlsx"
        );

        startActivityForResult(
                i,
                CREATE_EXCEL
        );
    }

    private void createBackup() {

        Intent i =
                new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType("application/json");

        i.putExtra(
                Intent.EXTRA_TITLE,
                "MyCablePro_Backup.json"
        );

        startActivityForResult(
                i,
                CREATE_BACKUP
        );
    }

    private void restoreBackup() {

        Intent i =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        i.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        i.setType("application/json");

        startActivityForResult(
                i,
                RESTORE_BACKUP
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (resultCode != RESULT_OK ||
                data == null ||
                data.getData() == null) {
            return;
        }

        try {

            Uri uri = data.getData();

            if (requestCode == PICK_EXCEL) {

                ExcelImporter.Result r =
                        ExcelImporter.read(
                                this,
                                uri
                        );

                if (r != null &&
                        r.customers != null) {

                    for (Customer c :
                            r.customers) {

                        if (c != null) {
                            upsert(c);
                        }
                    }

                    save();

                    toast(
                            "Imported "
                                    + r.customers.size()
                                    + " customers"
                    );

                    showDashboard();
                }

            } else if (
                    requestCode == CREATE_EXCEL
            ) {

                writeExcel(uri);

            } else if (
                    requestCode == CREATE_BACKUP
            ) {

                writeBackup(uri);

            } else if (
                    requestCode == RESTORE_BACKUP
            ) {

                readBackup(uri);
            }

        } catch (Exception e) {

            e.printStackTrace();

            toast(
                    "Error: "
                            + safe(e.getMessage())
            );
        }
    }

    private void upsert(Customer c) {

        if (c == null) return;

        String newBox =
                safe(c.boxId);

        if (newBox.isEmpty()) {
            customers.add(c);
            return;
        }

        for (int i = 0;
             i < customers.size();
             i++) {

            Customer existing =
                    customers.get(i);

            if (existing == null) {
                continue;
            }

            if (newBox.equals(
                    safe(existing.boxId)
            )) {

                customers.set(i, c);
                return;
            }
        }

        customers.add(c);
    }

    private void writeExcel(Uri uri)
            throws Exception {

        OutputStream out =
                getContentResolver()
                        .openOutputStream(uri);

        if (out == null) {
            throw new IOException(
                    "Unable to open file"
            );
        }

        try (
                OutputStream output = out;
                Workbook workbook =
                        new XSSFWorkbook()
        ) {

            Sheet sheet =
                    workbook.createSheet(
                            "Customers"
                    );

            String[] headers = {
                    "BOX ID",
                    "CUSTOMER NAME",
                    "PHONE NO",
                    "ADDRESS",
                    "ZONE",
                    "PACKAGE NAME",
                    "PACKAGE COST",
                    "DUE AMOUNT",
                    "ADVANCE AMOUNT",
                    "STATUS"
            };

            Row header =
                    sheet.createRow(0);

            for (int i = 0;
                 i < headers.length;
                 i++) {

                header.createCell(i)
                        .setCellValue(
                                headers[i]
                        );
            }

            int rowNumber = 1;

            for (Customer c :
                    customers) {

                if (c == null) continue;

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                row.createCell(0)
                        .setCellValue(
                                safe(c.boxId)
                        );

                row.createCell(1)
                        .setCellValue(
                                safe(c.name)
                        );

                row.createCell(2)
                        .setCellValue(
                                safe(c.phone)
                        );

                row.createCell(3)
                        .setCellValue(
                                safe(c.address)
                        );

                row.createCell(4)
                        .setCellValue(
                                safe(c.zone)
                        );

                row.createCell(5)
                        .setCellValue(
                                safe(c.packageName)
                        );

                row.createCell(6)
                        .setCellValue(
                                c.packageCost
                        );

                row.createCell(7)
                        .setCellValue(
                                c.dueAmount
                        );

                row.createCell(8)
                        .setCellValue(
                                c.advanceAmount
                        );

                row.createCell(9)
                        .setCellValue(
                                safe(c.status)
                        );
            }

            workbook.write(output);
        }

        toast("Excel exported");
    }

    // ============================================================
    // BACKUP
    // ============================================================

    private void writeBackup(Uri uri)
            throws Exception {

        JSONObject backup =
                new JSONObject();

        JSONArray customerArray =
                new JSONArray();

        for (Customer c :
                customers) {

            if (c != null) {
                customerArray.put(
                        c.toJson()
                );
            }
        }

        JSONArray paymentArray =
                new JSONArray();

        for (JSONObject p :
                payments) {

            if (p != null) {
                paymentArray.put(p);
            }
        }

        backup.put(
                "customers",
                customerArray
        );

        backup.put(
                "payments",
                paymentArray
        );

        backup.put(
                "billedMonths",
                new JSONArray(
                        billedMonths
                )
        );

        OutputStream out =
                getContentResolver()
                        .openOutputStream(uri);

        if (out == null) {
            throw new IOException(
                    "Unable to open backup file"
            );
        }

        try (OutputStream output = out) {

            output.write(
                    backup.toString(2)
                            .getBytes("UTF-8")
            );
        }

        toast("Backup created");
    }

    private void readBackup(Uri uri)
            throws Exception {

        InputStream input =
                getContentResolver()
                        .openInputStream(uri);

        if (input == null) {
            throw new IOException(
                    "Unable to open backup"
            );
        }

        StringBuilder text =
                new StringBuilder();

        try (
                InputStream in = input;
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        in
                                )
                        )
        ) {

            String line;

            while (
                    (line =
                            reader.readLine())
                            != null
            ) {

                text.append(line);
            }
        }

        JSONObject backup =
                new JSONObject(
                        text.toString()
                );

        customers.clear();

        JSONArray customerArray =
                backup.optJSONArray(
                        "customers"
                );

        if (customerArray != null) {

            for (
                    int i = 0;
                    i < customerArray.length();
                    i++
            ) {

                JSONObject object =
                        customerArray
                                .optJSONObject(i);

                if (object != null) {

                    customers.add(
                            Customer.fromJson(
                                    object
                            )
                    );
                }
            }
        }

        payments.clear();

        JSONArray paymentArray =
                backup.optJSONArray(
                        "payments"
                );

        if (paymentArray != null) {

            for (
                    int i = 0;
                    i < paymentArray.length();
                    i++
            ) {

                JSONObject object =
                        paymentArray
                                .optJSONObject(i);

                if (object != null) {
                    payments.add(object);
                }
            }
        }

        billedMonths.clear();

        JSONArray months =
                backup.optJSONArray(
                        "billedMonths"
                );

        if (months != null) {

            for (
                    int i = 0;
                    i < months.length();
                    i++
            ) {

                billedMonths.add(
                        months.optString(i)
                );
            }
        }

        save();

        showDashboard();

        toast("Backup restored");
    }

    // ============================================================
    // LOCAL STORAGE
    // ============================================================

    private void load() {

        customers.clear();
        payments.clear();
        billedMonths.clear();

        String customerData =
                getPreferences(
                        MODE_PRIVATE
                ).getString(
                        "customers",
                        "[]"
                );

        try {

            JSONArray array =
                    new JSONArray(
                            customerData
                    );

            for (
                    int i = 0;
                    i < array.length();
                    i++
            ) {

                JSONObject object =
                        array.optJSONObject(i);

                if (object != null) {

                    Customer c =
                            Customer.fromJson(
                                    object
                            );

                    if (c != null) {
                        customers.add(c);
                    }
                }
            }

        } catch (Exception ignored) {
        }

        String paymentData =
                getPreferences(
                        MODE_PRIVATE
                ).getString(
                        "payments",
                        "[]"
                );

        try {

            JSONArray array =
                    new JSONArray(
                            paymentData
                    );

            for (
                    int i = 0;
                    i < array.length();
                    i++
            ) {

                JSONObject object =
                        array.optJSONObject(i);

                if (object != null) {
                    payments.add(object);
                }
            }

        } catch (Exception ignored) {
        }

        String billedData =
                getPreferences(
                        MODE_PRIVATE
                ).getString(
                        "billed",
                        "[]"
                );

        try {

            JSONArray array =
                    new JSONArray(
                            billedData
                    );

            for (
                    int i = 0;
                    i < array.length();
                    i++
            ) {

                billedMonths.add(
                        array.optString(i)
                );
            }

        } catch (Exception ignored) {
        }
    }

    private void save() {

        try {

            JSONArray customersJson =
                    new JSONArray();

            for (Customer c :
                    customers) {

                if (c != null) {
                    customersJson.put(
                            c.toJson()
                    );
                }
            }

            JSONArray paymentsJson =
                    new JSONArray();

            for (JSONObject p :
                    payments) {

                if (p != null) {
                    paymentsJson.put(p);
                }
            }

            getPreferences(
                    MODE_PRIVATE
            )
                    .edit()
                    .putString(
                            "customers",
                            customersJson.toString()
                    )
                    .putString(
                            "payments",
                            paymentsJson.toString()
                    )
                    .putString(
                            "billed",
                            new JSONArray(
                                    billedMonths
                            ).toString()
                    )
                    .apply();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ============================================================
    // TOTALS
    // ============================================================

    private double totalDue() {

        double total = 0;

        for (Customer c :
                customers) {

            if (c != null) {

                total += Math.max(
                        0,
                        c.dueAmount
                );
            }
        }

        return total;
    }

    private double totalAdvance() {

        double total = 0;

        for (Customer c :
                customers) {

            if (c != null) {

                total += Math.max(
                        0,
                        c.advanceAmount
                );
            }
        }

        return total;
    }

    private double totalPaid() {

        double total = 0;

        for (JSONObject p :
                payments) {

            if (p != null) {

                total += Math.max(
                        0,
                        p.optDouble(
                                "amount",
                                0
                        )
                );
            }
        }

        return total;
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private TextView info(
            String key,
            String value
    ) {

        TextView t =
                label(
                        key
                                + "\n"
                                + safe(value),
                        13,
                        TEXT,
                        false
                );

        t.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        10
                )
        );

        return t;
    }

    private TextView balance(
            String key,
            double value
    ) {

        TextView t =
                label(
                        key
                                + "\n₹"
                                + fmt(value),
                        11,
                        TEXT,
                        true
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        10
                )
        );

        return t;
    }

    private LinearLayout row(
            String[] values
    ) {

        LinearLayout r =
                new LinearLayout(this);

        r.setOrientation(
                LinearLayout.HORIZONTAL
        );

        for (String x : values) {

            r.addView(
                    label(
                            safe(x),
                            10,
                            TEXT,
                            false
                    ),
                    new LinearLayout.LayoutParams(
                            0,
                            dp(42),
                            1
                    )
            );
        }

        return r;
    }

    private EditText field(
            String hint
    ) {

        EditText e =
                new EditText(this);

        e.setHint(hint);
        e.setTextSize(14);
        e.setSingleLine(true);

        e.setPadding(
                dp(14),
                0,
                dp(14),
                0
        );

        e.setTextColor(TEXT);
        e.setHintTextColor(
                Color.rgb(
                        120,
                        130,
                        140
                )
        );

        e.setBackground(
                round(
                        Color.WHITE,
                        10
                )
        );

        return e;
    }

    private Button button(
            String text,
            int color
    ) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setAllCaps(false);

        b.setMinHeight(0);
        b.setMinimumHeight(0);

        b.setBackground(
                round(
                        color,
                        12
                )
        );

        return b;
    }

    private Button smallButton(
            String text
    ) {

        Button b =
                button(
                        text,
                        Color.WHITE
                );

        b.setTextColor(TEXT);

        return b;
    }

    private TextView chip(
            String text
    ) {

        TextView t =
                label(
                        text,
                        12,
                        TEXT,
                        true
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        18
                )
        );

        return t;
    }

    private TextView card(
            String text,
            int size
    ) {

        return card(
                text,
                size,
                TEXT
        );
    }

    private TextView card(
            String text,
            int size,
            int color
    ) {

        TextView t =
                label(
                        text,
                        size,
                        color,
                        true
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setPadding(
                dp(8),
                dp(6),
                dp(8),
                dp(6)
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        14
                )
        );

        return t;
    }

    private TextView label(
            String text,
            int size,
            int color,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(
                safe(text)
        );

        t.setTextSize(size);
        t.setTextColor(color);

        t.setTypeface(
                null,
                bold ? 1 : 0
        );

        t.setGravity(
                Gravity.CENTER_VERTICAL
        );

        return t;
    }

    // ============================================================
    // DRAWABLES / LAYOUT
    // ============================================================

    private GradientDrawable round(
            int color,
            int radius
    ) {

        GradientDrawable g =
                new GradientDrawable();

        g.setColor(color);

        g.setCornerRadius(
                dp(radius)
        );

        g.setStroke(
                dp(1),
                Color.argb(
                        30,
                        0,
                        0,
                        0
                )
        );

        return g;
    }

    private GradientDrawable gradient(
            int start,
            int end
    ) {

        return new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        start,
                        end
                }
        );
    }

    private LinearLayout.LayoutParams lp(
            int top,
            int bottom
    ) {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(52)
                );

        p.setMargins(
                0,
                dp(top),
                0,
                dp(bottom)
        );

        return p;
    }

    private LinearLayout.LayoutParams lpw(
            int margin
    ) {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(52),
                        1
                );

        p.setMargins(
                dp(margin),
                0,
                0,
                0
        );

        return p;
    }

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
                        + 0.5f
        );
    }

    // ============================================================
    // UTILITIES
    // ============================================================

    private String safe(String value) {

        return value == null
                ? ""
                : value;
    }

    private String date() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.US
        ).format(
                new Date()
        );
    }

    private String fmt(double value) {

        return String.format(
                Locale.US,
                "%.0f",
                value
        );
    }

    private double num(
            EditText editText
    ) {

        if (editText == null) {
            return 0;
        }

        try {

            String value =
                    editText
                            .getText()
                            .toString()
                            .trim()
                            .replace(",", "")
                            .replace("₹", "");

            if (value.isEmpty()) {
                return 0;
            }

            return Double.parseDouble(value);

        } catch (Exception e) {

            return 0;
        }
    }

    private void toast(String message) {

        Toast.makeText(
                this,
                safe(message),
                Toast.LENGTH_SHORT
        ).show();
    }
}

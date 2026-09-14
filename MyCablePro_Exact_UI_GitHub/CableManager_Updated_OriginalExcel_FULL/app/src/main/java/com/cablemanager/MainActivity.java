package com.cablemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private LinearLayout bottom;
    private Customer selected;

    private final int GREEN = Color.rgb(0, 184, 124);
    private final int DARK_GREEN = Color.rgb(0, 150, 110);
    private final int BLUE = Color.rgb(25, 105, 220);
    private final int RED = Color.rgb(220, 70, 70);
    private final int BG = Color.rgb(246, 248, 250);
    private final int TEXT = Color.rgb(30, 42, 55);

    private final NumberFormat money =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(10, 30, 48));
        getWindow().setNavigationBarColor(Color.WHITE);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        load();
        showLogin();
    }

    // ============================================================
    // COMMON SCREEN
    // ============================================================

    private void base(String title, boolean showNavigation) {

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        setContentView(root);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(8), dp(5), dp(8), dp(5));
        toolbar.setBackgroundColor(DARK_GREEN);

        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextColor(Color.WHITE);
        back.setTextSize(38);
        back.setGravity(Gravity.CENTER);

        if (title.equals("MyCable Pro")) {
            back.setVisibility(View.GONE);
        } else {
            back.setVisibility(View.VISIBLE);
            back.setOnClickListener(v -> showDashboard());
        }

        toolbar.addView(
                back,
                new LinearLayout.LayoutParams(dp(45), dp(56))
        );

        TextView heading = new TextView(this);
        heading.setText(title);
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(21);
        heading.setTypeface(null, 1);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        toolbar.addView(
                heading,
                new LinearLayout.LayoutParams(0, dp(56), 1)
        );

        TextView menu = new TextView(this);
        menu.setText("⋮");
        menu.setTextColor(Color.WHITE);
        menu.setTextSize(30);
        menu.setGravity(Gravity.CENTER);
        menu.setOnClickListener(v -> showMenu());

        toolbar.addView(
                menu,
                new LinearLayout.LayoutParams(dp(45), dp(56))
        );

        root.addView(toolbar);

        if (showNavigation) {
            content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(8)
            );

            root.addView(
                    content,
                    new LinearLayout.LayoutParams(
                            -1,
                            0,
                            1
                    )
            );

            buildBottom();
        } else {
            ScrollView scroll = new ScrollView(this);
            scroll.setFillViewport(true);

            content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(
                    dp(12),
                    dp(12),
                    dp(12),
                    dp(20)
            );

            scroll.addView(content);

            root.addView(
                    scroll,
                    new LinearLayout.LayoutParams(
                            -1,
                            0,
                            1
                    )
            );
        }
    }

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

        for (String name : names) {

            TextView item = new TextView(this);
            item.setText(name);
            item.setTextSize(11);
            item.setTextColor(Color.DKGRAY);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, dp(5), 0, dp(5));

            item.setOnClickListener(v -> {

                String text = ((TextView) v).getText().toString();

                if (text.contains("Home")) {
                    showDashboard();
                } else if (text.contains("Customers")) {
                    showCustomers();
                } else if (text.contains("Billing")) {
                    showPayments();
                } else {
                    showReports();
                }
            });

            bottom.addView(
                    item,
                    new LinearLayout.LayoutParams(
                            0,
                            dp(62),
                            1
                    )
            );
        }

        root.addView(bottom);
    }

    // ============================================================
    // LOGIN
    // ============================================================

    private void showLogin() {

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER);
        page.setPadding(
                dp(25),
                dp(20),
                dp(25),
                dp(20)
        );

        page.setBackground(
                gradient(
                        Color.rgb(0, 160, 145),
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
                        dp(85)
                )
        );

        EditText username = field("Username");
        page.addView(
                username,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(54)
                )
        );

        EditText password = field("Password");
        password.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PASSWORD
        );

        page.addView(password, lp(10));

        Button login = button("LOGIN", GREEN);

        page.addView(login, lp(14));

        TextView hint = label(
                "Username: admin\nPassword: 123456",
                12,
                Color.WHITE,
                false
        );

        hint.setGravity(Gravity.CENTER);

        page.addView(hint, lp(10));

        login.setOnClickListener(v -> {

            String u = username.getText().toString().trim();
            String p = password.getText().toString();

            if (u.equals("admin") && p.equals("123456")) {
                showDashboard();
            } else {
                toast("Username: admin  |  Password: 123456");
            }
        });

        setContentView(page);
    }

    // ============================================================
    // DASHBOARD
    // ============================================================

    private void showDashboard() {

        base("MyCable Pro", true);

        TextView welcome = label(
                "Vanakkam,\nAdmin",
                17,
                TEXT,
                true
        );

        content.addView(welcome, lp(0));

        double due = totalDue();
        double advance = totalAdvance();

        int active = 0;

        for (Customer c : customers) {
            if (!"Inactive".equalsIgnoreCase(c.status)) {
                active++;
            }
        }

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);

        addStatRow(
                stats,
                new String[]{
                        "👥\nTotal Customers\n" + customers.size(),
                        "✓\nActive\n" + active
                },
                new int[]{
                        Color.rgb(215, 235, 255),
                        Color.rgb(218, 250, 226)
                }
        );

        addStatRow(
                stats,
                new String[]{
                        "₹\nTotal Due\n" + money.format(due),
                        "₹\nAdvance\n" + money.format(advance)
                },
                new int[]{
                        Color.rgb(255, 225, 225),
                        Color.rgb(255, 239, 205)
                }
        );

        content.addView(stats, lp(8));

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        String[][] buttons = {
                {"👥\nCustomers", "▣\nBilling", "₹\nCollection"},
                {"▤\nPackages", "📍\nZones", "▥\nReports"}
        };

        for (String[] row : buttons) {

            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (String text : row) {

                TextView card = card(text, 15);

                rowLayout.addView(
                        card,
                        new LinearLayout.LayoutParams(
                                0,
                                dp(88),
                                1
                        )
                );

                if (text.contains("Customers")) {
                    card.setOnClickListener(v -> showCustomers());
                } else if (text.contains("Billing")) {
                    card.setOnClickListener(v -> showPayments());
                } else if (text.contains("Collection")) {
                    card.setOnClickListener(v -> showCollect(false));
                } else if (text.contains("Packages")) {
                    card.setOnClickListener(v -> showPackages());
                } else if (text.contains("Zones")) {
                    card.setOnClickListener(v -> showZones());
                } else {
                    card.setOnClickListener(v -> showReports());
                }
            }

            grid.addView(rowLayout);
        }

        content.addView(grid, lp(10));

        LinearLayout excelRow = new LinearLayout(this);
        excelRow.setOrientation(LinearLayout.HORIZONTAL);

        Button importButton = button("Import Excel", BLUE);
        Button exportButton = button("Export Excel", GREEN);

        excelRow.addView(
                importButton,
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                )
        );

        LinearLayout.LayoutParams exportParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(54),
                        1
                );

        exportParams.setMargins(dp(8), 0, 0, 0);

        excelRow.addView(exportButton, exportParams);

        content.addView(excelRow, lp(10));

        importButton.setOnClickListener(v -> pickExcel());
        exportButton.setOnClickListener(v -> createExport());
    }

    private void addStatRow(
            LinearLayout parent,
            String[] values,
            int[] colors
    ) {

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < 2; i++) {

            TextView t = card(values[i], 14);

            t.setBackground(round(colors[i], 14));

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(82),
                            1
                    );

            if (i == 1) {
                p.setMargins(dp(6), 0, 0, 0);
            }

            row.addView(t, p);
        }

        parent.addView(row, lp(5));
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

        Button filterButton = button("FILTER", BLUE);

        LinearLayout.LayoutParams filterParams =
                new LinearLayout.LayoutParams(
                        dp(95),
                        dp(50)
                );

        filterParams.setMargins(dp(6), 0, 0, 0);

        searchRow.addView(filterButton, filterParams);

        content.addView(searchRow);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);

        String[] chipNames = {
                "All",
                "Due",
                "Advance",
                "Inactive"
        };

        for (String name : chipNames) {

            TextView chip = chip(name);

            LinearLayout.LayoutParams p =
                    new LinearLayout.LayoutParams(
                            0,
                            dp(42),
                            1
                    );

            p.setMargins(dp(2), dp(8), dp(2), dp(8));

            chips.addView(chip, p);

            chip.setOnClickListener(
                    v -> renderCustomerList(
                            search,
                            ((TextView) v).getText().toString()
                    )
            );
        }

        content.addView(chips);

        ListView list = new ListView(this);

        list.setDivider(null);
        list.setVerticalScrollBarEnabled(true);
        list.setBackgroundColor(BG);

        content.addView(
                list,
                new LinearLayout.LayoutParams(
                        -1,
                        0,
                        1
                )
        );

        renderCustomerList(search, "All");

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
                        renderCustomerList(
                                search,
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

        filterButton.setOnClickListener(
                v -> showFilter()
        );

        list.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Object item =
                            parent.getItemAtPosition(position);

                    if (item instanceof Customer) {

                        selected = (Customer) item;

                        showDetails(selected);
                    }
                }
        );
    }

    private void renderCustomerList(
            EditText search,
            String mode
    ) {

        if (content == null) {
            return;
        }

        String query =
                search == null
                        ? ""
                        : search.getText()
                        .toString()
                        .trim()
                        .toLowerCase(Locale.US);

        ArrayList<Customer> filtered =
                new ArrayList<>();

        for (Customer c : customers) {

            boolean matchesSearch =
                    query.isEmpty()
                            ||
                            (
                                    c.name + " "
                                            + c.phone + " "
                                            + c.boxId + " "
                                            + c.cardNo + " "
                                            + c.address + " "
                                            + c.zone + " "
                                            + c.searchCode
                            )
                                    .toLowerCase(Locale.US)
                                    .contains(query);

            boolean matchesMode = true;

            if (mode.equals("Due")) {
                matchesMode = c.dueAmount > 0;
            } else if (mode.equals("Advance")) {
                matchesMode = c.advanceAmount > 0;
            } else if (mode.equals("Inactive")) {
                matchesMode =
                        "Inactive".equalsIgnoreCase(c.status);
            }

            if (matchesSearch && matchesMode) {
                filtered.add(c);
            }
        }

        ListView list = findCustomerList();

        if (list == null) {
            return;
        }

        CustomerAdapter adapter =
                new CustomerAdapter(
                        this
                );

        adapter.setAll(filtered);

        list.setAdapter(adapter);

        if (filtered.isEmpty()) {

            toast(
                    customers.isEmpty()
                            ? "No customers imported"
                            : "No matching customers"
            );
        }
    }

    private ListView findCustomerList() {

        if (content == null) {
            return null;
        }

        for (int i = 0; i < content.getChildCount(); i++) {

            View v = content.getChildAt(i);

            if (v instanceof ListView) {
                return (ListView) v;
            }
        }

        return null;
    }

    // ============================================================
    // CUSTOMER DETAILS
    // ============================================================

    private void showDetails(Customer c) {

        if (c == null) {
            showCustomers();
            return;
        }

        base("Customer Details", true);

        TextView heading = card(
                "👤  " +
                        (c.name.isEmpty()
                                ? "Unnamed Customer"
                                : c.name)
                        + "\n📦 Box ID: " + c.boxId
                        + "\n📞 " +
                        (c.phone.isEmpty()
                                ? "-"
                                : c.phone),
                16
        );

        heading.setGravity(Gravity.CENTER_VERTICAL);

        content.addView(
                heading,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(110)
                )
        );

        content.addView(
                info("Zone", emptyDash(c.zone)),
                lp(8)
        );

        content.addView(
                info(
                        "Package",
                        emptyDash(c.packageName)
                                + "   •   ₹"
                                + fmt(c.packageCost)
                ),
                lp(6)
        );

        content.addView(
                info("Status", emptyDash(c.status)),
                lp(6)
        );

        content.addView(
                info(
                        "Address",
                        emptyDash(c.address)
                ),
                lp(6)
        );

        content.addView(
                info(
                        "Card / Smart Card",
                        emptyDash(c.cardNo)
                ),
                lp(6)
        );

        content.addView(
                info(
                        "MSO",
                        emptyDash(c.mso)
                ),
                lp(6)
        );

        LinearLayout balances =
                new LinearLayout(this);

        balances.setOrientation(
                LinearLayout.HORIZONTAL
        );

        balances.addView(
                balance("Bill", c.packageCost),
                weightParams()
        );

        balances.addView(
                balance(
                        "Due",
                        c.dueAmount
                ),
                weightParams()
        );

        balances.addView(
                balance(
                        "Advance",
                        c.advanceAmount
                ),
                weightParams()
        );

        content.addView(
                balances,
                lp(10)
        );

        Button collect =
                button(
                        "COLLECT PAYMENT",
                        GREEN
                );

        Button history =
                button(
                        "PAYMENT HISTORY",
                        BLUE
                );

        Button edit =
                button(
                        "EDIT CUSTOMER",
                        Color.rgb(105, 80, 210)
                );

        content.addView(collect, lp(8));
        content.addView(history, lp(8));
        content.addView(edit, lp(8));

        collect.setOnClickListener(
                v -> showCollect(false)
        );

        history.setOnClickListener(
                v -> showPaymentHistory(c)
        );

        edit.setOnClickListener(
                v -> showAdd(c)
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
                field("Mobile Number");

        EditText box =
                field("Box ID *");

        EditText card =
                field("Smart Card No");

        EditText mso =
                field("MSO");

        EditText zone =
                field("Zone");

        EditText pkg =
                field("Package Name");

        EditText amount =
                field("Monthly Amount");

        EditText address =
                field("Address");

        EditText due =
                field("Current Due");

        EditText advance =
                field("Advance");

        EditText agent =
                field("Agent Name");

        EditText agentPhone =
                field("Agent Phone");

        EditText subscription =
                field("Subscription");

        EditText[] fields = {
                name,
                phone,
                box,
                card,
                mso,
                zone,
                pkg,
                amount,
                address,
                due,
                advance,
                agent,
                agentPhone,
                subscription
        };

        for (EditText e : fields) {
            content.addView(e, lp(6));
        }

        RadioGroup statusGroup =
                new RadioGroup(this);

        RadioButton active =
                new RadioButton(this);

        active.setText("Active");

        RadioButton inactive =
                new RadioButton(this);

        inactive.setText("Inactive");

        statusGroup.addView(active);
        statusGroup.addView(inactive);

        content.addView(
                label(
                        "Status",
                        14,
                        TEXT,
                        true
                ),
                lp(8)
        );

        content.addView(statusGroup);

        if (edit != null) {

            name.setText(edit.name);
            phone.setText(edit.phone);
            box.setText(edit.boxId);
            card.setText(edit.cardNo);
            mso.setText(edit.mso);
            zone.setText(edit.zone);
            pkg.setText(edit.packageName);
            amount.setText(String.valueOf(edit.packageCost));
            address.setText(edit.address);
            due.setText(String.valueOf(edit.dueAmount));
            advance.setText(String.valueOf(edit.advanceAmount));
            agent.setText(edit.agentName);
            agentPhone.setText(edit.agentPhone);
            subscription.setText(edit.subscription);

            if ("Inactive".equalsIgnoreCase(edit.status)) {
                inactive.setChecked(true);
            } else {
                active.setChecked(true);
            }

        } else {
            active.setChecked(true);
        }

        Button saveButton =
                button(
                        "SAVE CUSTOMER",
                        GREEN
                );

        content.addView(
                saveButton,
                lp(12)
        );

        saveButton.setOnClickListener(v -> {

            String boxId =
                    box.getText()
                            .toString()
                            .trim();

            if (boxId.isEmpty()) {
                toast("BOX ID is required");
                return;
            }

            Customer c =
                    edit == null
                            ? new Customer()
                            : edit;

            c.name =
                    name.getText()
                            .toString()
                            .trim();

            c.phone =
                    phone.getText()
                            .toString()
                            .trim();

            c.boxId = boxId;

            c.cardNo =
                    card.getText()
                            .toString()
                            .trim();

            c.mso =
                    mso.getText()
                            .toString()
                            .trim();

            c.zone =
                    zone.getText()
                            .toString()
                            .trim();

            c.packageName =
                    pkg.getText()
                            .toString()
                            .trim();

            c.packageCost =
                    number(amount);

            c.address =
                    address.getText()
                            .toString()
                            .trim();

            c.dueAmount =
                    number(due);

            c.advanceAmount =
                    number(advance);

            c.agentName =
                    agent.getText()
                            .toString()
                            .trim();

            c.agentPhone =
                    agentPhone.getText()
                            .toString()
                            .trim();

            c.subscription =
                    subscription.getText()
                            .toString()
                            .trim();

            c.status =
                    inactive.isChecked()
                            ? "Inactive"
                            : "Active";

            if (edit == null) {

                boolean duplicate = false;

                for (Customer old : customers) {

                    if (old.boxId.equalsIgnoreCase(c.boxId)) {
                        duplicate = true;
                        break;
                    }
                }

                if (duplicate) {
                    toast("BOX ID already exists");
                    return;
                }

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

    private void showCollect(boolean advanceOnly) {

        if (selected == null) {

            if (!customers.isEmpty()) {
                selected = customers.get(0);
            } else {
                showAdd(null);
                return;
            }
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
                        c.name + "\nBox: " + c.boxId
                ),
                lp(0)
        );

        content.addView(
                info(
                        "Current Due",
                        money.format(c.dueAmount)
                ),
                lp(8)
        );

        content.addView(
                info(
                        "Advance Balance",
                        money.format(c.advanceAmount)
                ),
                lp(6)
        );

        RadioGroup type =
                new RadioGroup(this);

        RadioButton normal =
                new RadioButton(this);

        normal.setText("Normal Payment");

        RadioButton advance =
                new RadioButton(this);

        advance.setText("Advance Payment");

        type.addView(normal);
        type.addView(advance);

        normal.setChecked(!advanceOnly);
        advance.setChecked(advanceOnly);

        content.addView(
                label(
                        "Payment Type",
                        14,
                        TEXT,
                        true
                ),
                lp(8)
        );

        content.addView(type);

        EditText amount =
                field("Amount Paid");

        content.addView(amount, lp(8));

        Spinner paymentMode =
                new Spinner(this);

        String[] modes = {
                "Cash",
                "UPI",
                "Cheque",
                "Other"
        };

        paymentMode.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        modes
                )
        );

        content.addView(
                paymentMode,
                lp(8)
        );

        EditText notes =
                field("Notes");

        content.addView(notes, lp(8));

        Button savePayment =
                button(
                        "SAVE PAYMENT",
                        GREEN
                );

        content.addView(
                savePayment,
                lp(10)
        );

        savePayment.setOnClickListener(v -> {

            double value =
                    number(amount);

            if (value <= 0) {
                toast("Enter payment amount");
                return;
            }

            boolean isAdvance =
                    advance.isChecked();

            if (isAdvance) {

                c.advanceAmount += value;

            } else {

                double used =
                        Math.min(
                                value,
                                c.dueAmount
                        );

                c.dueAmount -= used;

                double extra =
                        value - used;

                if (extra > 0) {
                    c.advanceAmount += extra;
                }
            }

            JSONObject payment =
                    new JSONObject();

            try {

                payment.put(
                        "boxId",
                        c.boxId
                );

                payment.put(
                        "name",
                        c.name
                );

                payment.put(
                        "amount",
                        value
                );

                payment.put(
                        "type",
                        isAdvance
                                ? "Advance"
                                : "Normal"
                );

                payment.put(
                        "mode",
                        paymentMode
                                .getSelectedItem()
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

    private void showPaymentHistory(Customer c) {

        base(
                "Payment History",
                true
        );

        content.addView(
                info(
                        "Customer",
                        c.name + "\nBox: " + c.boxId
                ),
                lp(0)
        );

        boolean found = false;

        for (JSONObject p : payments) {

            if (!p.optString("boxId")
                    .equals(c.boxId)) {
                continue;
            }

            found = true;

            String text =
                    "₹" +
                            fmt(
                                    p.optDouble(
                                            "amount"
                                    )
                            )
                            + "   •   "
                            + p.optString("type")
                            + "\n"
                            + p.optString("mode")
                            + "   •   "
                            + p.optString("date");

            if (!p.optString("notes").isEmpty()) {
                text +=
                        "\n" +
                                p.optString(
                                        "notes"
                                );
            }

            content.addView(
                    card(text, 14),
                    lp(7)
            );
        }

        if (!found) {

            content.addView(
                    label(
                            "No payment history",
                            15,
                            Color.GRAY,
                            false
                    ),
                    lp(20)
            );
        }
    }

    // ============================================================
    // BILLING
    // ============================================================

    private void showPayments() {

        base("Billing", true);

        content.addView(
                card(
                        "💰 Billing & Collection\n\n" +
                                "Total Due: " +
                                money.format(totalDue()) +
                                "\n\nTotal Advance: " +
                                money.format(totalAdvance()),
                        17
                ),
                lp(0)
        );

        Button normal =
                button(
                        "NORMAL COLLECTION",
                        GREEN
                );

        Button advance =
                button(
                        "ADVANCE PAYMENT",
                        BLUE
                );

        content.addView(normal, lp(12));
        content.addView(advance, lp(8));

        normal.setOnClickListener(
                v -> showCollect(false)
        );

        advance.setOnClickListener(
                v -> showCollect(true)
        );

        Button monthly =
                button(
                        "MONTHLY BILLING",
                        Color.rgb(110, 80, 200)
                );

        content.addView(monthly, lp(8));

        monthly.setOnClickListener(
                v -> showMonthly()
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
                info(
                        "Billing",
                        "Generate monthly bills for all active customers."
                ),
                lp(0)
        );

        EditText month =
                field("Month");

        month.setText(
                new SimpleDateFormat(
                        "MMMM yyyy",
                        Locale.US
                ).format(
                        new java.util.Date()
                )
        );

        content.addView(month, lp(10));

        Button generate =
                button(
                        "GENERATE BILLS",
                        GREEN
                );

        content.addView(
                generate,
                lp(10)
        );

        generate.setOnClickListener(v -> {

            String m =
                    month.getText()
                            .toString()
                            .trim();

            if (m.isEmpty()) {
                toast("Enter month");
                return;
            }

            generateBills(m);

            toast("Bills generated");

            showDashboard();
        });
    }

    private void generateBills(String month) {

        if (billedMonths.contains(month)) {
            toast("This month is already billed");
            return;
        }

        for (Customer c : customers) {

            if ("Inactive".equalsIgnoreCase(c.status)) {
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
    // PACKAGES
    // ============================================================

    private void showPackages() {

        base(
                "Packages",
                true
        );

        String[] packages = {
                "MONTHLY 200   ₹200",
                "MONTHLY 280   ₹280",
                "MONTHLY 300   ₹300",
                "BASIC   ₹150",
                "PREMIUM   ₹500"
        };

        for (String p : packages) {

            content.addView(
                    card(
                            "●  " + p,
                            15
                    ),
                    lp(6)
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

        for (String zone : zones) {

            content.addView(
                    card(zone, 14),
                    lp(6)
            );
        }
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
                card(
                        "₹ " +
                                fmt(totalPaid()) +
                                "\nCollected",
                        18
                ),
                lp(0)
        );

        content.addView(
                card(
                        "₹ " +
                                fmt(totalDue()) +
                                "\nPending",
                        18
                ),
                lp(8)
        );

        content.addView(
                card(
                        "₹ " +
                                fmt(totalAdvance()) +
                                "\nAdvance",
                        18
                ),
                lp(8)
        );

        Button day =
                button(
                        "DAY COLLECTION REPORT",
                        BLUE
                );

        content.addView(day, lp(10));

        day.setOnClickListener(
                v -> showDayReport()
        );
    }

    private void showDayReport() {

        base(
                "Day Collection Report",
                false
        );

        double collected = 0;

        for (JSONObject p : payments) {

            if (p.optString("date")
                    .equals(date())) {

                collected +=
                        p.optDouble("amount");
            }
        }

        content.addView(
                card(
                        "Date: " + date()
                                + "\n\nCollected: ₹"
                                + fmt(collected)
                                + "\n\nTotal Payments: "
                                + payments.size(),
                        17
                ),
                lp(0)
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
                        "Customer Status",
                        15,
                        TEXT,
                        true
                ),
                lp(0)
        );

        String[] options = {
                "All Customers",
                "Due Customers",
                "Advance Customers",
                "Inactive Customers"
        };

        for (String option : options) {

            TextView t =
                    card(
                            "☐  " + option,
                            15
                    );

            content.addView(t, lp(6));
        }

        content.addView(
                label(
                        "Zone",
                        15,
                        TEXT,
                        true
                ),
                lp(15)
        );

        Spinner spinner =
                new Spinner(this);

        String[] zones = {
                "All Zones",
                "AM PERUMAL KOVIL ST",
                "AM MUTHUMARIYAMMAN KOVIL AP",
                "AM MAIN ROAD",
                "AM FLAT",
                "AM MUTAMIL NAGAR"
        };

        spinner.setAdapter(
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        zones
                )
        );

        content.addView(
                spinner,
                lp(8)
        );

        Button apply =
                button(
                        "APPLY",
                        GREEN
                );

        content.addView(
                apply,
                lp(10)
        );

        apply.setOnClickListener(
                v -> showCustomers()
        );
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
                "🏠  Dashboard",
                "👥  Customers",
                "▣  Billing",
                "₹  Collection",
                "▤  Packages",
                "📍  Zones",
                "▥  Reports",
                "⇄  Export / Import",
                "⚙  Settings",
                "↪  Logout"
        };

        for (String item : menu) {

            TextView t =
                    card(item, 16);

            t.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            content.addView(t, lp(5));

            if (item.contains("Dashboard")) {
                t.setOnClickListener(
                        v -> showDashboard()
                );
            } else if (item.contains("Customers")) {
                t.setOnClickListener(
                        v -> showCustomers()
                );
            } else if (item.contains("Billing")) {
                t.setOnClickListener(
                        v -> showPayments()
                );
            } else if (item.contains("Collection")) {
                t.setOnClickListener(
                        v -> showCollect(false)
                );
            } else if (item.contains("Packages")) {
                t.setOnClickListener(
                        v -> showPackages()
                );
            } else if (item.contains("Zones")) {
                t.setOnClickListener(
                        v -> showZones()
                );
            } else if (item.contains("Reports")) {
                t.setOnClickListener(
                        v -> showReports()
                );
            } else if (item.contains("Export")) {
                t.setOnClickListener(
                        v -> showExportImport()
                );
            } else if (item.contains("Logout")) {
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
                        "📗 Export Data\n\n" +
                                "Export all customers to Excel.",
                        16
                ),
                lp(0)
        );

        Button export =
                button(
                        "EXPORT EXCEL",
                        BLUE
                );

        content.addView(export, lp(8));

        content.addView(
                card(
                        "📕 Import Data\n\n" +
                                "Import customers from Excel.",
                        16
                ),
                lp(14)
        );

        Button importButton =
                button(
                        "IMPORT EXCEL",
                        GREEN
                );

        content.addView(
                importButton,
                lp(8)
        );

        content.addView(
                card(
                        "💾 Backup / Restore",
                        16
                ),
                lp(14)
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

        content.addView(backup, lp(8));
        content.addView(restore, lp(8));

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
    // EXCEL
    // ============================================================

    private void pickExcel() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        startActivityForResult(
                intent,
                PICK_EXCEL
        );
    }

    private void createExport() {

        Intent intent =
                new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );

        intent.putExtra(
                Intent.EXTRA_TITLE,
                "MyCablePro_Export.xlsx"
        );

        startActivityForResult(
                intent,
                CREATE_EXCEL
        );
    }

    private void createBackup() {

        Intent intent =
                new Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("application/json");

        intent.putExtra(
                Intent.EXTRA_TITLE,
                "MyCablePro_Backup.json"
        );

        startActivityForResult(
                intent,
                CREATE_BACKUP
        );
    }

    private void restoreBackup() {

        Intent intent =
                new Intent(
                        Intent.ACTION_OPEN_DOCUMENT
                );

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("application/json");

        startActivityForResult(
                intent,
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

        if (resultCode != Activity.RESULT_OK
                || data == null
                || data.getData() == null) {
            return;
        }

        try {

            Uri uri = data.getData();

            if (requestCode == PICK_EXCEL) {

                ExcelImporter.Result result =
                        ExcelImporter.read(
                                this,
                                uri
                        );

                int imported = 0;

                for (Customer c :
                        result.customers) {

                    upsert(c);
                    imported++;
                }

                save();

                if (imported > 0) {

                    toast(
                            "Imported "
                                    + imported
                                    + " customers"
                    );

                    showCustomers();

                } else {

                    toast(
                            "No customers found in Excel"
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

            toast(
                    "Import/Export error: "
                            + e.getMessage()
            );
        }
    }

    private void upsert(Customer incoming) {

        for (int i = 0;
             i < customers.size();
             i++) {

            Customer old =
                    customers.get(i);

            if (old.boxId.equalsIgnoreCase(
                    incoming.boxId
            )) {

                customers.set(i, incoming);
                return;
            }
        }

        customers.add(incoming);
    }

    private void writeExcel(Uri uri)
            throws Exception {

        OutputStream out =
                getContentResolver()
                        .openOutputStream(uri);

        if (out == null) {
            throw new IOException(
                    "Cannot open file"
            );
        }

        try (OutputStream stream = out;
             Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Customers"
                    );

            String[] headers = {
                    "BOX ID",
                    "CARD NO",
                    "MSO",
                    "CUSTOMER NAME",
                    "PHONE NO",
                    "ADDRESS",
                    "ZONE",
                    "SEARCH CODE",
                    "PACKAGE NAME",
                    "PACKAGE COST",
                    "AGENT NAME",
                    "AGENT PHONE",
                    "DUE AMOUNT",
                    "ADVANCE AMOUNT",
                    "STATUS",
                    "SUBSCRIPTION"
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

                Row row =
                        sheet.createRow(
                                rowNumber++
                        );

                row.createCell(0)
                        .setCellValue(c.boxId);

                row.createCell(1)
                        .setCellValue(c.cardNo);

                row.createCell(2)
                        .setCellValue(c.mso);

                row.createCell(3)
                        .setCellValue(c.name);

                row.createCell(4)
                        .setCellValue(c.phone);

                row.createCell(5)
                        .setCellValue(c.address);

                row.createCell(6)
                        .setCellValue(c.zone);

                row.createCell(7)
                        .setCellValue(c.searchCode);

                row.createCell(8)
                        .setCellValue(c.packageName);

                row.createCell(9)
                        .setCellValue(c.packageCost);

                row.createCell(10)
                        .setCellValue(c.agentName);

                row.createCell(11)
                        .setCellValue(c.agentPhone);

                row.createCell(12)
                        .setCellValue(c.dueAmount);

                row.createCell(13)
                        .setCellValue(c.advanceAmount);

                row.createCell(14)
                        .setCellValue(c.status);

                row.createCell(15)
                        .setCellValue(c.subscription);
            }

            workbook.write(stream);
        }

        toast("Excel exported successfully");
    }

    // ============================================================
    // BACKUP
    // ============================================================

    private void writeBackup(Uri uri)
            throws Exception {

        JSONObject rootObject =
                new JSONObject();

        JSONArray customerArray =
                new JSONArray();

        for (Customer c :
                customers) {

            customerArray.put(
                    c.toJson()
            );
        }

        JSONArray paymentArray =
                new JSONArray();

        for (JSONObject p :
                payments) {

            paymentArray.put(p);
        }

        JSONArray months =
                new JSONArray(
                        billedMonths
                );

        rootObject.put(
                "customers",
                customerArray
        );

        rootObject.put(
                "payments",
                paymentArray
        );

        rootObject.put(
                "billedMonths",
                months
        );

        OutputStream out =
                getContentResolver()
                        .openOutputStream(uri);

        if (out == null) {
            throw new IOException(
                    "Cannot open backup"
            );
        }

        try (OutputStream stream = out) {

            stream.write(
                    rootObject
                            .toString(2)
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
                    "Cannot open backup"
            );
        }

        StringBuilder builder =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        input
                                )
                        )
        ) {

            String line;

            while (
                    (line =
                            reader.readLine())
                            != null
            ) {

                builder.append(line);
            }
        }

        JSONObject rootObject =
                new JSONObject(
                        builder.toString()
                );

        customers.clear();
        payments.clear();
        billedMonths.clear();

        JSONArray customerArray =
                rootObject.optJSONArray(
                        "customers"
                );

        if (customerArray != null) {

            for (int i = 0;
                 i < customerArray.length();
                 i++) {

                customers.add(
                        Customer.fromJson(
                                customerArray
                                        .getJSONObject(i)
                        )
                );
            }
        }

        JSONArray paymentArray =
                rootObject.optJSONArray(
                        "payments"
                );

        if (paymentArray != null) {

            for (int i = 0;
                 i < paymentArray.length();
                 i++) {

                payments.add(
                        paymentArray
                                .getJSONObject(i)
                );
            }
        }

        JSONArray monthArray =
                rootObject.optJSONArray(
                        "billedMonths"
                );

        if (monthArray != null) {

            for (int i = 0;
                 i < monthArray.length();
                 i++) {

                billedMonths.add(
                        monthArray.getString(i)
                );
            }
        }

        save();

        toast("Backup restored");

        showDashboard();
    }

    // ============================================================
    // STORAGE
    // ============================================================

    private void load() {

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

            for (int i = 0;
                 i < array.length();
                 i++) {

                customers.add(
                        Customer.fromJson(
                                array.getJSONObject(i)
                        )
                );
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

            for (int i = 0;
                 i < array.length();
                 i++) {

                payments.add(
                        array.getJSONObject(i)
                );
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

            for (int i = 0;
                 i < array.length();
                 i++) {

                billedMonths.add(
                        array.getString(i)
                );
            }

        } catch (Exception ignored) {
        }
    }

    private void save() {

        try {

            JSONArray customersArray =
                    new JSONArray();

            for (Customer c :
                    customers) {

                customersArray.put(
                        c.toJson()
                );
            }

            JSONArray paymentsArray =
                    new JSONArray();

            for (JSONObject p :
                    payments) {

                paymentsArray.put(p);
            }

            JSONArray months =
                    new JSONArray(
                            billedMonths
                    );

            getPreferences(
                    MODE_PRIVATE
            )
                    .edit()
                    .putString(
                            "customers",
                            customersArray
                                    .toString()
                    )
                    .putString(
                            "payments",
                            paymentsArray
                                    .toString()
                    )
                    .putString(
                            "billed",
                            months.toString()
                    )
                    .apply();

        } catch (Exception ignored) {
        }
    }

    // ============================================================
    // TOTALS
    // ============================================================

    private double totalDue() {

        double total = 0;

        for (Customer c :
                customers) {

            total +=
                    Math.max(
                            0,
                            c.dueAmount
                    );
        }

        return total;
    }

    private double totalAdvance() {

        double total = 0;

        for (Customer c :
                customers) {

            total +=
                    Math.max(
                            0,
                            c.advanceAmount
                    );
        }

        return total;
    }

    private double totalPaid() {

        double total = 0;

        for (JSONObject p :
                payments) {

            total +=
                    p.optDouble(
                            "amount",
                            0
                    );
        }

        return total;
    }

    // ============================================================
    // UI HELPERS
    // ============================================================

    private TextView info(
            String title,
            String value
    ) {

        TextView t =
                label(
                        title
                                + "\n"
                                + value,
                        14,
                        TEXT,
                        false
                );

        t.setPadding(
                dp(14),
                dp(8),
                dp(14),
                dp(8)
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        12
                )
        );

        return t;
    }

    private TextView balance(
            String title,
            double value
    ) {

        TextView t =
                label(
                        title
                                + "\n₹"
                                + fmt(value),
                        12,
                        TEXT,
                        true
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setPadding(
                dp(5),
                dp(5),
                dp(5),
                dp(5)
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        10
                )
        );

        return t;
    }

    private LinearLayout.LayoutParams
    weightParams() {

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        0,
                        dp(75),
                        1
                );

        p.setMargins(
                dp(3),
                0,
                dp(3),
                0
        );

        return p;
    }

    private TextView card(
            String text,
            int size
    ) {

        TextView t =
                label(
                        text,
                        size,
                        TEXT,
                        true
                );

        t.setGravity(
                Gravity.CENTER
        );

        t.setPadding(
                dp(8),
                dp(8),
                dp(8),
                dp(8)
        );

        t.setBackground(
                round(
                        Color.WHITE,
                        14
                )
        );

        return t;
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
                        20
                )
        );

        return t;
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
                Color.rgb(120, 125, 130)
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

        b.setBackground(
                round(
                        color,
                        12
                )
        );

        return b;
    }

    private TextView label(
            String text,
            int size,
            int color,
            boolean bold
    ) {

        TextView t =
                new TextView(this);

        t.setText(text);
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
            int first,
            int second
    ) {

        return new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{
                        first,
                        second
                }
        );
    }

    private LinearLayout.LayoutParams lp(
            int top
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
                0
        );

        return p;
    }

    private int dp(int value) {

        return (int)
                (
                        value *
                                getResources()
                                        .getDisplayMetrics()
                                        .density
                                + 0.5f
                );
    }

    private double number(
            EditText editText
    ) {

        try {

            String s =
                    editText.getText()
                            .toString()
                            .replace(",", "")
                            .replace("₹", "")
                            .trim();

            if (s.isEmpty()) {
                return 0;
            }

            return Double.parseDouble(s);

        } catch (Exception e) {

            return 0;
        }
    }

    private String fmt(
            double value
    ) {

        return String.format(
                Locale.US,
                "%.0f",
                value
        );
    }

    private String date() {

        return new SimpleDateFormat(
                "dd-MM-yyyy",
                Locale.US
        ).format(
                new java.util.Date()
        );
    }

    private String emptyDash(
            String value
    ) {

        return value == null
                || value.trim().isEmpty()
                ? "-"
                : value;
    }

    private void toast(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}

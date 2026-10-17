package com.example.appvegetables.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.Cart.Cart_activity;
import com.example.appvegetables.Order.Order_Activity;
import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;
import com.example.appvegetables.home.Home;
import com.example.appvegetables.login.login_register_activity;

public class Account extends AppCompatActivity {
    private LinearLayout btn_Thong_Tin, btn_Doi_Mat_Khau, btn_Dang_Xuat;
    private TextView txt_User_Name;
    private DatabaseHelper dbHelper;

    // MENU VARIABLES - THÊM VÀO
    private LinearLayout item_Home, item_Cart, item_Order, item_Account;
    private TextView text_Home, text_Cart, text_Order, text_Account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadUserData();
        setupEvents();

        setupBottomMenu(); // THÊM DÒNG NÀY
    }

    private void initViews() {
        btn_Thong_Tin = findViewById(R.id.btnThongTin);
        btn_Doi_Mat_Khau = findViewById(R.id.btnDoiMatKhau);
        btn_Dang_Xuat = findViewById(R.id.btnDangXuat);
        txt_User_Name = findViewById(R.id.txt_User_Name);

        // INIT MENU VIEWS - THÊM VÀO
        initMenuViews();
    }

    // ==================== MENU METHODS - THÊM VÀO ====================
    private void initMenuViews() {
        item_Home = findViewById(R.id.item_Home_customer);
        item_Cart = findViewById(R.id.item_cart_customer);
        item_Order = findViewById(R.id.item_order_customer);
        item_Account = findViewById(R.id.item_account_customer);

        text_Home = findViewById(R.id.text_Home_customer);
        text_Cart = findViewById(R.id.text_cart_customer);
        text_Order = findViewById(R.id.text_order_customer);
        text_Account = findViewById(R.id.text_account_customer);
    }

    private void setupBottomMenu() {
        handleIntentMenu();
        setupMenuClickListeners();
    }

    private void handleIntentMenu() {
        String selectedMenu = getIntent().getStringExtra("SELECTED_MENU");
        if ("CART".equals(selectedMenu)) {
            selectMenu(item_Cart);
        } else if ("ACCOUNT".equals(selectedMenu)) {
            selectMenu(item_Account);
        } else if ("ORDER".equals(selectedMenu)) {
            selectMenu(item_Order);
        } else {
            selectMenu(item_Home);
        }
    }

    private void setupMenuClickListeners() {
        item_Home.setOnClickListener(v -> goTo(Home.class, "HOME"));
        item_Cart.setOnClickListener(v -> goTo(Cart_activity.class, "CART"));
        item_Order.setOnClickListener(v -> goTo(Order_Activity.class, "ORDER"));
        item_Account.setOnClickListener(v -> goTo(Account.class, "ACCOUNT"));
    }

    private void goTo(Class<?> activity, String menu) {
        selectMenu(getMenuItemByName(menu));
        Intent intent = new Intent(this, activity);
        intent.putExtra("SELECTED_MENU", menu);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private LinearLayout getMenuItemByName(String name) {
        switch (name) {
            case "CART": return item_Cart;
            case "ORDER": return item_Order;
            case "ACCOUNT": return item_Account;
            default: return item_Home;
        }
    }

    private void selectMenu(LinearLayout selectedItem) {
        resetMenu();
        selectedItem.setBackgroundResource(R.drawable.bg_menu_customer_selected);
        showSelectedText(selectedItem);
    }

    private void resetMenu() {
        int[] items = {R.id.item_Home_customer, R.id.item_cart_customer, R.id.item_order_customer, R.id.item_account_customer};
        for (int id : items) {
            findViewById(id).setBackgroundResource(R.drawable.bg_menu_customer);
        }

        text_Home.setVisibility(View.GONE);
        text_Cart.setVisibility(View.GONE);
        text_Order.setVisibility(View.GONE);
        text_Account.setVisibility(View.GONE);
    }

    private void showSelectedText(LinearLayout selectedItem) {
        if (selectedItem == item_Home) text_Home.setVisibility(View.VISIBLE);
        if (selectedItem == item_Cart) text_Cart.setVisibility(View.VISIBLE);
        if (selectedItem == item_Order) text_Order.setVisibility(View.VISIBLE);
        if (selectedItem == item_Account) text_Account.setVisibility(View.VISIBLE);
    }

    // CÁC PHƯƠNG THỨC CŨ GIỮ NGUYÊN...
    private void loadUserData() {
        int currentUserId = getCurrentUserId();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER,
                new String[]{DatabaseHelper.COLUMN_USERNAME},
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(currentUserId)},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            txt_User_Name.setText(username);
            cursor.close();
        } else {
            txt_User_Name.setText("Khách");
        }
        db.close();
    }

    private void setupEvents() {
        btn_Thong_Tin.setOnClickListener(v -> {
            Intent intent = new Intent(Account.this, account_thong_tin_ca_nhan.class);
            startActivity(intent);
        });
        btn_Doi_Mat_Khau.setOnClickListener(v -> {
            Intent intent = new Intent(Account.this, account_doi_mat_khau.class);
            startActivity(intent);
        });

        btn_Dang_Xuat.setOnClickListener(v -> {
            logout();
        });
    }

    private int getCurrentUserId() {
        SharedPreferences preferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        return preferences.getInt("USER_ID", 1);
    }

    private void logout() {
        SharedPreferences preferences = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(Account.this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Account.this, login_register_activity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }
}
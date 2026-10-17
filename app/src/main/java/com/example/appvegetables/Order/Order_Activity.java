package com.example.appvegetables.Order;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appvegetables.Cart.Cart_activity;
import com.example.appvegetables.R;
import com.example.appvegetables.account.Account;
import com.example.appvegetables.database.OrderDatabaseActivity;
import com.example.appvegetables.home.Home;

import java.util.ArrayList;

public class Order_Activity extends AppCompatActivity {

    private TextView txt_don_dat, txt_don_huy;
    private LinearLayout item_Home, item_Cart, item_Order, item_Account;
    private TextView text_Home, text_Cart, text_Order, text_Account;
    private RecyclerView recyclerDonHang;
    private OrderAdapter orderAdapter;
    private OrderDatabaseActivity orderDatabase;
    private ArrayList<Order_model> orderList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);

        // Ánh xạ
        txt_don_dat = findViewById(R.id.txt_don_dat);
        txt_don_huy = findViewById(R.id.txt_don_huy);
        recyclerDonHang = findViewById(R.id.recycler_don_hang);

        // Thiết lập RecyclerView
        recyclerDonHang.setLayoutManager(new LinearLayoutManager(this));
        orderDatabase = new OrderDatabaseActivity(this);

        // Mặc định hiển thị "Đơn đặt"
        selectTab(txt_don_dat, txt_don_huy);
        loadOrdersByStatus("Đơn đặt");

        // Sự kiện click từng tab
        txt_don_dat.setOnClickListener(v -> {
            selectTab(txt_don_dat, txt_don_huy);
            loadOrdersByStatus("Đơn đặt");
        });

        txt_don_huy.setOnClickListener(v -> {
            selectTab(txt_don_huy, txt_don_dat);
            loadOrdersByStatus("Đã hủy");
        });

        // Thiết lập menu dưới
        setupBottomMenu();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ORDER_DETAIL && resultCode == RESULT_OK) {
            // Reload danh sách đúng tab đang xem
            if (txt_don_dat.getCurrentTextColor() == Color.parseColor("#1E88E5")) {
                loadOrdersByStatus("Đơn đặt");
            } else {
                loadOrdersByStatus("Đã hủy");
            }
        }
    }


    // Gạch chân tab được chọn
    private void selectTab(TextView selected, TextView other) {
        selected.setPaintFlags(selected.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        selected.setTextColor(Color.parseColor("#1E88E5"));

        other.setPaintFlags(other.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        other.setTextColor(Color.BLACK);
    }

    // Lấy danh sách đơn hàng theo trạng thái
    private void loadOrdersByStatus(String status) {

        orderList = orderDatabase.getOrdersByStatus(status);
        orderAdapter = new OrderAdapter(this, orderList);
        recyclerDonHang.setAdapter(orderAdapter);
    }
    private static final int REQUEST_ORDER_DETAIL = 1001;

    // ---------------- MENU DƯỚI ----------------
    private void setupBottomMenu() {
        // Ánh xạ menu
        item_Home = findViewById(R.id.item_Home_customer);
        item_Cart = findViewById(R.id.item_cart_customer);
        item_Order = findViewById(R.id.item_order_customer);
        item_Account = findViewById(R.id.item_account_customer);

        text_Home = findViewById(R.id.text_Home_customer);
        text_Cart = findViewById(R.id.text_cart_customer);
        text_Order = findViewById(R.id.text_order_customer);
        text_Account = findViewById(R.id.text_account_customer);

        // Sự kiện click
        item_Home.setOnClickListener(v -> {
            selectMenu(item_Home);
            startActivity(new Intent(this, Home.class));
            overridePendingTransition(0, 0);
        });

        item_Cart.setOnClickListener(v -> {
            selectMenu(item_Cart);
            startActivity(new Intent(this, Cart_activity.class));
            overridePendingTransition(0, 0);
        });

        item_Order.setOnClickListener(v -> {
            selectMenu(item_Order);
        });
        item_Account.setOnClickListener(v -> {
            selectMenu(item_Account);
            startActivity(new Intent(this, Account.class));
            overridePendingTransition(0, 0);
        });

        selectMenu(item_Order); // Mặc định đang ở Order
    }

    private void selectMenu(LinearLayout selectedItem) {
        resetMenu();

        selectedItem.setBackgroundResource(R.drawable.bg_menu_customer_selected);

        if (selectedItem == item_Home) text_Home.setVisibility(View.VISIBLE);
        if (selectedItem == item_Cart) text_Cart.setVisibility(View.VISIBLE);
        if (selectedItem == item_Order) text_Order.setVisibility(View.VISIBLE);
        if (selectedItem == item_Account) text_Account.setVisibility(View.VISIBLE);
    }

    private void resetMenu() {
        item_Home.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Cart.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Order.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Account.setBackgroundResource(R.drawable.bg_menu_customer);

        text_Home.setVisibility(View.GONE);
        text_Cart.setVisibility(View.GONE);
        text_Order.setVisibility(View.GONE);
        text_Account.setVisibility(View.GONE);
    }
}

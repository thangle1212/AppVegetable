package com.example.appvegetables.Cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appvegetables.Order.Order_Activity;
import com.example.appvegetables.Order.Order_don_hang;
import com.example.appvegetables.R;
import com.example.appvegetables.account.Account;
import com.example.appvegetables.database.Cart_database_activity;
import com.example.appvegetables.home.Home;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

public class Cart_activity extends AppCompatActivity implements Cart_adapter.OnCartChangeListener {
    private RecyclerView recyclerView;
    private TextView txt_tong_tien;
    private Cart_adapter cart_adapter;
    private Button btn_don_hang;

    // MENU
    private LinearLayout item_Home, item_Cart, item_Order, item_Account;
    private TextView text_Home, text_Cart, text_Order, text_Account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cart);

        setupViews();
        setupBottomMenu();
        setupCart();
        setupOrderButton();
        syncCartManagerWithDatabase();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.recycler_cart);
        txt_tong_tien = findViewById(R.id.txt_tong_tien);
        btn_don_hang = findViewById(R.id.btn_don_hang);
    }

    private void setupCart() {
        Cart_database_activity Cda = new Cart_database_activity(this);
        ArrayList<ItemCart> cartItems = Cda.getAllCartItems();
        cart_adapter = new Cart_adapter(this, cartItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cart_adapter);

        updateTotalPrice();
        updateOrderButtonState();
    }

    private void setupOrderButton() {
        btn_don_hang.setOnClickListener(v -> {
            Cart_database_activity Cda = new Cart_database_activity(this);
            ArrayList<ItemCart> dbCartItems = Cda.getAllCartItems();

            if (dbCartItems == null || dbCartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng trống! Vui lòng thêm sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                syncCartManagerWithDatabase();

                // LẤY TỔNG TIỀN TỪ GIỎ HÀNG
                double totalPrice = cart_adapter.getTotalPrice();

                Intent intent = new Intent(Cart_activity.this, Order_don_hang.class);

                // TRUYỀN TỔNG TIỀN QUA INTENT
                intent.putExtra("TOTAL_PRICE", totalPrice);

                startActivity(intent);
            }
        });
    }

    private void syncCartManagerWithDatabase() {
        Cart_database_activity Cda = new Cart_database_activity(this);
        ArrayList<ItemCart> cartItems = Cda.getAllCartItems();

        CartManager.getInstance().getCart().clear();
        if (cartItems != null && !cartItems.isEmpty()) {
            CartManager.getInstance().getCart().addAll(cartItems);
        }
    }

    private void updateOrderButtonState() {
        boolean hasItems = !CartManager.getInstance().getCart().isEmpty();
        btn_don_hang.setEnabled(hasItems);
        btn_don_hang.setAlpha(hasItems ? 1.0f : 0.5f);
    }

    private void updateTotalPrice() {
        if (cart_adapter == null || txt_tong_tien == null) return;

        double total = cart_adapter.getTotalPrice();
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

        txt_tong_tien.setText(decimalFormat.format(total));
    }

    @Override
    public void onCartUpdated() {
        updateTotalPrice();
        updateOrderButtonState();
        syncCartManagerWithDatabase();
    }

    @Override
    public void onItemRemoved(int position) {
        syncCartManagerWithDatabase();
        updateTotalPrice();
        updateOrderButtonState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Lấy lại danh sách từ SQLite mỗi khi quay lại
        Cart_database_activity cartDB = new Cart_database_activity(this);
        ArrayList<ItemCart> updatedCart = cartDB.getAllCartItems();

        // Cập nhật lại adapter
        if (cart_adapter != null) {
            cart_adapter.refreshData(updatedCart);
        }

        // Đồng bộ với CartManager (để các Activity khác cũng thấy dữ liệu mới)
        syncCartManagerWithDatabase();

        // Cập nhật lại tổng tiền và trạng thái nút
        updateTotalPrice();
        updateOrderButtonState();
    }


    // ===================== MENU =====================
    private void setupBottomMenu() {
        initMenuViews();
        handleIntentMenu();
        setupMenuClickListeners();
    }

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
}
package com.example.appvegetables.Order;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.Cart.CartManager;
import com.example.appvegetables.Cart.ItemCart;
import com.example.appvegetables.R;
import com.example.appvegetables.database.Cart_database_activity;
import com.example.appvegetables.database.OrderDatabaseActivity;
import com.example.appvegetables.home.Home;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Order_don_hang extends AppCompatActivity {

    private TextView txt_product, txt_time, txt_tong_tien_thanh_toan;
    private EditText edt_full_name, edt_phone, edt_address;
    private RadioGroup radio_pay;
    private Button btn_cancel, btn_confirm;

    private OrderDatabaseActivity orderDb;
    private Cart_database_activity cartDb;

    @Override
    protected void onResume() {
        super.onResume();
        loadCartData(); // luôn tải lại từ database
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chi_tiet_don_hang);

        initViews();
        loadCartData();
        setupEventListeners();

        orderDb = new OrderDatabaseActivity(this);
        cartDb = new Cart_database_activity(this);
    }

    private void initViews() {
        txt_product = findViewById(R.id.txt_product);
        txt_tong_tien_thanh_toan = findViewById(R.id.txt_tong_tien_thanh_toan);
        txt_time = findViewById(R.id.txt_time);
        edt_full_name = findViewById(R.id.edt_full_name);
        edt_phone = findViewById(R.id.edt_phone);
        edt_address = findViewById(R.id.edt_address);
        radio_pay = findViewById(R.id.radioPaymentMethod);
        btn_confirm = findViewById(R.id.btn_confirm);
        btn_cancel = findViewById(R.id.btn_cancel);
    }

    @SuppressLint("SetTextI18n")
    private void loadCartData() {
        // LẤY TỔNG TIỀN TỪ INTENT
        double totalPrice = getIntent().getDoubleExtra("TOTAL_PRICE", 0);

        // LẤY DỮ LIỆU MỚI NHẤT TỪ DATABASE
        Cart_database_activity Cda = new Cart_database_activity(this);
        ArrayList<ItemCart> cartItems = Cda.getAllCartItems();

        if (cartItems == null || cartItems.isEmpty()) {
            txt_product.setText("Giỏ hàng trống!");
            txt_tong_tien_thanh_toan.setText("0 VND");
            return;
        }

        // ĐỊNH DẠNG DẤU PHẨY PHÂN CÁCH NGHÌN
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

        StringBuilder productsText = new StringBuilder();

        for (ItemCart item : cartItems) {
            // Làm sạch chuỗi giá -> chuyển sang số
            double price = 0;
            try {
                String cleanPrice = item.getGia().replaceAll("[^0-9]", "");
                if (!cleanPrice.isEmpty()) {
                    price = Double.parseDouble(cleanPrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Tổng tiền của sản phẩm = giá × số lượng
            double itemTotal = price * item.getSo_luong_grid_view();

            // Định dạng lại tiền
            String formattedPrice = formatter.format(price) + "đ";
            String formattedTotal = formatter.format(itemTotal) + "đ";

            // Thêm vào danh sách hiển thị
            productsText.append("• ")
                    .append(item.getName())
                    .append(" - SL: ").append(item.getSo_luong_grid_view())
                    .append(" - Giá: ").append(formattedPrice)
                    .append(" - Thành tiền: ").append(formattedTotal)
                    .append("\n");
        }

        // Gán vào TextView
        txt_product.setText(productsText.toString());

        // Tổng tiền toàn đơn
        txt_tong_tien_thanh_toan.setText(formatter.format(totalPrice) + " VND");

        // Hiển thị thời gian hiện tại
        String currentTime = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
                .format(new Date());
        txt_time.setText("Thời gian: " + currentTime);
    }




    // TÍNH TỔNG TIỀN CHO TỪNG SẢN PHẨM
    private double calculateItemTotal(ItemCart item) {
        try {
            String cleanPrice = item.getGia().replaceAll("[^0-9]", "");
            if (!cleanPrice.isEmpty()) {
                return Double.parseDouble(cleanPrice) * item.getSo_luong_grid_view();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setupEventListeners() {
        btn_confirm.setOnClickListener(v -> confirmOrder());
        btn_cancel.setOnClickListener(v -> finish());
    }

    private void confirmOrder() {
        if (validateForm()) {
            saveOrderToDatabase();
        }
    }

    private boolean validateForm() {
        String fullName = edt_full_name.getText().toString().trim();
        String phone = edt_phone.getText().toString().trim();
        String address = edt_address.getText().toString().trim();

        if (fullName.isEmpty()) {
            edt_full_name.setError("Vui lòng nhập họ tên");
            return false;
        }

        if (phone.isEmpty()) {
            edt_phone.setError("Vui lòng nhập số điện thoại");
            return false;
        }

        if (address.isEmpty()) {
            edt_address.setError("Vui lòng nhập địa chỉ");
            return false;
        }

        return true;
    }

    private void saveOrderToDatabase() {
        // Lấy thông tin từ form
        String customerName = edt_full_name.getText().toString().trim();
        String phone = edt_phone.getText().toString().trim();
        String address = edt_address.getText().toString().trim();

        // PHƯƠNG THỨC THANH TOÁN CỐ ĐỊNH
        String paymentMethod = "Thanh toán khi nhận hàng";

        // Lấy dữ liệu từ database
        Cart_database_activity Cda = new Cart_database_activity(this);
        ArrayList<ItemCart> cartItems = Cda.getAllCartItems();
        double totalPrice = calculateTotalPrice(cartItems);

        // Lưu đơn hàng
        long orderId = orderDb.saveOrder(customerName, phone, address, "",
                paymentMethod, totalPrice, cartItems);

        if (orderId != -1) {
            clearCart();
            showSuccessDialog(orderId);
        } else {
            Toast.makeText(this, "Lỗi khi lưu đơn hàng", Toast.LENGTH_SHORT).show();
        }
    }

    // TÍNH TỔNG TIỀN TỪ DANH SÁCH SẢN PHẨM
    private double calculateTotalPrice(ArrayList<ItemCart> cartItems) {
        double total = 0;
        for (ItemCart item : cartItems) {
            total += calculateItemTotal(item);
        }
        return total;
    }

    private void clearCart() {
        try {
            // Xóa trong CartManager
            CartManager.getInstance().clearCart();

            // Xóa trong database
            cartDb.clearCart();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi xóa giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSuccessDialog(long orderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đặt hàng thành công!")
                .setMessage("Mã đơn hàng: #" + orderId + "\nCảm ơn bạn đã đặt hàng!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Quay về màn hình chính
                    Intent intent = new Intent(this, Home.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
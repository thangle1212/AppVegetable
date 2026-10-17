package com.example.appvegetables.Order;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import java.text.DecimalFormat;

public class Order_don_dat extends AppCompatActivity {

    private TextView txtCustomerInfo, txtPaymentMethod, txtProductList, txtTotalPrice, txtOrderId, txtOrderTime;
    private Button btnCancel;
    private com.example.appvegetables.database.OrderDatabaseActivity orderDb;
    private long orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chi_tiet_don_dat);

        // Ánh xạ view
        txtCustomerInfo = findViewById(R.id.txt_customer_info);
        txtPaymentMethod = findViewById(R.id.txt_payment_method);
        txtProductList = findViewById(R.id.txt_product_list);
        txtTotalPrice = findViewById(R.id.txt_total_price);
        txtOrderId = findViewById(R.id.txt_order_id);
        txtOrderTime = findViewById(R.id.txt_order_time);
        btnCancel = findViewById(R.id.btn_cancel);

        // Khởi tạo database
        orderDb = new com.example.appvegetables.database.OrderDatabaseActivity(this);

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        this.orderId = intent.getLongExtra("ORDER_ID", -1);
        Log.d("ORDER_CLICK", "Order ID nhận được: " + orderId);


        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load chi tiết đơn hàng
        loadOrderDetail(orderId);

        // Nút hủy đơn
        btnCancel.setOnClickListener(v -> confirmCancelOrder());
    }

    // ==================== HIỂN THỊ CHI TIẾT ĐƠN ====================
    private void loadOrderDetail(long orderId) {
        Cursor orderCursor = orderDb.getOrderById(orderId);
        if (orderCursor == null || !orderCursor.moveToFirst()) {
            Toast.makeText(this, "Không thể tải thông tin đơn hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        // Lấy dữ liệu chính của đơn hàng
        String customerName = orderCursor.getString(orderCursor.getColumnIndexOrThrow("customer_name"));
        String phone = orderCursor.getString(orderCursor.getColumnIndexOrThrow("phone"));
        String address = orderCursor.getString(orderCursor.getColumnIndexOrThrow("address"));
        String paymentMethod = orderCursor.getString(orderCursor.getColumnIndexOrThrow("payment_method"));
        double totalPrice = orderCursor.getDouble(orderCursor.getColumnIndexOrThrow("total_price"));
        String orderDate = orderCursor.getString(orderCursor.getColumnIndexOrThrow("order_date"));
        String status = orderCursor.getString(orderCursor.getColumnIndexOrThrow("status"));

        txtCustomerInfo.setText(customerName + "\nSĐT: " + phone + "\nĐịa chỉ: " + address);
        txtPaymentMethod.setText(paymentMethod);
        txtTotalPrice.setText(formatter.format(totalPrice) + "đ");
        txtOrderId.setText("#" + orderId);
        txtOrderTime.setText(orderDate);

        // Nếu đơn đã hủy thì ẩn nút hủy
        if ("Đã hủy".equalsIgnoreCase(status)) {
            btnCancel.setEnabled(false);
            btnCancel.setText("Đã hủy");
        }

        orderCursor.close();

        // Load sản phẩm trong đơn
        Cursor detailCursor = orderDb.getOrderItems(orderId);
        if (detailCursor != null && detailCursor.moveToFirst()) {
            StringBuilder productList = new StringBuilder();

            do {
                String productName = detailCursor.getString(detailCursor.getColumnIndexOrThrow("detail_name"));
                int quantity = detailCursor.getInt(detailCursor.getColumnIndexOrThrow("detail_quantity"));
                double price = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow("detail_price"));

                double correctedPrice = price * 1000;
                long roundedPrice = Math.round(correctedPrice);

                productList.append("• ")
                        .append(productName)
                        .append(" (x").append(quantity).append(") - ")
                        .append(formatter.format(roundedPrice)).append("đ\n");
            } while (detailCursor.moveToNext());

            txtProductList.setText(productList.toString());
            detailCursor.close();
        }
    }

    // ==================== XÁC NHẬN HỦY ĐƠN ====================
    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy đơn")
                .setMessage("Bạn có chắc chắn muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (dialog, which) -> cancelOrder())
                .setNegativeButton("Không", null)
                .show();
    }

    // ==================== HỦY ĐƠN ====================
    private void cancelOrder() {
        int result = orderDb.updateOrderStatus(orderId, "Đã hủy");

        if (result > 0) {
            Toast.makeText(this, "Đơn hàng đã được hủy!", Toast.LENGTH_SHORT).show();
            btnCancel.setEnabled(false);
            btnCancel.setText("Đã hủy");

            // Trả kết quả về cho danh sách đơn hàng
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi hủy đơn!", Toast.LENGTH_SHORT).show();
        }
    }
}

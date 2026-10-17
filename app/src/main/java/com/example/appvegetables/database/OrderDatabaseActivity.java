package com.example.appvegetables.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.appvegetables.Cart.ItemCart;
import com.example.appvegetables.Order.Order_model;
import com.example.appvegetables.database.DatabaseHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderDatabaseActivity {

    private DatabaseHelper dbHelper;

    public OrderDatabaseActivity(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ==================== LƯU ĐƠN HÀNG ====================
    public long saveOrder(String customerName, String phone, String address,
                          String note, String paymentMethod, double totalPrice,
                          ArrayList<ItemCart> cartItems) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long orderId = -1;

        try {
            db.beginTransaction();

            ContentValues orderValues = new ContentValues();
            orderValues.put(DatabaseHelper.COLUMN_CUSTOMER_NAME, customerName);
            orderValues.put(DatabaseHelper.COLUMN_PHONE, phone);
            orderValues.put(DatabaseHelper.COLUMN_ADDRESS, address);
            orderValues.put(DatabaseHelper.COLUMN_NOTE, note);
            orderValues.put(DatabaseHelper.COLUMN_PAYMENT_METHOD, paymentMethod);
            orderValues.put(DatabaseHelper.COLUMN_TOTAL_PRICE, totalPrice);
            orderValues.put(DatabaseHelper.COLUMN_STATUS, "Đã đặt");

            orderId = db.insert(DatabaseHelper.TABLE_ORDERS, null, orderValues);

            if (orderId != -1 && cartItems != null) {
                for (ItemCart item : cartItems) {
                    ContentValues itemValues = new ContentValues();
                    itemValues.put(DatabaseHelper.COLUMN_ORDER_FK_ID, orderId);
                    itemValues.put(DatabaseHelper.COLUMN_DETAIL_NAME, item.getName());
                    itemValues.put(DatabaseHelper.COLUMN_DETAIL_GIA, item.getGia());
                    itemValues.put(DatabaseHelper.COLUMN_DETAIL_SO_LUONG, item.getSo_luong_grid_view());
                    itemValues.put(DatabaseHelper.COLUMN_DETAIL_IMAGE, item.getHinh_anh_san_pham());
                    db.insert(DatabaseHelper.TABLE_ORDER_DETAIL, null, itemValues);
                    Log.d("ORDER_SAVE", "Order ID created: " + orderId);

                }
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return orderId;
    }

    // ==================== LẤY ĐƠN HÀNG THEO ID ====================
    public Cursor getOrderById(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE " + DatabaseHelper.COLUMN_ORDER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    // ==================== LẤY DANH SÁCH SẢN PHẨM TRONG ĐƠN ====================
    public Cursor getOrderItems(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_ORDER_DETAIL +
                " WHERE " + DatabaseHelper.COLUMN_ORDER_FK_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(orderId)});
    }

    // ==================== CẬP NHẬT TRẠNG THÁI ====================
    public int updateOrderStatus(long orderId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_STATUS, newStatus);
        Log.d("ORDER_UPDATE", "🟢 Thử cập nhật đơn ID = " + orderId + " sang: " + newStatus);

        int result = db.update(
                DatabaseHelper.TABLE_ORDERS,
                values,
                DatabaseHelper.COLUMN_ORDER_ID + " = ?",
                new String[]{String.valueOf(orderId)}
        );
        Log.d("ORDER_UPDATE", "🔹 Kết quả cập nhật: " + result);

        db.close();
        return result;
    }

    // ==================== LẤY DANH SÁCH ĐƠN THEO TRẠNG THÁI ====================
    public ArrayList<Order_model> getOrdersByStatus(String status) {
        ArrayList<Order_model> orders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ORDERS,
                null,
                DatabaseHelper.COLUMN_STATUS + " = ?",
                new String[]{status},
                null, null,
                DatabaseHelper.COLUMN_ORDER_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Order_model order = new Order_model();
                order.setOrderId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_ID)));
                Log.d("ORDER_DB", "Order ID load: " + order.getOrderId());

                order.setCustomerName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CUSTOMER_NAME)));
                order.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PHONE)));
                order.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ADDRESS)));
                order.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOTAL_PRICE)));
                order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_STATUS)));
                order.setOrderDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ORDER_DATE)));

                // Lấy danh sách sản phẩm chi tiết
                StringBuilder productList = new StringBuilder();
                Cursor detailCursor = db.query(
                        DatabaseHelper.TABLE_ORDER_DETAIL,
                        null,
                        DatabaseHelper.COLUMN_ORDER_FK_ID + " = ?",
                        new String[]{String.valueOf(order.getOrderId())},
                        null, null, null
                );

                if (detailCursor != null && detailCursor.moveToFirst()) {
                    DecimalFormat formatter = new DecimalFormat("#,###");
                    do {
                        String productName = detailCursor.getString(detailCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_NAME));
                        int quantity = detailCursor.getInt(detailCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_SO_LUONG));
                        double price = detailCursor.getDouble(detailCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DETAIL_GIA));
                        productList.append("• ").append(productName).append(" (x").append(quantity).append(")\n");
                    } while (detailCursor.moveToNext());
                    detailCursor.close();
                }

                order.setProductList(productList.toString());
                orders.add(order);

            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return orders;
    }
}

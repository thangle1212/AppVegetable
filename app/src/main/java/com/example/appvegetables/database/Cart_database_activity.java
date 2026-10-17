package com.example.appvegetables.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.appvegetables.Cart.ItemCart;

import java.util.ArrayList;

public class Cart_database_activity {
    private final DatabaseHelper dbHelper;

    public Cart_database_activity(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ➕ Thêm sản phẩm vào giỏ
    public long addToCart(ItemCart item) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CART,
                null,
                DatabaseHelper.COLUMN_PRODUCT_NAME + "=?",
                new String[]{item.getName()},
                null, null, null
        );

        try {
            if (cursor.moveToFirst()) {
                // Nếu sản phẩm đã có trong giỏ => tăng số lượng
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG));

                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG, currentQty + 1);

                db.update(
                        DatabaseHelper.TABLE_CART,
                        values,
                        DatabaseHelper.COLUMN_PRODUCT_NAME + "=?",
                        new String[]{item.getName()}
                );
                result = 1;
                Log.d("DB_CART", "🔁 Sản phẩm đã tồn tại, tăng số lượng");
            } else {
                // Nếu sản phẩm chưa có => thêm mới
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PRODUCT_NAME, item.getName());
                values.put(DatabaseHelper.COLUMN_PRODUCT_GIA, item.getGia());
                values.put(DatabaseHelper.COLUMN_IMAGE, item.getHinh_anh_san_pham());
                // đảm bảo ít nhất là 1
                int soLuong = item.getSo_luong_grid_view();
                if (soLuong <= 0) soLuong = 1;
                values.put(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG, soLuong);

                result = db.insert(DatabaseHelper.TABLE_CART, null, values);
                Log.d("DB_CART", "✅ Thêm sản phẩm mới vào giỏ, result = " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DB_CART", "❌ Lỗi thêm vào giỏ: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            if (db != null && db.isOpen()) db.close();
        }

        return result;
    }


    // 📜 Lấy tất cả sản phẩm trong giỏ
    public ArrayList<ItemCart> getAllCartItems() {
        ArrayList<ItemCart> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CART,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_NAME));
                String price = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_GIA));
                int image = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG));

                ItemCart item = new ItemCart(name, price, image, qty);
                list.add(item);
            } while (cursor.moveToNext());
        }
        android.util.Log.d("DB_CART", "🧾 Tổng sản phẩm trong DB: " + list.size());
        cursor.close();
        db.close();
        return list;
    }

    // 🧾 Cập nhật số lượng
    public int updateQuantity(String name, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG, quantity);

        int result = db.update(DatabaseHelper.TABLE_CART,
                values,
                DatabaseHelper.COLUMN_PRODUCT_NAME + "=?",
                new String[]{name});

        db.close();
        return result;
    }

    // ❌ Xóa sản phẩm theo tên
    public int deleteItem(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = db.delete(DatabaseHelper.TABLE_CART,
                DatabaseHelper.COLUMN_PRODUCT_NAME + "=?",
                new String[]{name});
        db.close();
        return result;
    }
    // 🧩 Cập nhật số lượng của sản phẩm trong giỏ
    public void updateCartItemQuantity(String productName, int newQuantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PRODUCT_SO_LUONG, newQuantity);
        db.update(DatabaseHelper.TABLE_CART, values,
                DatabaseHelper.COLUMN_PRODUCT_NAME + " = ?",
                new String[]{productName});
        db.close();
    }


    // 🧹 Xóa toàn bộ giỏ hàng
    public void clearCart() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CART, null, null);
        db.close();
    }
}

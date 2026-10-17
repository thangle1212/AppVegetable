package com.example.appvegetables.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Manager.db";
    private static final int DATABASE_VERSION = 7;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 🔹 Bảng user - ĐÃ SỬA LỖI
        String create_table_user = "CREATE TABLE " + TABLE_USER + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, " +
                COLUMN_PASS + " TEXT NOT NULL, " +
                COLUMN_SEX + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_SDT + " TEXT, " +
                COLUMN_ROLE + " TEXT DEFAULT 'user')"; // ✅ ĐÃ SỬA
        db.execSQL(create_table_user);

        // 🔹 Bảng cart
        String create_table_cart = "CREATE TABLE " + TABLE_CART + " (" +
                COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                COLUMN_PRODUCT_GIA + " TEXT NOT NULL, " +
                COLUMN_PRODUCT_SO_LUONG + " INTEGER NOT NULL, " +
                COLUMN_IMAGE + " INTEGER)";
        db.execSQL(create_table_cart);

        // 🔹 Bảng orders
        String create_table_orders = "CREATE TABLE " + TABLE_ORDERS + " (" +
                COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CUSTOMER_NAME + " TEXT NOT NULL, " +
                COLUMN_PHONE + " TEXT NOT NULL, " +
                COLUMN_ADDRESS + " TEXT NOT NULL, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_PAYMENT_METHOD + " TEXT NOT NULL, " +
                COLUMN_TOTAL_PRICE + " REAL NOT NULL, " +
                COLUMN_ORDER_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COLUMN_STATUS + " TEXT DEFAULT 'Đã đặt')";
        db.execSQL(create_table_orders);

        // 🔹 Bảng order_detail
        String create_table_order_details = "CREATE TABLE " + TABLE_ORDER_DETAIL + " (" +
                COLUMN_ORDER_DETAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ORDER_FK_ID + " INTEGER NOT NULL, " +
                COLUMN_DETAIL_NAME + " TEXT NOT NULL, " +
                COLUMN_DETAIL_GIA + " REAL NOT NULL, " +
                COLUMN_DETAIL_SO_LUONG + " INTEGER NOT NULL, " +
                COLUMN_DETAIL_IMAGE + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COLUMN_ORDER_FK_ID + ") REFERENCES " +
                TABLE_ORDERS + "(" + COLUMN_ORDER_ID + "))";
        db.execSQL(create_table_order_details);

        // 🔹 THÊM: Bảng products
        String create_table_products = "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PRODUCT_TEN + " TEXT NOT NULL, " +
                COLUMN_PRODUCT_PRICE + " REAL NOT NULL, " +
                COLUMN_PRODUCT_IMAGE + " TEXT, " +
                COLUMN_PRODUCT_DESCRIPTION + " TEXT, " +
                COLUMN_PRODUCT_CATEGORY + " TEXT, " +
                COLUMN_PRODUCT_STATUS + " TEXT DEFAULT 'active')";
        db.execSQL(create_table_products);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_DETAIL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS); // ✅ THÊM
        onCreate(db);
    }

    // ==================== ROLE ====================
    public static final String COLUMN_ROLE = "role";

    // ==================== USER ====================
    public static final String TABLE_USER = "user";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASS = "password";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_SDT = "sdt";
    public static final String COLUMN_SEX = "sex";

    // ==================== SẢN PHẨM ====================
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_PRODUCT_PRICE = "product_price";
    public static final String COLUMN_PRODUCT_IMAGE = "product_image";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "product_description";
    public static final String COLUMN_PRODUCT_CATEGORY = "product_category";
    public static final String COLUMN_PRODUCT_STATUS = "product_status";

    // ==================== GIỎ HÀNG ====================
    public static final String TABLE_CART = "cart";
    public static final String COLUMN_CART_ID = "id";
    public static final String COLUMN_PRODUCT_TEN = "product_name";
    public static final String COLUMN_PRODUCT_GIA = "product_price";
    public static final String COLUMN_PRODUCT_SO_LUONG = "quantity";
    public static final String COLUMN_IMAGE = "image";

    // ==================== ĐƠN HÀNG ====================
    public static final String TABLE_ORDERS = "orders";
    public static final String COLUMN_ORDER_ID = "order_id";
    public static final String COLUMN_CUSTOMER_NAME = "customer_name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_PAYMENT_METHOD = "payment_method";
    public static final String COLUMN_TOTAL_PRICE = "total_price";
    public static final String COLUMN_ORDER_DATE = "order_date";
    public static final String COLUMN_STATUS = "status";

    // ==================== CHI TIẾT ĐƠN HÀNG ====================
    public static final String TABLE_ORDER_DETAIL = "order_detail";
    public static final String COLUMN_ORDER_DETAIL_ID = "order_detail_id";
    public static final String COLUMN_ORDER_FK_ID = "order_fk_id";
    public static final String COLUMN_DETAIL_NAME = "detail_name";
    public static final String COLUMN_DETAIL_GIA = "detail_price";
    public static final String COLUMN_DETAIL_SO_LUONG = "detail_quantity";
    public static final String COLUMN_DETAIL_IMAGE = "detail_image";
}
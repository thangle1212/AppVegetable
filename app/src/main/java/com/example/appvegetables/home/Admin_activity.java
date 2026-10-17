package com.example.appvegetables.home;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;
import com.example.appvegetables.home.Product;
import com.example.appvegetables.home.VegetableAdapter;

import java.util.ArrayList;
import java.util.List;

public class Admin_activity extends AppCompatActivity {

    // MENU ADMIN - TƯƠNG TỰ NHƯ HOME
    private LinearLayout item_Home, item_ThongKe, item_DonHang, item_Account;
    private TextView text_Home, text_ThongKe, text_DonHang, text_Account;

    // GRIDVIEW và SEARCH VIEW - TƯƠNG TỰ NHƯ HOME
    private GridView gvDsThucPham;
    private EditText search_bar;
    private List<Product> productList;
    private List<Product> list_filtered;
    private VegetableAdapter adapter;

    // DATABASE
    private DatabaseHelper dbHelper;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_admin);

        // Hiển thị tên admin
        TextView txt_hello = findViewById(R.id.txt_hello_admin);
        String username = getIntent().getStringExtra("username");

        // Khởi tạo database
        dbHelper = new DatabaseHelper(this);

        // 1. Thiết lập menu admin
        setupBottomMenu();
        // 2. Thiết lập searchview
        search_view();
        // 3. Thiết lập GridView
        gvDsThucPham = findViewById(R.id.gv_ds_thuc_pham_admin);
        loadProductsFromDatabase();
        setupGridView();

        Toast.makeText(this, "Chào mừng đến trang Quản Trị!", Toast.LENGTH_SHORT).show();
    }

    // SEARCHVIEW - TƯƠNG TỰ NHƯ HOME
    @SuppressLint("ClickableViewAccessibility")
    private void search_view() {
        search_bar = findViewById(R.id.search_bar_admin);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString().trim();
                filter_product(string);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void filter_product(String string) {
        if (list_filtered == null) {
            list_filtered = new ArrayList<>();
        }

        list_filtered.clear();
        if (string.isEmpty()) {
            list_filtered.addAll(productList);
        } else {
            String search_lower_case = string.toLowerCase().trim();
            for (Product product : productList) {
                String name_product = product.getName().toLowerCase();
                if (name_product.contains(search_lower_case)) {
                    list_filtered.add(product);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // MENU ADMIN - TƯƠNG TỰ NHƯ HOME NHƯNG CÓ CHỨC NĂNG KHÁC
    @SuppressLint("WrongViewCast")
    private void setupBottomMenu() {
        // Gắn ID
        item_Home = findViewById(R.id.item_Home_customer);
        item_ThongKe = findViewById(R.id.item_thong_ke_admin);
        item_DonHang = findViewById(R.id.item_order_customer);
        item_Account = findViewById(R.id.item_account_customer);

        text_Home = findViewById(R.id.text_Home_admin);
        text_ThongKe = findViewById(R.id.text_thong_ke_admin);
        text_DonHang = findViewById(R.id.text_order_admin);
        text_Account = findViewById(R.id.text_account_admin);

        // click các icon ở thanh menu
        item_Home.setOnClickListener(v -> {
            selectMenu(item_Home);
            // Refresh danh sách sản phẩm
            loadProductsFromDatabase();
        });

        item_ThongKe.setOnClickListener(v -> {
            selectMenu(item_ThongKe);
            Toast.makeText(this, "Tính năng Thống kê đang phát triển", Toast.LENGTH_SHORT).show();
        });

        item_DonHang.setOnClickListener(v -> {
            selectMenu(item_DonHang);
            Toast.makeText(this, "Tính năng Đơn hàng đang phát triển", Toast.LENGTH_SHORT).show();
        });

        item_Account.setOnClickListener(v -> {
            selectMenu(item_Account);
            Toast.makeText(this, "Tính năng Tài khoản đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Mặc định chọn Trang chủ
        selectMenu(item_Home);
    }

    private void selectMenu(LinearLayout selectedItem) {
        // Reset tất cả item
        resetMenu();

        // Đặt nền cho item được chọn
        selectedItem.setBackgroundResource(R.drawable.bg_menu_customer_selected);

        // Hiện chữ của item được chọn
        if (selectedItem == item_Home) text_Home.setVisibility(View.VISIBLE);
        if (selectedItem == item_ThongKe) text_ThongKe.setVisibility(View.VISIBLE);
        if (selectedItem == item_DonHang) text_DonHang.setVisibility(View.VISIBLE);
        if (selectedItem == item_Account) text_Account.setVisibility(View.VISIBLE);
    }

    private void resetMenu() {
        // Reset nền
        item_Home.setBackgroundResource(R.drawable.bg_menu_customer);
        item_ThongKe.setBackgroundResource(R.drawable.bg_menu_customer);
        item_DonHang.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Account.setBackgroundResource(R.drawable.bg_menu_customer);

        // Ẩn text
        text_Home.setVisibility(View.GONE);
        text_ThongKe.setVisibility(View.GONE);
        text_DonHang.setVisibility(View.GONE);
        text_Account.setVisibility(View.GONE);
    }

    // ===================== QUẢN LÝ SẢN PHẨM =====================
    private void loadProductsFromDatabase() {
        productList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                new String[]{
                        DatabaseHelper.COLUMN_PRODUCT_TEN,
                        DatabaseHelper.COLUMN_PRODUCT_GIA,
                        DatabaseHelper.COLUMN_PRODUCT_IMAGE,
                        DatabaseHelper.COLUMN_PRODUCT_CATEGORY
                },
                null, null, null, null,
                DatabaseHelper.COLUMN_PRODUCT_TEN + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String productName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_TEN));
                double productPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_GIA));
                String productCategory = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRODUCT_CATEGORY));

                // Tạm thời dùng ảnh mặc định, sau này có thể lấy từ database
                int imageResource = getImageResourceByCategory(productCategory);

                String priceText = formatPrice(productPrice) + " / kg";
                productList.add(new Product(productName, priceText, imageResource));
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            // Nếu chưa có sản phẩm, thêm sản phẩm mẫu
            themSanPhamMau();
        }

        db.close();

        list_filtered = new ArrayList<>(productList);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupGridView() {
        adapter = new VegetableAdapter(this, list_filtered);

        adapter.setOnItemClickListener(new VegetableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                openProductManagement(product);
            }
        });

        gvDsThucPham.setAdapter(adapter);
    }

    private void openProductManagement(Product product) {
        // Hiển thị dialog hoặc activity để quản lý sản phẩm
        Toast.makeText(this, "Quản lý: " + product.getName(), Toast.LENGTH_SHORT).show();

        // Tạm thời hiển thị options
        showProductOptions(product);
    }

    private void showProductOptions(Product product) {
        // Có thể thay bằng Dialog hoặc BottomSheet
        String[] options = {"Sửa thông tin", "Xóa sản phẩm", "Thêm vào kho"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Quản lý: " + product.getName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Sửa
                    suaSanPham(product);
                    break;
                case 1: // Xóa
                    xoaSanPham(product);
                    break;
                case 2: // Thêm kho
                    themSoLuongSanPham(product);
                    break;
            }
        });
        builder.show();
    }

    // ===================== CÁC HÀM QUẢN LÝ SẢN PHẨM =====================
    private void themSanPhamMau() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Danh sách sản phẩm mẫu
        String[] tenSanPham = {"Rau Muống", "Rau Cải", "Cà Chua", "Cà Rốt", "Bắp Cải", "Su Hào"};
        double[] giaSanPham = {10000, 15000, 20000, 12000, 18000, 14000};
        String[] danhMuc = {"Rau lá", "Rau lá", "Củ quả", "Củ", "Rau lá", "Củ"};

        int count = 0;

        for (int i = 0; i < tenSanPham.length; i++) {
            // Kiểm tra xem sản phẩm đã tồn tại chưa
            Cursor cursor = db.query(
                    DatabaseHelper.TABLE_PRODUCTS,
                    new String[]{DatabaseHelper.COLUMN_PRODUCT_TEN},
                    DatabaseHelper.COLUMN_PRODUCT_TEN + " = ?",
                    new String[]{tenSanPham[i]},
                    null, null, null
            );

            if (cursor.getCount() == 0) {
                // Thêm sản phẩm mới
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PRODUCT_TEN, tenSanPham[i]);
                values.put(DatabaseHelper.COLUMN_PRODUCT_GIA, giaSanPham[i]);
                values.put(DatabaseHelper.COLUMN_PRODUCT_CATEGORY, danhMuc[i]);
                values.put(DatabaseHelper.COLUMN_PRODUCT_DESCRIPTION, tenSanPham[i] + " tươi ngon");

                long result = db.insert(DatabaseHelper.TABLE_PRODUCTS, null, values);
                if (result != -1) {
                    count++;
                }
            }
            cursor.close();
        }

        db.close();

        if (count > 0) {
            Toast.makeText(this, "Đã thêm " + count + " sản phẩm mẫu", Toast.LENGTH_SHORT).show();
            loadProductsFromDatabase(); // Tải lại danh sách
        }
    }

    private void suaSanPham(Product product) {
        // Tăng giá sản phẩm lên 10% (tạm thời)
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PRODUCTS,
                new String[]{DatabaseHelper.COLUMN_PRODUCT_GIA},
                DatabaseHelper.COLUMN_PRODUCT_TEN + " = ?",
                new String[]{product.getName()},
                null, null, null
        );

        if (cursor.moveToFirst()) {
            double currentPrice = cursor.getDouble(0);
            double newPrice = currentPrice * 1.1; // Tăng 10%

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_PRODUCT_GIA, newPrice);

            int result = db.update(
                    DatabaseHelper.TABLE_PRODUCTS,
                    values,
                    DatabaseHelper.COLUMN_PRODUCT_TEN + " = ?",
                    new String[]{product.getName()}
            );

            if (result > 0) {
                Toast.makeText(this, "Đã sửa giá " + product.getName() + ": " +
                        formatPrice(currentPrice) + " → " + formatPrice(newPrice), Toast.LENGTH_SHORT).show();
                loadProductsFromDatabase();
            }
        }
        cursor.close();
        db.close();
    }

    private void xoaSanPham(Product product) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc muốn xóa sản phẩm: " + product.getName() + "?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            int result = db.delete(
                    DatabaseHelper.TABLE_PRODUCTS,
                    DatabaseHelper.COLUMN_PRODUCT_TEN + " = ?",
                    new String[]{product.getName()}
            );

            if (result > 0) {
                Toast.makeText(this, "Đã xóa sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
                loadProductsFromDatabase();
            } else {
                Toast.makeText(this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
            db.close();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void themSoLuongSanPham(Product product) {
        Toast.makeText(this, "Đã thêm số lượng cho: " + product.getName(), Toast.LENGTH_SHORT).show();
        // Có thể phát triển thêm tính năng quản lý kho
    }

    // ===================== HÀM HỖ TRỢ =====================
    private String formatPrice(double price) {
        if (price >= 1000) {
            return String.format("%,.0f VND", price);
        } else {
            return String.format("%.0f VND", price);
        }
    }

    private int getImageResourceByCategory(String category) {
        // Ánh xạ danh mục với ảnh (tạm thời)
        switch (category) {
            case "Rau lá":
                return R.drawable.rau_muong;
            case "Củ quả":
                return R.drawable.dua_chuot;
            case "Củ":
                return R.drawable.bap_cai;
            default:
                return R.drawable.rau_cai;
        }
    }

    // ===================== QUẢN LÝ VÒNG ĐỜI =====================
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh danh sách khi quay lại
        if (search_bar != null) {
            search_bar.setText("");
            search_bar.clearFocus();
        }
        loadProductsFromDatabase();
    }
}
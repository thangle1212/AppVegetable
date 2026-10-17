package com.example.appvegetables.account;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;

public class account_doi_mat_khau extends AppCompatActivity {

    EditText edt_pass_old, edt_pass_new, edt_pass_re_pass;
    Button btn_doi_mat_khau;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_doi_mat_khau);

        dbHelper = new DatabaseHelper(this);

        // Tìm các view
        edt_pass_old = findViewById(R.id.edt_pass_old);
        edt_pass_new = findViewById(R.id.edt_pass_new);
        edt_pass_re_pass = findViewById(R.id.edt_pass_re_enter);
        btn_doi_mat_khau = findViewById(R.id.btn_thay_mat_khau);

        // Kiểm tra session
        kiemTraSession();

        // Thêm chức năng ẩn/hiện mật khẩu
        setupAnHienMatKhau(edt_pass_old);
        setupAnHienMatKhau(edt_pass_new);
        setupAnHienMatKhau(edt_pass_re_pass);

        // Xử lý nút đổi mật khẩu
        btn_doi_mat_khau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doiMatKhau();
            }
        });
    }

    // KIỂM TRA SESSION
    private void kiemTraSession() {
        SharedPreferences prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        int userId = prefs.getInt("USER_ID", -1);
        String username = prefs.getString("USERNAME", "null");
        boolean isLoggedIn = prefs.getBoolean("IS_LOGGED_IN", false);

        Log.d("SESSION_CHECK", "User ID: " + userId);
        Log.d("SESSION_CHECK", "Username: " + username);
        Log.d("SESSION_CHECK", "Is Logged In: " + isLoggedIn);

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Chưa đăng nhập! Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // Hàm ẩn/hiện mật khẩu
    @SuppressLint("ClickableViewAccessibility")
    private void setupAnHienMatKhau(final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - 100)) {
                        anHienMatKhau(editText);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    // Hàm chuyển đổi giữa ẩn và hiện mật khẩu
    private void anHienMatKhau(EditText editText) {
        if (editText.getTransformationMethod() == null) {
            // Đang HIỆN -> chuyển sang ẨN
            editText.setTransformationMethod(new PasswordTransformationMethod());
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_lock, 0, R.drawable.icon_eye_visibility, 0);
        } else {
            // Đang ẨN -> chuyển sang HIỆN
            editText.setTransformationMethod(null);
            editText.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.icon_lock, 0, R.drawable.icon_eye_visibility_off, 0);
        }

        // Đặt con trỏ về cuối text
        editText.setSelection(editText.getText().length());
    }

    // HÀM CHÍNH ĐỔI MẬT KHẨU
    private void doiMatKhau() {
        String matKhauCu = edt_pass_old.getText().toString().trim();
        String matKhauMoi = edt_pass_new.getText().toString().trim();
        String nhapLaiMatKhau = edt_pass_re_pass.getText().toString().trim();

        Log.d("DEBUG", "Bắt đầu đổi mật khẩu...");
        Log.d("DEBUG", "Mật khẩu cũ: '" + matKhauCu + "'");
        Log.d("DEBUG", "Mật khẩu mới: '" + matKhauMoi + "'");
        Log.d("DEBUG", "Nhập lại: '" + nhapLaiMatKhau + "'");

        // Kiểm tra ô trống
        if (matKhauCu.isEmpty() || matKhauMoi.isEmpty() || nhapLaiMatKhau.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu mới có khớp không
        if (!matKhauMoi.equals(nhapLaiMatKhau)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lấy ID người dùng đang đăng nhập
        int userId = layUserId();

        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu cũ có đúng không
        if (kiemTraMatKhauCu(userId, matKhauCu)) {
            // Cập nhật mật khẩu mới
            if (capNhatMatKhau(userId, matKhauMoi)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                // Clear các field
                edt_pass_old.setText("");
                edt_pass_new.setText("");
                edt_pass_re_pass.setText("");

                // Quay lại màn hình trước
                finish();
            } else {
                Toast.makeText(this, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    // Lấy ID user từ SharedPreferences
    private int layUserId() {
        SharedPreferences prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        return prefs.getInt("USER_ID", -1);
    }

    // Kiểm tra mật khẩu cũ
    private boolean kiemTraMatKhauCu(int userId, String matKhauCu) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + DatabaseHelper.COLUMN_PASS +
                " FROM " + DatabaseHelper.TABLE_USER +
                " WHERE " + DatabaseHelper.COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        Log.d("DEBUG_PASSWORD", "User ID: " + userId);
        Log.d("DEBUG_PASSWORD", "Mật khẩu nhập: " + matKhauCu);

        if (cursor.moveToFirst()) {
            String matKhauTrongDB = cursor.getString(0);

            Log.d("DEBUG_PASSWORD", "Mật khẩu trong DB: " + matKhauTrongDB);
            Log.d("DEBUG_PASSWORD", "So sánh: " + matKhauCu.equals(matKhauTrongDB));

            cursor.close();
            db.close();
            return matKhauCu.equals(matKhauTrongDB);
        } else {
            Log.d("DEBUG_PASSWORD", "Không tìm thấy user trong database");
        }

        cursor.close();
        db.close();
        return false;
    }

    // Cập nhật mật khẩu mới
    private boolean capNhatMatKhau(int userId, String matKhauMoi) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_PASS, matKhauMoi);

        int result = db.update(DatabaseHelper.TABLE_USER, values,
                DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(userId)});
        db.close();
        return result > 0;
    }
}
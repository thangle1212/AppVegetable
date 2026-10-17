package com.example.appvegetables.login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;

public class login_forgot_pass extends AppCompatActivity {

    private EditText edt_forgot_email, edt_forgot_sdt, edt_pass_new_enter, edt_pass_new_re_enter;
    private Button btn_thay_mat_khau;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_quen_mat_khau); // Thay bằng tên file XML của bạn

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupEvents();
        setupPasswordVisibility();
    }

    private void initViews() {
        edt_forgot_email = findViewById(R.id.edt_forgot_email);
        edt_forgot_sdt = findViewById(R.id.edt_forgot_sdt);
        edt_pass_new_enter = findViewById(R.id.edt_pass_new_enter);
        edt_pass_new_re_enter = findViewById(R.id.edt_pass_new_re_enter);
        btn_thay_mat_khau = findViewById(R.id.btn_thay_mat_khau);
    }

    private void setupEvents() {
        btn_thay_mat_khau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                xuLyQuenMatKhau();
            }
        });
    }

    private void setupPasswordVisibility() {
        setupAnHienMatKhau(edt_pass_new_enter);
        setupAnHienMatKhau(edt_pass_new_re_enter);
    }

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
        editText.setSelection(editText.getText().length());
    }

    private void xuLyQuenMatKhau() {
        String email = edt_forgot_email.getText().toString().trim();
        String sdt = edt_forgot_sdt.getText().toString().trim();
        String matKhauMoi = edt_pass_new_enter.getText().toString().trim();
        String nhapLaiMatKhau = edt_pass_new_re_enter.getText().toString().trim();

        // Kiểm tra ô trống
        if (email.isEmpty() || sdt.isEmpty() || matKhauMoi.isEmpty() || nhapLaiMatKhau.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra mật khẩu mới có khớp không
        if (!matKhauMoi.equals(nhapLaiMatKhau)) {
            Toast.makeText(this, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
            edt_pass_new_re_enter.setError("Mật khẩu không khớp");
            return;
        }

        // Kiểm tra email và số điện thoại có tồn tại không
        int userId = kiemTraThongTinUser(email, sdt);

        if (userId != -1) {
            // Cập nhật mật khẩu mới
            if (capNhatMatKhau(userId, matKhauMoi)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                finish(); // Quay lại màn hình đăng nhập
            } else {
                Toast.makeText(this, "Lỗi! Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Email hoặc số điện thoại không đúng", Toast.LENGTH_SHORT).show();
        }
    }

    private int kiemTraThongTinUser(String email, String sdt) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " + DatabaseHelper.COLUMN_ID +
                " FROM " + DatabaseHelper.TABLE_USER +
                " WHERE " + DatabaseHelper.COLUMN_EMAIL + " = ? AND " +
                DatabaseHelper.COLUMN_SDT + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{email, sdt});

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
        }

        cursor.close();
        db.close();
        return userId;
    }

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
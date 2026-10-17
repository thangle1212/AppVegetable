package com.example.appvegetables.account;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;

public class account_thong_tin_ca_nhan extends AppCompatActivity {

    private EditText edtHoTen, edtEmail, edtSoDienThoai, edtNamSinh;
    private RadioGroup rdg_Gioi_Tinh;
    private Button btnLuuThongTin;

    private DatabaseHelper databaseHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_thong_tin_ca_nhan);

        databaseHelper = new DatabaseHelper(this);
        currentUserId = getCurrentUserId();

        initViews();
        setupEvents();
        loadCurrentUserData();
    }

    private void initViews() {
        edtHoTen = findViewById(R.id.edt_Ho_Ten);
        edtEmail = findViewById(R.id.edt_Email);
        edtSoDienThoai = findViewById(R.id.edt_So_Dien_Thoai);
        edtNamSinh = findViewById(R.id.edt_Nam_Sinh);
        rdg_Gioi_Tinh = findViewById(R.id.rdg_Gioi_Tinh);
        btnLuuThongTin = findViewById(R.id.btnLuuThongTin);
    }

    private void setupEvents() {
        btnLuuThongTin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePersonalInfo();
            }
        });
    }

    private int getCurrentUserId() {
        SharedPreferences preferences = getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE);
        return preferences.getInt("USER_ID", 1); // Mặc định là 1
    }

    private void loadCurrentUserData() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();

        String[] projection = {
                DatabaseHelper.COLUMN_USERNAME,
                DatabaseHelper.COLUMN_EMAIL,
                DatabaseHelper.COLUMN_SDT,
                DatabaseHelper.COLUMN_SEX
        };

        String selection = DatabaseHelper.COLUMN_ID + " = ?";
        String[] selectionArgs = { String.valueOf(currentUserId) };

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USER,
                projection,
                selection,
                selectionArgs,
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EMAIL));
            String sdt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SDT));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SEX));

            // Hiển thị lên form
            edtHoTen.setText(username);
            edtEmail.setText(email);
            edtSoDienThoai.setText(sdt);

            // Set giới tính
            if (sex != null && sex.equals("Nữ")) {
                ((RadioButton)findViewById(R.id.rd_Nu)).setChecked(true);
            } else {
                ((RadioButton)findViewById(R.id.rd_Nam)).setChecked(true);
            }

            cursor.close();
        }
        db.close();
    }

    private void savePersonalInfo() {
        // Lấy dữ liệu từ form
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String soDienThoai = edtSoDienThoai.getText().toString().trim();
        String namSinh = edtNamSinh.getText().toString().trim();

        // Lấy giới tính
        String gioiTinh = "Nam";
        int selectedId = rdg_Gioi_Tinh.getCheckedRadioButtonId();
        if (selectedId == R.id.rd_Nu) {
            gioiTinh = "Nữ";
        }

        // 🔥 KIỂM TRA ĐƠN GIẢN THEO YÊU CẦU
        if (hoTen.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.contains("@gmail.com")) {
            Toast.makeText(this, "Email phải có @gmail.com", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!soDienThoai.startsWith("0") || soDienThoai.length() != 10) {
            Toast.makeText(this, "Số điện thoại phải bắt đầu bằng 0 và có 10 số", Toast.LENGTH_SHORT).show();
            return;
        }

        // Năm sinh: nhập bao nhiêu cũng được, không cần kiểm tra

        // Lưu vào database
        if (updateUserInfo(hoTen, email, soDienThoai, gioiTinh)) {
            Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
            finish(); // Quay về màn hình trước
        } else {
            Toast.makeText(this, "Cập nhật thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean updateUserInfo(String hoTen, String email, String soDienThoai, String gioiTinh) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USERNAME, hoTen);
        values.put(DatabaseHelper.COLUMN_EMAIL, email);
        values.put(DatabaseHelper.COLUMN_SDT, soDienThoai);
        values.put(DatabaseHelper.COLUMN_SEX, gioiTinh);

        String whereClause = DatabaseHelper.COLUMN_ID + " = ?";
        String[] whereArgs = { String.valueOf(currentUserId) };

        int rowsAffected = db.update(DatabaseHelper.TABLE_USER, values, whereClause, whereArgs);
        db.close();

        return rowsAffected > 0;
    }
}
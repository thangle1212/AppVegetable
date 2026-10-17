package com.example.appvegetables.login;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.database.DatabaseHelper;
import com.example.appvegetables.home.Admin_activity;
import com.example.appvegetables.home.Home;
import com.example.appvegetables.R;

public class login_register_activity extends AppCompatActivity {
    EditText edt_user_login, edt_pass_login, edt_user_dang_ki, edt_pass_dang_ki, edt_pass_re_enter;
    LinearLayout form_login, form_dang_ki;
    TextView txt_login, txt_dang_ki, txt_forgot_pass;
    Button btn_login, btn_dang_ki;
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_register);
        dbHelper =new DatabaseHelper(this);
        db =dbHelper.getWritableDatabase();

        // Ánh xạ
        form_dang_ki = findViewById(R.id.form_dang_ki);
        form_login = findViewById(R.id.form_login);
        edt_pass_login = findViewById(R.id.edt_pass_login);
        edt_user_dang_ki = findViewById(R.id.edt_user_dang_ki);
        edt_user_login = findViewById(R.id.edt_user_login);
        edt_pass_dang_ki = findViewById(R.id.edt_pass_dang_ki);
        edt_pass_re_enter = findViewById(R.id.edt_pass_re_enter);

        // TextView
        txt_login = findViewById(R.id.txt_login);
        txt_dang_ki = findViewById(R.id.txt_dang_ki);
        txt_forgot_pass = findViewById(R.id.txt_forgot_pass);

        // Button
        btn_login = findViewById(R.id.btn_login);
        btn_dang_ki = findViewById(R.id.btn_dang_ki);

        Swap_login_register();
        click_buton();
        eyeIconClick();
        taoTaiKhoanAdmin();
    }

    private void setActiveTab(TextView activeTab, TextView inactiveTab) {
        // Tab có gạch chân, đậm, màu đen
        activeTab.setTextColor(getResources().getColor(android.R.color.black));
        activeTab.setTypeface(null, Typeface.BOLD);
        activeTab.setPaintFlags(activeTab.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        // Tab không gạch chân, bình thường, màu xám
        inactiveTab.setTextColor(getResources().getColor(android.R.color.darker_gray));
        inactiveTab.setTypeface(null, Typeface.NORMAL);
        inactiveTab.setPaintFlags(inactiveTab.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
    }

    private void Swap_login_register() {
        form_login.setVisibility(View.VISIBLE);
        form_dang_ki.setVisibility(View.GONE);
        setActiveTab(txt_login, txt_dang_ki);

        // Bấm đăng nhập ẩn đăng kí
        txt_login.setOnClickListener(v -> {
            form_login.setVisibility(View.VISIBLE);
            form_dang_ki.setVisibility(View.GONE);
            setActiveTab(txt_login, txt_dang_ki);
        });

        // Bấm đăng kí thì ẩn đăng nhập
        txt_dang_ki.setOnClickListener(v -> {
            form_login.setVisibility(View.GONE);
            form_dang_ki.setVisibility(View.VISIBLE);
            setActiveTab(txt_dang_ki, txt_login);
        });
    }

    private void click_buton() {
        btn_login.setOnClickListener(v -> DangNhap());

        btn_dang_ki.setOnClickListener(v -> DangKy());

        txt_forgot_pass.setOnClickListener(v -> {
            Intent intent=new Intent(login_register_activity.this, login_forgot_pass.class);
            startActivity(intent);
        });
    }

    // HÀM LƯU SESSION ĐĂNG NHẬP
    private void luuSessionDangNhap(int userId, String username, String role) {
        SharedPreferences prefs = getSharedPreferences("USER_SESSION", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("USER_ID", userId);
        editor.putString("USERNAME", username);
        editor.putString("ROLE", role);
        editor.putBoolean("IS_LOGGED_IN", true);
        editor.apply();

        Log.d("LOGIN_DEBUG", "Đã lưu session - User ID: " + userId + ", Username: " + username + ", Role: " +role);
    }

    // Đăng nhập
    private void DangNhap() {
        String user = edt_user_login.getText().toString().trim();
        String pass = edt_pass_login.getText().toString().trim();

        if(user.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, username, role FROM user WHERE username =? AND password=?",
                new String[]{user, pass});
        if(cursor.getCount() > 0){
            cursor.moveToFirst();

            // LẤY USER ID từ database
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String role = cursor.getString(cursor.getColumnIndexOrThrow("role"));

            cursor.close();
            db.close();

            // LƯU SESSION ĐĂNG NHẬP
            luuSessionDangNhap(userId, username,role);
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();


            if ("admin".equals(role)) {
                Log.d("LOGIN_DEBUG", "🔧 Chuyển đến ADMIN");
                try {
                    // ✅ SỬA: Dùng AdminMainActivity thay vì Admin_activity
                    Intent intent = new Intent(this, com.example.appvegetables.home.Admin_activity.class);
                    intent.putExtra("username", user); // Truyền username nếu cần
                    startActivity(intent);
                    Log.d("LOGIN_DEBUG", "✅ Đã chuyển đến AdminMainActivity thành công");
                } catch (Exception e) {
                    Log.e("LOGIN_DEBUG", "❌ Lỗi khi chuyển đến admin: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi hệ thống, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        } else {
            Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
        }
    }

    // Đăng kí
    private void DangKy() {
        String user = edt_user_dang_ki.getText().toString().trim();
        String pass = edt_pass_dang_ki.getText().toString().trim();
        String re_pass = edt_pass_re_enter.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty() || re_pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(re_pass)) {
            Toast.makeText(this, "Mật khẩu nhập lại sai!", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE username=?", new String[]{user});

        if(cursor.getCount() > 0){
            Toast.makeText(this, "Tài khoản tồn tại!", Toast.LENGTH_SHORT).show();
            cursor.close();
            db.close();
            return;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("username", user);
        values.put("password", pass);
        long result = db.insert("user", null, values);

        if(result != -1){
            // SAU KHI ĐĂNG KÝ THÀNH CÔNG, TỰ ĐỘNG ĐĂNG NHẬP
            Cursor newUserCursor = db.rawQuery("SELECT id, role FROM user WHERE username=?", new String[]{user});
            if(newUserCursor.moveToFirst()){
                int newUserId = newUserCursor.getInt(0);
                String role = newUserCursor.getString(1);
                luuSessionDangNhap(newUserId, user, role);
            }
            newUserCursor.close();

            Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển đến Home
            Intent intent = new Intent(this, Home.class);
            intent.putExtra("username", user);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this,"Đăng kí thất bại!", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    // hiện thị mật khẩu
    private void PasswordVisibility(EditText passwordEditText) {
        //Lấy vị trí con trỏ hiện tại
        int Position = passwordEditText.getSelectionEnd();
        // Ktra xem hiện tại đang ẩn hay hiện mật khẩu
        if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            // từ typepass ->text
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                    passwordEditText.getCompoundDrawables()[0],//đây là giữ lại icon bên trái
                    null,
                    ContextCompat.getDrawable(this, R.drawable.icon_eye_visibility_off), null
            );
        } else{
            // từ text ->typepass
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                    passwordEditText.getCompoundDrawables()[0],null,
                    ContextCompat.getDrawable(this, R.drawable.icon_eye_visibility), null);
        }
        passwordEditText.setSelection(Position);//con trỏ trở về chỗ cux để gõ tiếp
    }

    @SuppressLint("ClickableViewAccessibility")
    private void eyeIconClick() {
        // xử lí ô pass login
        edt_pass_login.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Xác định vị trí click có phải là icon bên phải không
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt_pass_login.getRight() - edt_pass_login.getCompoundDrawables()[2].getBounds().width())) {
                        // Nếu click vào icon mắt -> bật/tắt hiển thị mật khẩu
                        PasswordVisibility(edt_pass_login);
                        return true;
                    }
                }
                return false;
            }
        });

        // Xử lý cho pass đăng kí
        edt_pass_dang_ki.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edt_pass_dang_ki.getRight() - edt_pass_dang_ki.getCompoundDrawables()[2].getBounds().width())) {
                    PasswordVisibility(edt_pass_dang_ki);
                    return true;
                }
            }
            return false;
        });

        // Xử lý cho ô re_enter pass
        edt_pass_re_enter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (edt_pass_re_enter.getRight() - edt_pass_re_enter.getCompoundDrawables()[2].getBounds().width())) {
                        PasswordVisibility(edt_pass_re_enter);
                        return true;
                    }
                }
                return false;
            }
        });
    }
    private void taoTaiKhoanAdmin() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // KIỂM TRA KỸ CÓ PHẢI ADMIN ĐÚNG KHÔNG
        Cursor cursor = db.rawQuery("SELECT username, password, role FROM user WHERE username = ?", new String[]{"admin"});

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            String currentPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
            String currentRole = cursor.getString(cursor.getColumnIndexOrThrow("role"));

            Log.d("ADMIN_CHECK", "Admin hiện tại - Password: " + currentPassword + ", Role: " + currentRole);

            // NẾU ADMIN CÓ VẤN ĐỀ, XÓA VÀ TẠO LẠI
            if (!"admin123".equals(currentPassword) || !"admin".equals(currentRole)) {
                Log.d("ADMIN_CHECK", "❌ Admin có vấn đề, xóa và tạo lại...");

                // Xóa admin cũ
                db.delete("user", "username = ?", new String[]{"admin"});

                // Tạo admin mới
                ContentValues values = new ContentValues();
                values.put("username", "admin");
                values.put("password", "admin123");
                values.put("role", "admin");
                values.put("email", "admin@example.com");
                values.put("sdt", "0123456789");

                long result = db.insert("user", null, values);
                if (result != -1) {
                    Log.d("ADMIN_SETUP", "✅ Đã tạo lại tài khoản admin thành công");
                }
            } else {
                Log.d("ADMIN_CHECK", "✅ Admin đã đúng: admin/admin123/role=admin");
            }
        } else {
            // Tạo admin mới nếu chưa có
            Log.d("ADMIN_SETUP", "Tạo admin mới...");
            ContentValues values = new ContentValues();
            values.put("username", "admin");
            values.put("password", "admin123");
            values.put("role", "admin");
            values.put("email", "admin@example.com");
            values.put("sdt", "0123456789");

            long result = db.insert("user", null, values);
            if (result != -1) {
                Log.d("ADMIN_SETUP", "✅ Đã tạo tài khoản admin thành công");
            }
        }

        cursor.close();
        db.close();
    }
    private void resetDatabase() {
        Log.d("DB_RESET", "🔧 Bắt đầu reset database...");

        // Đóng kết nối hiện tại
        if (db != null && db.isOpen()) {
            db.close();
        }

        // Xóa database cũ
        boolean result = deleteDatabase("Manager.db");
        Log.d("DB_RESET", "Xóa database cũ: " + result);

        // Tạo lại database
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        Log.d("DB_RESET", "✅ Đã tạo lại database mới");
    }
}
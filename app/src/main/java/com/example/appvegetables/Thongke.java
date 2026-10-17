package com.example.appvegetables.ThongKe;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.R;
import com.example.appvegetables.database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ThongKeActivity extends AppCompatActivity {

    EditText edtNgayBatDau, edtNgayKetThuc;
    Button btnThongKe;
    TextView txtDoanhThu;
    DatabaseHelper dbHelper;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke); // đổi tên đúng XML của bạn

        // Ánh xạ view
        edtNgayBatDau = findViewById(R.id.edtNgayBatDau);
        edtNgayKetThuc = findViewById(R.id.edtNgayKetThuc);
        btnThongKe = findViewById(R.id.btnThongKe);
        txtDoanhThu = findViewById(R.id.txtDoanhThu);

        dbHelper = new DatabaseHelper(this);

        // Chọn ngày bắt đầu
        edtNgayBatDau.setOnClickListener(v -> showDatePicker(edtNgayBatDau));

        // Chọn ngày kết thúc
        edtNgayKetThuc.setOnClickListener(v -> showDatePicker(edtNgayKetThuc));

        // Xử lý nút thống kê
        btnThongKe.setOnClickListener(v -> {
            String startDateStr = edtNgayBatDau.getText().toString().trim();
            String endDateStr = edtNgayKetThuc.getText().toString().trim();

            if (startDateStr.isEmpty() || endDateStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double doanhThu = tinhDoanhThu(startDateStr, endDateStr);
                txtDoanhThu.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", doanhThu));
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi định dạng ngày!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                    targetEditText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    // ===================== TÍNH DOANH THU =====================
    private double tinhDoanhThu(String startDate, String endDate) throws ParseException {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Chuyển định dạng ngày để so sánh (YYYY-MM-DD)
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String start = dbFormat.format(sdf.parse(startDate));
        String end = dbFormat.format(sdf.parse(endDate));

        double tongDoanhThu = 0;

        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_TOTAL_PRICE + ") AS tong " +
                "FROM " + DatabaseHelper.TABLE_ORDERS +
                " WHERE " + DatabaseHelper.COLUMN_STATUS + " = ?" +
                " AND date(" + DatabaseHelper.COLUMN_ORDER_DATE + ") BETWEEN date(?) AND date(?)";

        Cursor cursor = db.rawQuery(query, new String[]{"Đã đặt", start, end});

        if (cursor != null && cursor.moveToFirst()) {
            tongDoanhThu = cursor.getDouble(cursor.getColumnIndexOrThrow("tong"));
            cursor.close();
        }

        db.close();
        return tongDoanhThu;
    }
}

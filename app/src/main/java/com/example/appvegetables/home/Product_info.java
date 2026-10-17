package com.example.appvegetables.home;

import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appvegetables.Cart.CartManager;
import com.example.appvegetables.Cart.ItemCart;
import com.example.appvegetables.R;
import com.example.appvegetables.database.Cart_database_activity;

public class Product_info extends AppCompatActivity {
    private ImageView img_product_detail;
    private TextView txt_product_name, txt_product_mo_ta, txt_product_price;
    private Button btn_add_cart;
    private Product product;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_info);
        initView();
        getProductData();
        setupclick();
    }

    private void initView() {
        img_product_detail = findViewById(R.id.img_product_detail);
        txt_product_name = findViewById(R.id.txt_product_name);
        txt_product_price = findViewById(R.id.txt_product_price);
        txt_product_mo_ta = findViewById(R.id.txt_product_mo_ta);
        btn_add_cart = findViewById(R.id.btn_add_cart);

        txt_product_mo_ta.setMovementMethod(new ScrollingMovementMethod());
    }
        private void getProductData(){
        product = (Product) getIntent().getSerializableExtra("PRODUCT");
        if (product != null) {
            displayProductInfo();
        } else {
            Toast.makeText(this, "Lỗi: Không có thông tin sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayProductInfo() {
        // Hiển thị thông tin sản phẩm
        img_product_detail.setImageResource(product.getImageResId());
        txt_product_name.setText(product.getName());
        txt_product_price.setText(product.getPrice());

        // Thêm mô tả chi tiết cho từng sản phẩm
        String description = getProductDescription(product.getName());
        if (description.contains("<br/>") || description.contains("<b>") || description.contains("<")) {
            txt_product_mo_ta.setText(Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY));
        } else {
            txt_product_mo_ta.setText(description);
        }
    }

    private String getProductDescription(String productName) {
        // Tạo mô tả dựa trên tên sản phẩm
        switch (productName) {
            case "Rau muống":
                return getString(R.string.rau_muong_description);
            case "Bắp cải":
                return getString(R.string.bap_cai_description);
            /*case "Rau cải":
                return getString(R.string.rau_cai_description);*/
            case "Dưa chuột":
                return getString(R.string.dua_chuot_description);
            default:
                return getString(R.string.default_product_description);

        }
    }
    private void setupclick() {
        btn_add_cart.setOnClickListener(v -> {
            addtoCart();
        });
    }

    private void addtoCart() {
        if (product != null) {
            // Tạo ItemCart với số lượng mặc định là 1
            ItemCart item = new ItemCart(
                    product.getName(),
                    product.getPrice(),
                    product.getImageResId(),
                    1 // Số lượng mặc định
            );
            // Thêm vào giỏ hàng và lưu vào database
            Cart_database_activity dbCart = new Cart_database_activity(this);
            long result = dbCart.addToCart(item);

            // Thông báo thành công
            Toast.makeText(this, "Đã thêm " + product.getName() + " vào giỏ hàng",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
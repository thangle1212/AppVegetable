package com.example.appvegetables.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appvegetables.account.Account;
import com.example.appvegetables.Cart.Cart_activity;
import com.example.appvegetables.Order.Order_Activity;
import com.example.appvegetables.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Home extends AppCompatActivity {

    // MENU KHÁCH HÀNG
    private LinearLayout item_Home, item_Cart, item_Order, item_Account;
    private TextView text_Home, text_Cart, text_Order, text_Account;

    // GRIDVIEW và SEARCH VIEW
    private GridView gvDsThucPham;
    private EditText search_bar;
    private List<Product> productList;
    private List<Product> list_filtered;
    private VegetableAdapter adapter;

    // IMAGE SLIDER
    private RecyclerView recyclerViewHorizontal;
    private ImageSliderAdapter imageSliderAdapter;
    private Timer sliderTimer;
    private int currentPage = 0;
    private final long SLIDER_DELAY = 3000; // 3 giggly
    private List<Integer> imageList;

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        TextView txt_hello= findViewById(R.id.txt_hello);
        String username= getIntent().getStringExtra("username");
        if(username !=null){
            txt_hello.setText(username + "!");
        }
        // 1Thiết lập menu khách hàng
        setupBottomMenu();
        // Thiết lập Image Slider
        setupImageSlider();
        // Thiêt lập searchview
        search_view();

        //Thiết lập GridView
        gvDsThucPham = findViewById(R.id.gv_ds_thuc_pham);
        initVegetableData();
        setupGridView();
    }

    // SEARCHVIEW
    @SuppressLint("ClickableViewAccessibility")
    private void search_view(){
        search_bar =findViewById(R.id.search_Bar);
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String string =s.toString().trim();
                filter_product(string);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }//null

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }//null
        });
        search_bar.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            Drawable drawableRight = search_bar.getCompoundDrawables()[DRAWABLE_RIGHT];

            // kiểm tra xem có null không trc khi action
            if (drawableRight != null && event.getAction() == MotionEvent.ACTION_UP) {
                if (!(event.getRawX() >= (search_bar.getRight() - drawableRight.getBounds().width()))) {
                    return false;
                }
                perform_search();
                return true;
            }
            return false;
        });
    }
    private void perform_search(){
        String perform=search_bar.getText().toString().trim();
        filter_product(perform);
        remove_keyboard();
    }
    //cái này dùng để ẩn bàn phim sau khi gõ xong
    private void remove_keyboard(){
        View view=this.getCurrentFocus();
        if(view != null){
            android.view.inputmethod.InputMethodManager input=(android.view.inputmethod.InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void filter_product(String string) {

        if (list_filtered == null) {
            list_filtered = new ArrayList<>();
        }//cá này kiểm tra xem list_filtered đã có chưa, chưa thì khởi tạo một
        // cái để chứa kết quả sau khi lọc

        list_filtered.clear();
        if (string.isEmpty()) {
            list_filtered.addAll(productList);//hiển thị all sản phẩm
        } else {
            //lọc sản phẩm
            String search_lower_case = string.toLowerCase().trim();
            for (Product product : productList) {
                String name_product = product.getName().toLowerCase();
                if (name_product.contains(search_lower_case)) {
                    list_filtered.add(product);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();//cập nhật gridview
        }
    }

    //  MENU KHÁCH HÀNG
    private void setupBottomMenu() {
        // Gắn ID
        item_Home = findViewById(R.id.item_Home_customer);
        item_Cart = findViewById(R.id.item_cart_customer);
        item_Order = findViewById(R.id.item_order_customer);
        item_Account = findViewById(R.id.item_account_customer);

        text_Home = findViewById(R.id.text_Home_customer);
        text_Cart = findViewById(R.id.text_cart_customer);
        text_Order = findViewById(R.id.text_order_customer);
        text_Account = findViewById(R.id.text_account_customer);

        // click các icon ở thanh menu - THÊM TRUYỀN INTENT
        item_Home.setOnClickListener(v -> {
            selectMenu(item_Home);
            // Không cần startActivity vì đang ở Home
        });

        item_Cart.setOnClickListener(v -> {
            selectMenu(item_Cart);
            Intent intent = new Intent(Home.this, Cart_activity.class);
            intent.putExtra("SELECTED_MENU", "CART"); // TRUYỀN MENU ĐANG CHỌN
            startActivity(intent);
        });

        item_Order.setOnClickListener(v -> {
            selectMenu(item_Order);
            Intent intent = new Intent(Home.this, Order_Activity.class);
            intent.putExtra("SELECTED_MENU", "ORDER");
           startActivity(intent);});

        item_Account.setOnClickListener(v -> {
            selectMenu(item_Account);
            Intent intent = new Intent(Home.this, Account.class);
            intent.putExtra("SELECTED_MENU", "ACCOUNT");
            startActivity(intent);
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
        if (selectedItem == item_Cart) text_Cart.setVisibility(View.VISIBLE);
        if (selectedItem == item_Order) text_Order.setVisibility(View.VISIBLE);
        if (selectedItem == item_Account) text_Account.setVisibility(View.VISIBLE);
    }

    private void resetMenu() {
        // Reset nền
        item_Home.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Cart.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Order.setBackgroundResource(R.drawable.bg_menu_customer);
        item_Account.setBackgroundResource(R.drawable.bg_menu_customer);

        // Ẩn text
        text_Home.setVisibility(View.GONE);
        text_Cart.setVisibility(View.GONE);
        text_Order.setVisibility(View.GONE);
        text_Account.setVisibility(View.GONE);

    }

    // ===================== IMAGE SLIDER =====================
    private void setupImageSlider() {
        recyclerViewHorizontal = findViewById(R.id.recyclerViewHorizontal);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        );
        recyclerViewHorizontal.setLayoutManager(layoutManager);

        // Giúp vuốt dừng chính xác ở từng ảnh
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerViewHorizontal);

        recyclerViewHorizontal.setHasFixedSize(true);
        recyclerViewHorizontal.setClipToPadding(false);
        recyclerViewHorizontal.setClipChildren(false);
        recyclerViewHorizontal.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        // Tạo padding hai bên để ảnh trung tâm nổi bật hơn
        int padding = getResources().getDimensionPixelSize(R.dimen.slider_side_padding);
        recyclerViewHorizontal.setPadding(padding, 0, padding, 0);

        imageList = Arrays.asList(
                R.drawable.rau_cu_1,
                R.drawable.rau_cu_2,
                R.drawable.rau_cu_3
        );

        imageSliderAdapter = new ImageSliderAdapter(imageList);
        recyclerViewHorizontal.setAdapter(imageSliderAdapter);

        recyclerViewHorizontal.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                float midpoint = recyclerView.getWidth() / 2.0f;
                for (int i = 0; i < recyclerView.getChildCount(); i++) {
                    float childMidpoint = (recyclerView.getChildAt(i).getLeft() + recyclerView.getChildAt(i).getRight()) / 2.0f;
                    float d = Math.min(1f, Math.abs(midpoint - childMidpoint) / midpoint);
                    float scale = 1 - 0.2f * d; // giảm 20% khi xa tâm
                    recyclerView.getChildAt(i).setScaleX(scale);
                    recyclerView.getChildAt(i).setScaleY(scale);
                    recyclerView.getChildAt(i).setAlpha(0.7f + (1 - d) * 0.3f); // mờ dần ở rìa
                }

                // Cập nhật currentPage khi người dùng scroll thủ công
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
                    if (firstVisiblePosition != RecyclerView.NO_POSITION) {
                        currentPage = firstVisiblePosition;
                    }
                }
            }
        });

        // Đặt vị trí bắt đầu (vòng lặp vô hạn)
        currentPage = 1000; // Bắt đầu từ vị trí an toàn
        recyclerViewHorizontal.scrollToPosition(currentPage);

        // Khởi động slider tự động với smooth scroll
        startSmoothAutoSlider();
    }

    private void startSmoothAutoSlider() {
        // Dừng timer cũ nếu có
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }

        final Handler handler = new Handler();
        sliderTimer = new Timer();

        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> {
                    if (recyclerViewHorizontal != null && imageSliderAdapter != null) {
                        currentPage++;

                        // Sử dụng smoothScrollToPosition để chuyển ảnh mượt mà
                        recyclerViewHorizontal.smoothScrollToPosition(currentPage);

                        // Debug log
                        int actualImage = (currentPage % imageList.size()) + 1;
                        Log.d("AutoSlider", "Chuyển đến ảnh: " + actualImage);
                    }
                });
            }
        }, SLIDER_DELAY, SLIDER_DELAY); // Mỗi 3 giây chuyển ảnh
    }

    // ===================== GRIDVIEW =====================
    private void initVegetableData() {
        productList = new ArrayList<>();
        productList.add(new Product("Rau muống", "10.000 VND / bó", R.drawable.rau_muong));
        productList.add(new Product("Bắp cải", "17.000 VND / bó", R.drawable.bap_cai));
        productList.add(new Product("Rau cải", "15.000 VND / bó", R.drawable.rau_cai));
        productList.add(new Product("Dưa chuột", "19.000 VND / kg", R.drawable.dua_chuot));
        productList.add(new Product("Bắp cải", "17.000 VND / bó", R.drawable.bap_cai));
        productList.add(new Product("Bắp cải", "17.000 VND / bó", R.drawable.bap_cai));

        list_filtered=new ArrayList<>(productList);
    }

    private void setupGridView() {
        adapter = new VegetableAdapter(this, list_filtered);

        adapter.setOnItemClickListener(new VegetableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Product product) {
                openProductDetail(product);
            }
        });

        gvDsThucPham.setAdapter(adapter);
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(Home.this, Product_info.class);
        intent.putExtra("PRODUCT", product);
        startActivity(intent);
    }
    // ===================== QUẢN LÝ VÒNG ĐỜI =====================
    @Override
    protected void onPause() {
        super.onPause();
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageSliderAdapter != null && sliderTimer == null) {
            startSmoothAutoSlider();
        }
        //xóa hết text trong search view nếu ấn back
        if (search_bar != null) {
            search_bar.setText("");
            search_bar.clearFocus();
            remove_keyboard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }
    }
}

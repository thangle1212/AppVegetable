package com.example.appvegetables.Cart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appvegetables.R;
import com.example.appvegetables.database.Cart_database_activity;

import java.util.ArrayList;

public class Cart_adapter extends RecyclerView.Adapter<Cart_adapter.CartViewHolder> {
    private final Context context;
    private ArrayList<ItemCart> itemCarts;
    private final OnCartChangeListener listener;

    public Cart_adapter(Context context, ArrayList<ItemCart> itemCarts, OnCartChangeListener listener) {
        this.context = context;
        this.itemCarts = itemCarts;
        this.listener = listener;
    }

    public interface OnCartChangeListener {
        void onCartUpdated();
        void onItemRemoved(int position);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txt_name, txt_gia, txt_so_luong_grid_cart;
        ImageButton btn_add, btn_remove, btn_delete_product_cart;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            txt_name = itemView.findViewById(R.id.txt_product_name);
            txt_gia = itemView.findViewById(R.id.txt_product_price);
            txt_so_luong_grid_cart = itemView.findViewById(R.id.txt_so_luong_grid_cart);

            btn_add = itemView.findViewById(R.id.btn_add);
            btn_remove = itemView.findViewById(R.id.btn_remove);
            btn_delete_product_cart = itemView.findViewById(R.id.btn_delete_product_cart);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gridview_vegetable_cart, parent, false);
        return new CartViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        ItemCart item = itemCarts.get(position);

        // Hiển thị thông tin
        holder.imgProduct.setImageResource(item.getHinh_anh_san_pham());
        holder.txt_name.setText(item.getName());
        holder.txt_so_luong_grid_cart.setText(String.valueOf(item.getSo_luong_grid_view()));
        holder.txt_gia.setText(item.getGia());

        // Xử lý tăng/giảm số lượng - ĐƠN GIẢN
        holder.btn_add.setOnClickListener(v -> updateQuantity(position, 1));
        holder.btn_remove.setOnClickListener(v -> updateQuantity(position, -1));
        holder.btn_delete_product_cart.setOnClickListener(v -> remove_item(position));
    }

    private void updateQuantity(int position, int change) {
        ItemCart item = itemCarts.get(position);
        int newQuantity = item.getSo_luong_grid_view() + change;

        if (newQuantity < 1) return; // Không cho nhỏ hơn 1

        // ✅ Cập nhật trong object hiện tại
        item.setSo_luong(newQuantity);

        // ✅ Cập nhật hiển thị số lượng
        notifyItemChanged(position);

        // ✅ Ghi xuống database thật
        Cart_database_activity db = new Cart_database_activity(context);
        db.updateQuantity(item.getName(), newQuantity);

        // ✅ Cập nhật lại trong CartManager (bộ nhớ tạm)
        for (ItemCart it : CartManager.getInstance().getCart()) {
            if (it.getName().equals(item.getName())) {
                it.setSo_luong(newQuantity);
                break;
            }
        }

        if (listener != null) listener.onCartUpdated();
    }


    private void updateCartManager(String productName, int newQuantity) {
        ArrayList<ItemCart> managerItems = CartManager.getInstance().getCart();
        for (ItemCart managerItem : managerItems) {
            if (managerItem.getName().equals(productName)) {
                managerItem.setSo_luong(newQuantity);
                return;
            }
        }

        Cart_database_activity Cda = new Cart_database_activity(context);
        ArrayList<ItemCart> dbItems = Cda.getAllCartItems();
        CartManager.getInstance().getCart().clear();
        CartManager.getInstance().getCart().addAll(dbItems);
    }

    private void remove_item(int position) {
        if (position >= 0 && position < itemCarts.size()) {
            ItemCart remove_item = itemCarts.get(position);

            Cart_database_activity Cda = new Cart_database_activity(context);
            Cda.deleteItem(remove_item.getName());

            removeFromCartManager(remove_item.getName());

            itemCarts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemCarts.size());

            if (listener != null) {
                listener.onItemRemoved(position);
                listener.onCartUpdated();
            }
            Toast.makeText(context, "Đã xóa " + remove_item.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFromCartManager(String productName) {
        ArrayList<ItemCart> managerItems = CartManager.getInstance().getCart();
        for (int i = 0; i < managerItems.size(); i++) {
            if (managerItems.get(i).getName().equals(productName)) {
                managerItems.remove(i);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemCarts.size();
    }

    public double getTotalPrice() {
        double total = 0;
        for (ItemCart item : itemCarts) {
            total += calculateItemTotal(item);
        }
        return total;
    }

    private double calculateItemTotal(ItemCart item) {
        try {
            String cleanPrice = item.getGia().replaceAll("[^0-9]", "");
            if (!cleanPrice.isEmpty()) {
                return Double.parseDouble(cleanPrice) * item.getSo_luong_grid_view();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void refreshData(ArrayList<ItemCart> newData) {
        this.itemCarts = newData;
        notifyDataSetChanged();
    }
}
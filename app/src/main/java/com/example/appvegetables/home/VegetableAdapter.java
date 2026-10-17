package com.example.appvegetables.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appvegetables.Cart.CartManager;
import com.example.appvegetables.Cart.ItemCart;
import com.example.appvegetables.R;
import com.example.appvegetables.database.Cart_database_activity;

import java.util.List;

public class VegetableAdapter extends BaseAdapter {

    private final Context context;
    private final List<Product> list_filtered;

    public VegetableAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.list_filtered = productList;
    }

    @Override
    public int getCount() {
        return list_filtered.size();
    }

    @Override
    public Object getItem(int position) {
        return list_filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private OnItemClickListener itemClickListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_gridview_vegetable, parent, false);
            holder = new ViewHolder();
            holder.imgVegetable = convertView.findViewById(R.id.img_rau_muong);
            holder.txtName = convertView.findViewById(R.id.txt_ten_rau_muong);
            holder.txtPrice = convertView.findViewById(R.id.txt_gia);
            holder.btnAddShopping = convertView.findViewById(R.id.btn_add_shopping);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Product veg = list_filtered.get(position);
        holder.imgVegetable.setImageResource(veg.getImageResId());
        holder.txtName.setText(veg.getName());
        holder.txtPrice.setText(veg.getPrice());

        convertView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(veg);
            }
        });

        holder.btnAddShopping.setOnClickListener(v -> {
            ItemCart newItem = new ItemCart(
                    veg.getName(),
                    veg.getPrice(),
                    veg.getImageResId(),
                    1
            );

            // Thêm vào database
            Cart_database_activity cartDb = new Cart_database_activity(context);
            long result = cartDb.addToCart(newItem);

            // Thêm vào CartManager
            CartManager.getInstance().addItem(newItem);

            if (result > 0) {
                Toast.makeText(context, "Đã thêm " + veg.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView imgVegetable;
        TextView txtName, txtPrice;
        Button btnAddShopping;
    }
}
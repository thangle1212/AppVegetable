package com.example.appvegetables.Order;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appvegetables.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Order_model> list;

    public OrderAdapter(Context context, ArrayList<Order_model> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order_model order = list.get(position);

        holder.txtOrderId.setText("Mã đơn: #" + order.getOrderId());
        holder.txtCustomerName.setText(order.getCustomerName());
        holder.txtProductList.setText(order.getProductList());
        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(order.getTotalPrice()) + " VND";
        holder.txtTotalPrice.setText(formattedPrice);
        holder.txtStatus.setText(order.getStatus());

        holder.itemView.setOnClickListener(v -> {
            Log.d("ORDER_CLICK", "Order ID click: " + order.getOrderId());

            Intent intent = new Intent(context, Order_don_dat.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            ((Activity) context).startActivityForResult(intent, 1001);
        });

        // 🎨 Đổi màu trạng thái theo loại đơn hàng
        switch (order.getStatus()) {
            case "Đã đặt":
                holder.txtStatus.setTextColor(Color.parseColor("#2196F3")); // xanh dương
                break;
            case "Đang giao":
                holder.txtStatus.setTextColor(Color.parseColor("#FF9800")); // cam
                break;
            case "Hoàn thành":
                holder.txtStatus.setTextColor(Color.parseColor("#4CAF50")); // xanh lá
                break;
            case "Đã hủy":
                holder.txtStatus.setTextColor(Color.parseColor("#F44336")); // đỏ
                break;
            default:
                holder.txtStatus.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtCustomerName, txtProductList, txtTotalPrice, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txt_order_id);
            txtCustomerName = itemView.findViewById(R.id.txt_customer_name);
            txtProductList = itemView.findViewById(R.id.txt_product_list);
            txtTotalPrice = itemView.findViewById(R.id.txt_total_price);
            txtStatus = itemView.findViewById(R.id.txt_status_order);
        }
    }
}

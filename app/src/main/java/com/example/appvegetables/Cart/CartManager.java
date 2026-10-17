package com.example.appvegetables.Cart;


import java.util.ArrayList;

public class CartManager {
    private static CartManager instance;
    private final ArrayList<ItemCart> cartList = new ArrayList<>();

    private CartManager() {// khai báo ở dòng 8 trên rồi
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public ArrayList<ItemCart> getCart() {
        return cartList;
    }

    public void addItem(ItemCart newItem) {
        boolean found = false;
        for (ItemCart item : cartList) {
            if (item.getName().equals(newItem.getName())) {
                item.setSo_luong(item.getSo_luong_grid_view() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            newItem.setSo_luong(1);
            cartList.add(newItem);
        }
    }

    public void remove_item(int position){
        if(position >=0 && position < cartList.size()){
            cartList.remove(position);
        }
    }
    public void remove_item(ItemCart itemCart){
        cartList.remove(cartList);
    }

    public void clearCart() {
        cartList.clear();
    }

    public double getTotalPrice() {
        double total = 0;
        for (ItemCart item : cartList) {
            try {
                String cleanPrice = item.getGia().replaceAll("[^0-9]", "");
                if (!cleanPrice.isEmpty()) {
                    total += Double.parseDouble(cleanPrice) * item.getSo_luong_grid_view();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return total;
    }
}

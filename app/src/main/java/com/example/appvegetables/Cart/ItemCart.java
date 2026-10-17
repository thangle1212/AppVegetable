package com.example.appvegetables.Cart;

import java.io.Serializable;

public class ItemCart implements Serializable {
    private String name;
    private String gia;
    private int hinh_anh_san_pham;
    private int so_luong_grid_view;

    public ItemCart(String name, String gia, int hinh_anh_san_pham, int so_luong_grid_view) {
        this.name = name;
        this.gia = gia;
        this.hinh_anh_san_pham = hinh_anh_san_pham;
        this.so_luong_grid_view = so_luong_grid_view;
    }
    public ItemCart(String name, String gia, int hinh_anh_san_pham) {
        this(name, gia, hinh_anh_san_pham, 1);
    }

    // Getter - Setter
    public String getName() {
        return name;
    }

    public String getGia() {
        return gia;
    }

    public int getHinh_anh_san_pham() {
        return hinh_anh_san_pham;
    }

    public int getSo_luong_grid_view() {
        return so_luong_grid_view;
    }

    public void setSo_luong(int so_luong) {
        this.so_luong_grid_view = so_luong;
    }
}

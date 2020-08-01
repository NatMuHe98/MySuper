package com.example.mysuper.models;

import java.io.Serializable;

public class ProductoModel implements Serializable {
    String id;
    String cantidad;
    String producto;
    String precio;
    String total;

    public ProductoModel() {
    }

    public ProductoModel(String id, String cantidad, String producto, String precio, String total) {
        this.id = id;
        this.cantidad = cantidad;
        this.producto = producto;
        this.precio = precio;
        this.total = total;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}

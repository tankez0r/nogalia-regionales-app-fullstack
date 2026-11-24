package com.nogalina.regionales;

import java.io.Serializable;

public class Producto implements Serializable {
    public Integer id;
    public String nombre;
    public String categoria;
    public double precio;
    public int stock;
    public int stockMinimo;
    public String unidad;
    public String origen;
    public String descripcion;

    public boolean bajoStock() {
        return stock <= stockMinimo;
    }
}

package com.nogalina.regionales;

import java.util.List;

public class Venta {
    public int id;
    public String fecha;
    public double total;
    public String estado;
    public List<Item> items;

    public boolean anulada() {
        return "anulada".equals(estado);
    }

    public static class Item {
        public Integer productoId;
        public String productoNombre;
        public int cantidad;
        public double precioUnitario;

        public Item(int productoId, int cantidad) {
            this.productoId = productoId;
            this.cantidad = cantidad;
        }
    }

    public static class Nueva {
        public List<Item> items;

        public Nueva(List<Item> items) {
            this.items = items;
        }
    }

    public static class Respuesta {
        public Venta venta;
        public String advertencia;
    }
}

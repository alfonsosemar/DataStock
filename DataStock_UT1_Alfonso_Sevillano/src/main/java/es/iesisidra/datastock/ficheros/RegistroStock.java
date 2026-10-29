package es.iesisidra.datastock.ficheros;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Un registro leído de stock.bin: código, precio y stock. */
public class RegistroStock {

    private final String codigo;
    private final double precio;
    private final int stock;

    public RegistroStock(String codigo, double precio, int stock) {
        this.codigo = codigo;
        this.precio = precio;
        this.stock = stock;
    }

    public String getCodigo() { return codigo; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }

    @Override
    public String toString() {
        return codigo + " | " + BigDecimal.valueOf(precio).setScale(2, RoundingMode.HALF_UP).toPlainString() + " € | stock " + stock;
    }
}

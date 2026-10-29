package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Reglas comunes de los productos (apartado 6 del enunciado).
 * JSON, XML y .dat usan estas mismas reglas, así que el resultado es equivalente.
 */
public final class ValidadorProducto {

    private static final int MAX_CODIGO = 12;
    private static final int MIN_NOMBRE = 3;
    private static final int MAX_NOMBRE = 100;

    private ValidadorProducto() { }

    /**
     * Comprueba los seis campos recibidos como texto (null si faltan).
     * Devuelve la lista de errores; si está vacía, el producto es correcto.
     */
    public static List<String> validar(String codigo, String nombre, String precio,
                                       String stock, String categoriaId, String proveedorId) {
        List<String> errores = new ArrayList<>();

        // Código: obligatorio, máximo 12 caracteres y patrón [A-Z0-9-]+
        if (codigo == null || codigo.trim().isEmpty()) {
            errores.add("el código es obligatorio");
        } else {
            String c = codigo.trim();
            if (c.length() > MAX_CODIGO) {
                errores.add("el código no puede superar los 12 caracteres");
            }
            if (!c.matches("[A-Z0-9-]+")) {
                errores.add("el código solo admite mayúsculas, números y guiones");
            }
        }

        // Nombre: entre 3 y 100 caracteres
        if (nombre == null || nombre.trim().length() < MIN_NOMBRE || nombre.trim().length() > MAX_NOMBRE) {
            errores.add("el nombre debe tener entre 3 y 100 caracteres");
        }

        // Precio: número decimal mayor que 0
        if (precio == null || precio.trim().isEmpty()) {
            errores.add("el precio es obligatorio");
        } else {
            try {
                BigDecimal valor = new BigDecimal(precio.trim());
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    errores.add("el precio debe ser mayor que 0");
                }
            } catch (NumberFormatException e) {
                errores.add("el precio no es un número válido (" + precio.trim() + ")");
            }
        }

        // Stock: entero mayor o igual que 0
        if (stock == null || stock.trim().isEmpty()) {
            errores.add("el stock es obligatorio");
        } else {
            try {
                if (Integer.parseInt(stock.trim()) < 0) {
                    errores.add("el stock no puede ser negativo");
                }
            } catch (NumberFormatException e) {
                errores.add("el stock debe ser un número entero (" + stock.trim() + ")");
            }
        }

        // Identificadores: enteros positivos
        validarIdPositivo(categoriaId, "categoriaId", errores);
        validarIdPositivo(proveedorId, "proveedorId", errores);

        return errores;
    }

    /** Igual que el método anterior, pero para un Producto ya creado (por ejemplo, el leído de un .dat). */
    public static List<String> validar(Producto p) {
        String precio = null;
        if (p.getPrecio() != null) {
            precio = p.getPrecio().toPlainString();
        }
        return validar(p.getCodigo(), p.getNombre(), precio, String.valueOf(p.getStock()),
                String.valueOf(p.getCategoriaId()), String.valueOf(p.getProveedorId()));
    }

    /** Crea el Producto. Solo debe llamarse cuando validar(...) no ha devuelto errores. */
    public static Producto crearProducto(String codigo, String nombre, String precio,
                                         String stock, String categoriaId, String proveedorId) {
        return new Producto(codigo.trim(), nombre.trim(), new BigDecimal(precio.trim()),
                Integer.parseInt(stock.trim()), Integer.parseInt(categoriaId.trim()),
                Integer.parseInt(proveedorId.trim()));
    }

    private static void validarIdPositivo(String valor, String campo, List<String> errores) {
        if (valor == null || valor.trim().isEmpty()) {
            errores.add(campo + " es obligatorio");
            return;
        }
        try {
            if (Integer.parseInt(valor.trim()) <= 0) {
                errores.add(campo + " debe ser un entero positivo");
            }
        } catch (NumberFormatException e) {
            errores.add(campo + " debe ser un número entero (" + valor.trim() + ")");
        }
    }
}
package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Reglas comunes de los productos.
 * <p>JSON, XML, .dat (UT1) y los menús de MySQL (UT2) usan estas mismas reglas,
 * que coinciden con las restricciones CHECK de la tabla {@code producto}.
 * Cada método {@code errorXxx} devuelve el motivo del error o null si el dato
 * es válido; así el menú puede volver a pedir solo el campo incorrecto.</p>
 */
public final class ValidadorProducto {

    private static final int MAX_CODIGO = 12;
    private static final int MIN_NOMBRE = 3;
    private static final int MAX_NOMBRE = 100;
    private static final int MAX_DECIMALES = 2;
    // DECIMAL(10,2): como mucho 8 cifras enteras y 2 decimales.
    private static final BigDecimal PRECIO_MAXIMO = new BigDecimal("99999999.99");

    private ValidadorProducto() { }

    /**
     * Comprueba los seis campos recibidos como texto (null si faltan).
     * Devuelve la lista de errores; si está vacía, el producto es correcto.
     */
    public static List<String> validar(String codigo, String nombre, String precio,
                                       String stock, String categoriaId, String proveedorId) {
        List<String> errores = new ArrayList<>();
        anadirSiHayError(errores, errorCodigo(codigo));
        anadirSiHayError(errores, errorNombre(nombre));
        anadirSiHayError(errores, errorPrecio(precio));
        anadirSiHayError(errores, errorStock(stock));
        anadirSiHayError(errores, errorId(categoriaId, "categoriaId"));
        anadirSiHayError(errores, errorId(proveedorId, "proveedorId"));
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

    /** Quita espacios y pasa a mayúsculas (tec-001 → TEC-001). Devuelve "" si es null. */
    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    /** Código: 1 a 12 caracteres A-Z, 0-9 o guion. */
    public static String errorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return "el código es obligatorio";
        }
        String c = codigo.trim();
        if (c.length() > MAX_CODIGO) {
            return "el código no puede superar los 12 caracteres";
        }
        if (!c.matches("[A-Z0-9-]+")) {
            return "el código solo admite mayúsculas, números y guiones";
        }
        return null;
    }

    /** Nombre: entre 3 y 100 caracteres sin contar los espacios de los extremos. */
    public static String errorNombre(String nombre) {
        if (nombre == null || nombre.trim().length() < MIN_NOMBRE || nombre.trim().length() > MAX_NOMBRE) {
            return "el nombre debe tener entre 3 y 100 caracteres";
        }
        return null;
    }

    /** Precio: número mayor que 0, con 2 decimales como máximo y dentro de DECIMAL(10,2). */
    public static String errorPrecio(String precio) {
        if (precio == null || precio.trim().isEmpty()) {
            return "el precio es obligatorio";
        }
        BigDecimal valor;
        try {
            valor = new BigDecimal(precio.trim());
        } catch (NumberFormatException e) {
            return "el precio no es un número válido (" + precio.trim() + ")";
        }
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            return "el precio debe ser mayor que 0";
        }
        if (valor.stripTrailingZeros().scale() > MAX_DECIMALES) {
            return "el precio admite como máximo dos decimales";
        }
        if (valor.compareTo(PRECIO_MAXIMO) > 0) {
            return "el precio supera el máximo admitido (99999999.99)";
        }
        return null;
    }

    /** Stock: entero mayor o igual que 0. */
    public static String errorStock(String stock) {
        if (stock == null || stock.trim().isEmpty()) {
            return "el stock es obligatorio";
        }
        try {
            if (Integer.parseInt(stock.trim()) < 0) {
                return "el stock no puede ser negativo";
            }
        } catch (NumberFormatException e) {
            return "el stock debe ser un número entero (" + stock.trim() + ")";
        }
        return null;
    }

    /** Identificador de categoría o proveedor: entero positivo. */
    public static String errorId(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            return campo + " es obligatorio";
        }
        try {
            if (Integer.parseInt(valor.trim()) <= 0) {
                return campo + " debe ser un entero positivo";
            }
        } catch (NumberFormatException e) {
            return campo + " debe ser un número entero (" + valor.trim() + ")";
        }
        return null;
    }

    private static void anadirSiHayError(List<String> errores, String error) {
        if (error != null) {
            errores.add(error);
        }
    }
}

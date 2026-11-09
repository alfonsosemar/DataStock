package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.bd.ProductoBD;
import es.iesisidra.datastock.modelo.FichaProducto;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ValidadorProducto;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Presentación de productos de MySQL, compartida por los menús 7.4, 7.5 y 7.6.
 */
final class VistaProducto {

    private VistaProducto() { }

    /** Muestra todos los datos de un producto. */
    static void mostrarFicha(FichaProducto ficha) {
        Producto p = ficha.getProducto();
        System.out.println("Código:    " + p.getCodigo());
        System.out.println("Nombre:    " + p.getNombre());
        System.out.println("Precio:    " + p.getPrecio().toPlainString() + " €");
        System.out.println("Stock:     " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " - " + ficha.getCategoriaNombre());
        System.out.println("Proveedor: " + p.getProveedorId() + " - " + ficha.getProveedorNombre());
    }

    /** Muestra una tabla con encabezados, o el aviso indicado si la lista está vacía. */
    static void mostrarTabla(List<FichaProducto> fichas, String avisoSiVacia) {
        if (fichas.isEmpty()) {
            Mensajes.aviso(avisoSiVacia);
            return;
        }
        String formato = "%-12s %-28s %10s %6s  %-18s %s%n";
        System.out.printf(formato, "CÓDIGO", "NOMBRE", "PRECIO €", "STOCK", "CATEGORÍA", "PROVEEDOR");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (FichaProducto f : fichas) {
            Producto p = f.getProducto();
            System.out.printf(formato, p.getCodigo(), p.getNombre(), p.getPrecio().toPlainString(),
                    p.getStock(), f.getCategoriaNombre(), f.getProveedorNombre());
        }
        System.out.println("Total de productos encontrados: " + fichas.size());
    }

    /**
     * Pide un código, lo normaliza (mayúsculas, sin espacios) y busca el producto.
     * @return la ficha, o null si se cancela o no existe (ya se ha avisado al usuario).
     */
    static FichaProducto pedirExistente() throws SQLException {
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código del producto" + EntradaDatos.CANCELAR));
        if (codigo.isEmpty()) {
            Mensajes.aviso("Operación cancelada.");
            return null;
        }
        FichaProducto ficha = ProductoBD.buscar(codigo);
        if (ficha == null) {
            Mensajes.aviso("No existe ningún producto con el código " + codigo + ".");
        }
        return ficha;
    }
}

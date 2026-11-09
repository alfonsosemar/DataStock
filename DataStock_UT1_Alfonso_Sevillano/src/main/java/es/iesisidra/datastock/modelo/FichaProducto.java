package es.iesisidra.datastock.modelo;

/**
 * DTO con un producto de MySQL y los nombres de su categoría y proveedor.
 * <p>Se construye a partir de una fila del ResultSet de una consulta con JOIN,
 * para no mostrar la fila como valores sueltos sin identificar (RF-JDBC-04).</p>
 */
public class FichaProducto {
    private final Producto producto;
    private final String categoriaNombre;
    private final String proveedorNombre;

    public FichaProducto(Producto producto, String categoriaNombre, String proveedorNombre) {
        this.producto = producto;
        this.categoriaNombre = categoriaNombre;
        this.proveedorNombre = proveedorNombre;
    }

    /** @return datos de la tabla producto. */
    public Producto getProducto() { return producto; }
    /** @return nombre de la categoría (tabla categoria). */
    public String getCategoriaNombre() { return categoriaNombre; }
    /** @return nombre del proveedor (tabla proveedor). */
    public String getProveedorNombre() { return proveedorNombre; }
}

package es.iesisidra.datastock.bd;

/**
 * Datos obtenidos al comprobar la conexión (RF-JDBC-01).
 */
public class InfoConexion {
    private final String baseDatos;
    private final String gestor;
    private final String version;
    private final String driver;
    private final int productos;
    private final boolean procedimientoDisponible;

    public InfoConexion(String baseDatos, String gestor, String version, String driver,
                        int productos, boolean procedimientoDisponible) {
        this.baseDatos = baseDatos;
        this.gestor = gestor;
        this.version = version;
        this.driver = driver;
        this.productos = productos;
        this.procedimientoDisponible = procedimientoDisponible;
    }

    /** @return nombre del esquema al que se ha conectado. */
    public String getBaseDatos() { return baseDatos; }
    /** @return nombre del gestor (MySQL). */
    public String getGestor() { return gestor; }
    /** @return versión del servidor MySQL. */
    public String getVersion() { return version; }
    /** @return nombre y versión del driver JDBC. */
    public String getDriver() { return driver; }
    /** @return número de filas de la tabla producto. */
    public int getProductos() { return productos; }
    /** @return true si existe el procedimiento registrar_movimiento. */
    public boolean isProcedimientoDisponible() { return procedimientoDisponible; }
}

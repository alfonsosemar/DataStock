package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Copia de productos del inventario en memoria (UT1) a MySQL (RF-JDBC-07).
 * <p>Las comprobaciones previas se hacen producto a producto. Después, todos los
 * productos aceptados se insertan en UNA sola transacción JDBC: o se guardan
 * todos, o ninguno.</p>
 */
public final class ImportacionBD {

    private ImportacionBD() { }

    /**
     * Comprueba que la categoría y el proveedor del producto existen en MySQL.
     * No crea nada automáticamente.
     * @return el motivo del rechazo, o null si ambas referencias existen.
     */
    public static String motivoReferencias(Producto p) throws SQLException {
        boolean hayCategoria = CategoriaBD.buscar(p.getCategoriaId()) != null;
        boolean hayProveedor = ProveedorBD.buscar(p.getProveedorId()) != null;
        if (!hayCategoria && !hayProveedor) {
            return "No existen la categoría con ID " + p.getCategoriaId()
                    + " ni el proveedor con ID " + p.getProveedorId() + ".";
        }
        if (!hayCategoria) {
            return "No existe la categoría con ID " + p.getCategoriaId() + ".";
        }
        if (!hayProveedor) {
            return "No existe el proveedor con ID " + p.getProveedorId() + ".";
        }
        return null;
    }

    /**
     * Inserta el lote en una transacción: setAutoCommit(false), un INSERT por
     * producto y commit() al final. Si cualquier INSERT falla se hace rollback()
     * y no queda guardado ninguno del lote.
     * @return número de productos insertados.
     * @throws SQLException el error que provocó el rollback.
     */
    public static int insertarLote(List<Producto> productos) throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                int insertados = 0;
                for (Producto p : productos) {
                    insertados += ProductoBD.insertar(con, p);
                }
                con.commit();
                return insertados;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                // Se restaura el modo por defecto antes de cerrar la conexión.
                con.setAutoCommit(true);
            }
        }
    }
}

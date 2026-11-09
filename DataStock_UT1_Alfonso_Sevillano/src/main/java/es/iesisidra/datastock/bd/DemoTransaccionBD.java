package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

/**
 * Demostración de commit y rollback desde Java (RF-JDBC-08).
 * <p>Usa siempre las categorías de prueba 9001, 9002 y 9003, cuyos nombres
 * empiezan por "Demo transacción". No toca ningún dato real.</p>
 */
public final class DemoTransaccionBD {

    public static final int ID_A1 = 9001;
    public static final int ID_A2 = 9002;
    public static final int ID_B = 9003;
    public static final String PREFIJO_NOMBRE = "Demo transacción ";

    private DemoTransaccionBD() { }

    /**
     * Escenario A: inserta dos categorías en una transacción y hace commit().
     * Si algo falla se hace rollback() y se relanza el error.
     */
    public static void escenarioCommit() throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                CategoriaBD.insertar(con, new Categoria(ID_A1, PREFIJO_NOMBRE + ID_A1));
                CategoriaBD.insertar(con, new Categoria(ID_A2, PREFIJO_NOMBRE + ID_A2));
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Escenario B: inserta una categoría y la vuelve a insertar a propósito para
     * provocar un error de clave duplicada. Se captura el error y se hace
     * rollback(), que deshace también la primera inserción.
     * @return el error capturado, o null si no hubo error.
     */
    public static SQLException escenarioRollback() throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                CategoriaBD.insertar(con, new Categoria(ID_B, PREFIJO_NOMBRE + ID_B)); // correcta
                CategoriaBD.insertar(con, new Categoria(ID_B, PREFIJO_NOMBRE + ID_B)); // duplicada: error
                con.commit();
                return null;
            } catch (SQLException e) {
                con.rollback();
                return e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** @return true si la categoría existe en MySQL. */
    public static boolean existe(int id) throws SQLException {
        return CategoriaBD.buscar(id) != null;
    }

    /**
     * Borra las categorías de demostración (ids 9001 a 9003 con nombre "Demo transacción ...").
     * @return número de categorías borradas.
     */
    public static int eliminarCategoriasDemo() throws SQLException {
        String sql = "DELETE FROM categoria WHERE id BETWEEN ? AND ? AND nombre LIKE ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ID_A1);
            ps.setInt(2, ID_B);
            ps.setString(3, PREFIJO_NOMBRE + "%");
            return ps.executeUpdate();
        }
    }
}

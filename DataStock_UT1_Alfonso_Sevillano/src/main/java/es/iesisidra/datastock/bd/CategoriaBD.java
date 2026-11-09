package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Categoria;

/**
 * Operaciones JDBC sobre la tabla {@code categoria} (RF-JDBC-02).
 * <p>Todos los valores del usuario se pasan con PreparedStatement. Los métodos
 * que modifican datos devuelven las filas afectadas por executeUpdate().</p>
 */
public final class CategoriaBD {

    private CategoriaBD() { }

    /** @return la categoría con ese id, o null si no existe. */
    public static Categoria buscar(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM categoria WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(rs.getInt("id"), rs.getString("nombre"));
                }
                return null;
            }
        }
    }

    /** @return true si otra categoría (con id distinto de idExcluido) ya usa ese nombre. */
    public static boolean existeNombre(String nombre, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ? AND id <> ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** @return todas las categorías ordenadas por id. */
    public static List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT id, nombre FROM categoria ORDER BY id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return categorias;
    }

    /** @return filas insertadas (1 si todo va bien). */
    public static int insertar(Categoria c) throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            return insertar(con, c);
        }
    }

    /**
     * Inserta usando una conexión ya abierta. La usa la demostración de transacciones,
     * que necesita varias inserciones dentro de la misma transacción.
     */
    static int insertar(Connection con, Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (id, nombre) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getId());
            ps.setString(2, c.getNombre());
            return ps.executeUpdate();
        }
    }

    /** @return filas modificadas; 0 si el id no existía. */
    public static int cambiarNombre(int id, String nombre) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate();
        }
    }

    /**
     * @return filas borradas; 0 si el id no existía.
     * @throws SQLException con código 1451 si hay productos asociados.
     */
    public static int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}

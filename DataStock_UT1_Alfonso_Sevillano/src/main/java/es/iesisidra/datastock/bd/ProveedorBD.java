package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Proveedor;

/**
 * Operaciones JDBC sobre la tabla {@code proveedor} (RF-JDBC-02).
 * <p>El email es opcional: cuando no se indica se guarda NULL con setNull().</p>
 */
public final class ProveedorBD {

    private ProveedorBD() { }

    /** @return el proveedor con ese id, o null si no existe. */
    public static Proveedor buscar(int id) throws SQLException {
        String sql = "SELECT id, nombre, email FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerFila(rs);
                }
                return null;
            }
        }
    }

    /** @return true si otro proveedor (con id distinto de idExcluido) ya usa ese email. */
    public static boolean existeEmail(String email, int idExcluido) throws SQLException {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE email = ? AND id <> ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idExcluido);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** @return todos los proveedores ordenados por id. */
    public static List<Proveedor> listar() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT id, nombre, email FROM proveedor ORDER BY id";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(leerFila(rs));
            }
        }
        return proveedores;
    }

    /** @return filas insertadas (1 si todo va bien). */
    public static int insertar(Proveedor p) throws SQLException {
        String sql = "INSERT INTO proveedor (id, nombre, email) VALUES (?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNombre());
            asignarEmail(ps, 3, p.getEmail());
            return ps.executeUpdate();
        }
    }

    /** Actualiza nombre y email. @return filas modificadas; 0 si el id no existía. */
    public static int modificar(Proveedor p) throws SQLException {
        String sql = "UPDATE proveedor SET nombre = ?, email = ? WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            asignarEmail(ps, 2, p.getEmail());
            ps.setInt(3, p.getId());
            return ps.executeUpdate();
        }
    }

    /**
     * @return filas borradas; 0 si el id no existía.
     * @throws SQLException con código 1451 si hay productos asociados.
     */
    public static int eliminar(int id) throws SQLException {
        String sql = "DELETE FROM proveedor WHERE id = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    private static Proveedor leerFila(ResultSet rs) throws SQLException {
        return new Proveedor(rs.getInt("id"), rs.getString("nombre"), rs.getString("email"));
    }

    private static void asignarEmail(PreparedStatement ps, int posicion, String email) throws SQLException {
        if (email == null) {
            ps.setNull(posicion, Types.VARCHAR);
        } else {
            ps.setString(posicion, email);
        }
    }
}

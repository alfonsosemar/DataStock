package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.FichaProducto;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Operaciones JDBC sobre la tabla {@code producto} (RF-JDBC-03 y RF-JDBC-04).
 * <p>Las consultas usan JOIN con categoria y proveedor para devolver
 * {@link FichaProducto}. El precio se trata siempre como BigDecimal.</p>
 */
public final class ProductoBD {

    // Parte común de todas las consultas de productos; cada método añade su WHERE y ORDER BY.
    private static final String SELECT_FICHA =
            "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, "
            + "c.nombre AS categoria, v.nombre AS proveedor "
            + "FROM producto p "
            + "JOIN categoria c ON c.id = p.categoria_id "
            + "JOIN proveedor v ON v.id = p.proveedor_id ";

    private ProductoBD() { }

    /** @return la ficha del producto con ese código exacto, o null si no existe. */
    public static FichaProducto buscar(String codigo) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_FICHA + "WHERE p.codigo = ?")) {
            ps.setString(1, codigo);
            List<FichaProducto> fichas = leerFichas(ps);
            return fichas.isEmpty() ? null : fichas.get(0);
        }
    }

    /** @return todos los productos ordenados por código. */
    public static List<FichaProducto> listar() throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_FICHA + "ORDER BY p.codigo")) {
            return leerFichas(ps);
        }
    }

    /** @return productos cuyo nombre contiene el fragmento (LIKE, sin distinguir mayúsculas por la colación). */
    public static List<FichaProducto> buscarPorNombre(String fragmento) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_FICHA + "WHERE p.nombre LIKE ? ORDER BY p.codigo")) {
            // Los % se añaden al valor, no a la sentencia: el texto del usuario sigue siendo un parámetro.
            ps.setString(1, "%" + fragmento + "%");
            return leerFichas(ps);
        }
    }

    /** @return productos con stock estrictamente menor que el umbral, ordenados por stock y código. */
    public static List<FichaProducto> stockMenorQue(int umbral) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_FICHA + "WHERE p.stock < ? ORDER BY p.stock, p.codigo")) {
            ps.setInt(1, umbral);
            return leerFichas(ps);
        }
    }

    /** @return true si ya existe un producto con ese código. */
    public static boolean existe(String codigo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE codigo = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    /** @return filas insertadas (1 si todo va bien). */
    public static int insertar(Producto p) throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            return insertar(con, p);
        }
    }

    /**
     * Inserta usando una conexión ya abierta. La usa la importación desde memoria,
     * que necesita hacer todas las inserciones dentro de la misma transacción.
     */
    static int insertar(Connection con, Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, precio, stock, categoria_id, proveedor_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setBigDecimal(3, p.getPrecio());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getCategoriaId());
            ps.setInt(6, p.getProveedorId());
            return ps.executeUpdate();
        }
    }

    /**
     * Modifica nombre, precio, categoría y proveedor. El stock NO se toca aquí:
     * solo cambia mediante el procedimiento registrar_movimiento.
     * @return filas modificadas; 0 si el código no existía.
     */
    public static int modificar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET nombre = ?, precio = ?, categoria_id = ?, proveedor_id = ? WHERE codigo = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setBigDecimal(2, p.getPrecio());
            ps.setInt(3, p.getCategoriaId());
            ps.setInt(4, p.getProveedorId());
            ps.setString(5, p.getCodigo());
            return ps.executeUpdate();
        }
    }

    /**
     * @return filas borradas; 0 si el código no existía.
     * @throws SQLException con código 1451 si el producto tiene movimientos.
     */
    public static int eliminar(String codigo) throws SQLException {
        String sql = "DELETE FROM producto WHERE codigo = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate();
        }
    }

    // Ejecuta la consulta ya preparada y convierte cada fila del ResultSet en un objeto FichaProducto.
    private static List<FichaProducto> leerFichas(PreparedStatement ps) throws SQLException {
        List<FichaProducto> fichas = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(rs.getString("codigo"), rs.getString("nombre"),
                        rs.getBigDecimal("precio"), rs.getInt("stock"),
                        rs.getInt("categoria_id"), rs.getInt("proveedor_id"));
                fichas.add(new FichaProducto(p, rs.getString("categoria"), rs.getString("proveedor")));
            }
        }
        return fichas;
    }
}

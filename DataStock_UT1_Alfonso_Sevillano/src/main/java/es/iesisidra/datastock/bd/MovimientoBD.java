package es.iesisidra.datastock.bd;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.MovimientoStock.TipoMovimiento;

/**
 * Movimientos de stock (RF-JDBC-05) e historial (RF-JDBC-06).
 * <p>Las entradas y salidas se hacen SOLO con el procedimiento almacenado
 * {@code registrar_movimiento}, que ya incluye su propia transacción
 * (START TRANSACTION, SELECT ... FOR UPDATE, UPDATE, INSERT y COMMIT/ROLLBACK).
 * Por eso aquí no se abre otra transacción ni se actualiza producto.stock.</p>
 */
public final class MovimientoBD {

    private static final String SELECT_MOVIMIENTO =
            "SELECT id, producto_codigo, tipo, cantidad, fecha, stock_anterior, stock_posterior "
            + "FROM movimiento_stock ";

    private MovimientoBD() { }

    /**
     * Llama a registrar_movimiento(codigo, tipo, cantidad) con CallableStatement.
     * @return el movimiento que acaba de crear el procedimiento.
     * @throws SQLException con estado 45000 si el procedimiento lo rechaza
     *         (producto inexistente, cantidad no válida o stock insuficiente).
     */
    public static MovimientoStock registrar(String codigo, TipoMovimiento tipo, int cantidad) throws SQLException {
        try (Connection con = ConexionBD.conectar()) {
            try (CallableStatement cs = con.prepareCall("{CALL registrar_movimiento(?, ?, ?)}")) {
                cs.setString(1, codigo);
                cs.setString(2, tipo.name());
                cs.setInt(3, cantidad);
                cs.execute();
            }
            // Último movimiento del producto: el que acaba de insertar el procedimiento.
            String sql = SELECT_MOVIMIENTO + "WHERE producto_codigo = ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, codigo);
                List<MovimientoStock> ultimo = leerMovimientos(ps);
                return ultimo.get(0);
            }
        }
    }

    /** @return todos los movimientos ordenados por fecha e id. */
    public static List<MovimientoStock> listar() throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_MOVIMIENTO + "ORDER BY fecha, id")) {
            return leerMovimientos(ps);
        }
    }

    /** @return movimientos de un producto ordenados por fecha e id (lista vacía si no tiene). */
    public static List<MovimientoStock> listarPorProducto(String codigo) throws SQLException {
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(SELECT_MOVIMIENTO + "WHERE producto_codigo = ? ORDER BY fecha, id")) {
            ps.setString(1, codigo);
            return leerMovimientos(ps);
        }
    }

    // Convierte cada fila del ResultSet en un objeto MovimientoStock.
    private static List<MovimientoStock> leerMovimientos(PreparedStatement ps) throws SQLException {
        List<MovimientoStock> movimientos = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                movimientos.add(new MovimientoStock(rs.getLong("id"), rs.getString("producto_codigo"),
                        TipoMovimiento.valueOf(rs.getString("tipo")), rs.getInt("cantidad"),
                        rs.getTimestamp("fecha").toLocalDateTime(),
                        rs.getInt("stock_anterior"), rs.getInt("stock_posterior")));
            }
        }
        return movimientos;
    }
}

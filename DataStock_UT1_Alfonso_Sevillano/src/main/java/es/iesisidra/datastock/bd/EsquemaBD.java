package es.iesisidra.datastock.bd;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.config.ConexionBD;

/**
 * Comprobación de la conexión y del esquema datastock (RF-JDBC-01).
 */
public final class EsquemaBD {

    private EsquemaBD() { }

    /**
     * Abre una conexión, lee los metadatos y cuenta los productos.
     * La conexión se cierra al salir del try, incluso si hay una excepción.
     */
    public static InfoConexion comprobar() throws SQLException {
        try (Connection con = ConexionBD.conectar();
             Statement st = con.createStatement();
             // Consulta fija, sin datos del usuario: basta con Statement.
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM producto")) {

            DatabaseMetaData meta = con.getMetaData();
            int productos = 0;
            if (rs.next()) {
                productos = rs.getInt(1);
            }
            return new InfoConexion(con.getCatalog(),
                    meta.getDatabaseProductName(),
                    meta.getDatabaseProductVersion(),
                    meta.getDriverName() + " " + meta.getDriverVersion(),
                    productos,
                    existeProcedimiento(meta, con.getCatalog()));
        }
    }

    private static boolean existeProcedimiento(DatabaseMetaData meta, String baseDatos) throws SQLException {
        try (ResultSet rs = meta.getProcedures(baseDatos, null, "registrar_movimiento")) {
            return rs.next();
        }
    }
}

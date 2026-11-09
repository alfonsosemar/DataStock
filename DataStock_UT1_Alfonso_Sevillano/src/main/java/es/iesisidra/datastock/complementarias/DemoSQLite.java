package es.iesisidra.datastock.complementarias;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import es.iesisidra.datastock.util.Mensajes;

/**
 * Actividad comparativa obligatoria (apartado 7): SQLite como gestor embebido.
 * <p>Se ejecuta por separado, sin pasar por el menú de DataStock. La base de
 * datos es un fichero local; no hace falta servidor, usuario ni contraseña.
 * Al repetir la ejecución no se duplican filas gracias a INSERT OR IGNORE.</p>
 */
public class DemoSQLite {

    private static final String RUTA = "datos/sqlite/productos_demo.db";
    private static final String URL = "jdbc:sqlite:" + RUTA;

    public static void main(String[] args) {
        // SQLite crea el fichero .db, pero no la carpeta: la creamos si no existe.
        try {
            Files.createDirectories(Path.of("datos", "sqlite"));
        } catch (IOException e) {
            Mensajes.error("No se ha podido crear la carpeta datos/sqlite: " + e.getMessage());
            return;
        }

        try (Connection con = DriverManager.getConnection(URL)) {
            Mensajes.ok("Conexión SQLite abierta: " + RUTA);

            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS producto_demo ("
                        + "codigo TEXT PRIMARY KEY NOT NULL, "
                        + "nombre TEXT NOT NULL)");
            }
            Mensajes.ok("Tabla producto_demo disponible.");

            // INSERT OR IGNORE: si el código ya existe no inserta y devuelve 0 filas.
            int insertados = 0;
            String sql = "INSERT OR IGNORE INTO producto_demo (codigo, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, "SQL-001");
                ps.setString(2, "Producto demo A");
                insertados = insertados + ps.executeUpdate();

                ps.setString(1, "SQL-002");
                ps.setString(2, "Producto demo B");
                insertados = insertados + ps.executeUpdate();
            }
            int existentes = 2 - insertados;
            System.out.println("Insertados: " + insertados + " | Ya existentes: " + existentes);

            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT codigo, nombre FROM producto_demo ORDER BY codigo");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString("codigo") + " | " + rs.getString("nombre"));
                }
            }
        } catch (SQLException e) {
            Mensajes.error("Error de SQLite: " + e.getMessage());
            return;
        }
        Mensajes.ok("Conexión cerrada. El fichero " + RUTA + " se conserva para la próxima ejecución.");
    }
}

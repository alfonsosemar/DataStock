package es.iesisidra.datastock.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexiones JDBC con MySQL.
 * <p>Los datos de conexión (URL, usuario y contraseña) se leen del fichero
 * local {@code config/datastock.properties}, que está en {@code .gitignore}
 * para que la contraseña no llegue a GitHub. En el repositorio solo se sube
 * la plantilla {@code config/datastock.properties.example}.</p>
 * <p>Si existen las variables de entorno {@code JDBC_URL}, {@code JDBC_USER} o
 * {@code JDBC_PASSWORD}, tienen prioridad sobre el fichero.</p>
 * <p>Cada operación abre una conexión corta y la cierra con try-with-resources;
 * por eso esta clase no guarda ninguna conexión abierta.</p>
 */
public final class ConexionBD {

    private static final Path FICHERO = Path.of("config", "datastock.properties");

    private ConexionBD() { }

    /**
     * Devuelve una conexión nueva. Quien la pide es responsable de cerrarla.
     * @throws SQLException si falta algún dato de conexión o MySQL la rechaza.
     */
    public static Connection conectar() throws SQLException {
        Properties config = cargarFichero();
        String url = elegir(System.getenv("JDBC_URL"), config.getProperty("jdbc.url"));
        String usuario = elegir(System.getenv("JDBC_USER"), config.getProperty("jdbc.usuario"));
        String clave = elegir(System.getenv("JDBC_PASSWORD"), config.getProperty("jdbc.contrasena"));

        if (url == null || usuario == null || clave == null) {
            throw new SQLException("Faltan datos de conexión. Copia config/datastock.properties.example como "
                    + "config/datastock.properties y completa jdbc.url, jdbc.usuario y jdbc.contrasena.");
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    // Si el fichero no existe se devuelve vacío y solo se usan las variables de entorno.
    private static Properties cargarFichero() throws SQLException {
        Properties config = new Properties();
        if (Files.exists(FICHERO)) {
            try (InputStream entrada = Files.newInputStream(FICHERO)) {
                config.load(entrada);
            } catch (IOException e) {
                throw new SQLException("No se puede leer " + FICHERO + ".");
            }
        }
        return config;
    }

    // Devuelve el primer valor que no esté vacío, o null si ninguno lo tiene.
    private static String elegir(String preferente, String alternativo) {
        if (preferente != null && !preferente.isBlank()) {
            return preferente.trim();
        }
        if (alternativo != null && !alternativo.isBlank()) {
            return alternativo.trim();
        }
        return null;
    }
}

package es.iesisidra.datastock.bd;

import java.sql.SQLException;

/**
 * Traduce una {@link SQLException} a un mensaje comprensible para el usuario.
 * <p>Se usa el código de error de MySQL ({@code getErrorCode()}) y el estado SQL
 * ({@code getSQLState()}) en lugar de mostrar la traza técnica. Ningún mensaje
 * incluye la contraseña.</p>
 */
public final class ErroresBD {

    // Códigos de error de MySQL que la aplicación explica de forma específica.
    private static final int ACCESO_DENEGADO = 1045;
    private static final int BD_DESCONOCIDA = 1049;
    private static final int TABLA_INEXISTENTE = 1146;
    private static final int CLAVE_DUPLICADA = 1062;
    private static final int FILA_PADRE_CON_HIJOS = 1451;   // borrar algo que otros referencian
    private static final int FILA_PADRE_INEXISTENTE = 1452; // referenciar algo que no existe
    private static final int CHECK_INCUMPLIDO = 3819;

    private ErroresBD() { }

    /** @return explicación del error para mostrar en rojo. */
    public static String explicar(SQLException e) {
        String estado = e.getSQLState();
        if (estado != null && estado.startsWith("08")) {
            return "No se puede conectar con el servidor MySQL. Comprueba que está arrancado y que la URL es correcta.";
        }
        if ("45000".equals(estado)) {
            // Error lanzado con SIGNAL por el procedimiento registrar_movimiento.
            return e.getMessage() + ".";
        }
        switch (e.getErrorCode()) {
            case ACCESO_DENEGADO:
                return "MySQL ha rechazado el usuario o la contraseña.";
            case BD_DESCONOCIDA:
                return "La base de datos no existe. Ejecuta antes el script DataStock_BD_MySQL.sql.";
            case TABLA_INEXISTENTE:
                return "Falta una tabla del esquema. Ejecuta el script DataStock_BD_MySQL.sql.";
            case CLAVE_DUPLICADA:
                return "Ya existe un registro con ese valor (clave duplicada).";
            case FILA_PADRE_CON_HIJOS:
                return "No se puede eliminar: hay otros registros que dependen de él (integridad referencial).";
            case FILA_PADRE_INEXISTENTE:
                return "La categoría o el proveedor indicado no existe (integridad referencial).";
            case CHECK_INCUMPLIDO:
                return "Los datos incumplen una restricción CHECK de la base de datos.";
            default:
                break;
        }
        if (e.getErrorCode() == 0 && estado == null) {
            // Errores propios de ConexionBD (por ejemplo, falta JDBC_PASSWORD).
            return e.getMessage();
        }
        return "Error de base de datos (código " + e.getErrorCode() + "): " + e.getMessage();
    }

    /** @return true si el error es una clave primaria o única repetida. */
    public static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == CLAVE_DUPLICADA;
    }

    /** @return true si un DELETE falla porque otros registros lo referencian (clave foránea). */
    public static boolean tieneDependientes(SQLException e) {
        return e.getErrorCode() == FILA_PADRE_CON_HIJOS;
    }
}

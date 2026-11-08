package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Lectura de campos para los menús de MySQL.
 * <p>Cada método pide un dato, lo valida y vuelve a preguntar si es incorrecto.
 * Dejar el dato vacío y pulsar Intro cancela la operación: en ese caso se
 * devuelve null y el menú vuelve atrás sin tocar la base de datos.</p>
 */
final class EntradaDatos {

    static final String CANCELAR = " (Intro para cancelar): ";

    private EntradaDatos() { }

    /** Pide un identificador entero positivo. @return el id, o null si se cancela. */
    static Integer pedirId(String campo) {
        while (true) {
            String texto = LectorConsola.leerTexto(campo + CANCELAR).trim();
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int id = Integer.parseInt(texto);
                if (id > 0) {
                    return id;
                }
                Mensajes.error("El identificador debe ser un entero positivo.");
            } catch (NumberFormatException e) {
                Mensajes.error("\"" + texto + "\" no es un número entero.");
            }
        }
    }

    /** Pide un texto obligatorio sin espacios extremos. @return el texto, o null si se cancela. */
    static String pedirTexto(String campo, int minimo, int maximo) {
        while (true) {
            String texto = LectorConsola.leerTexto(campo + CANCELAR).trim();
            if (texto.isEmpty()) {
                return null;
            }
            String error = errorLongitud(texto, campo, minimo, maximo);
            if (error == null) {
                return texto;
            }
            Mensajes.error(error);
        }
    }

    /**
     * Pide un texto que puede dejarse vacío para conservar el valor actual.
     * @return el nuevo texto, o el actual si se pulsa Intro.
     */
    static String pedirTextoOConservar(String campo, String actual, int minimo, int maximo) {
        while (true) {
            String texto = LectorConsola.leerTexto(campo + " [" + actual + "] (Intro conserva): ").trim();
            if (texto.isEmpty()) {
                return actual;
            }
            String error = errorLongitud(texto, campo, minimo, maximo);
            if (error == null) {
                return texto;
            }
            Mensajes.error(error);
        }
    }

    /** @return mensaje de error si la longitud no está entre minimo y maximo; null si es válida. */
    static String errorLongitud(String texto, String campo, int minimo, int maximo) {
        if (texto.length() < minimo || texto.length() > maximo) {
            return campo + " debe tener entre " + minimo + " y " + maximo + " caracteres.";
        }
        return null;
    }
}

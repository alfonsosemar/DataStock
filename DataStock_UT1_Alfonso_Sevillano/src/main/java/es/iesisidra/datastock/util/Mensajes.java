package es.iesisidra.datastock.util;

/**
 * Mensajes de resultado con prefijo y color.
 * <p>El prefijo ([OK], [ERROR], [AVISO]) permite entender el resultado aunque
 * la terminal no muestre colores ANSI. {@link ColoresConsola#pintar} añade
 * RESET al final de cada mensaje.</p>
 */
public final class Mensajes {

    private Mensajes() { }

    /** Operación completada: verde. */
    public static void ok(String texto) {
        System.out.println(ColoresConsola.pintar("[OK] " + texto, ColoresConsola.VERDE));
    }

    /** Operación rechazada o fallida: rojo. */
    public static void error(String texto) {
        System.out.println(ColoresConsola.pintar("[ERROR] " + texto, ColoresConsola.ROJO));
    }

    /** Aviso informativo o cancelación: amarillo. */
    public static void aviso(String texto) {
        System.out.println(ColoresConsola.pintar("[AVISO] " + texto, ColoresConsola.AMARILLO));
    }

    /** Título de una pantalla, seguido de una línea separadora. */
    public static void titulo(String texto) {
        System.out.println();
        System.out.println(ColoresConsola.pintar(texto, ColoresConsola.NEGRITA));
        System.out.println("----------------------------------------");
    }
}

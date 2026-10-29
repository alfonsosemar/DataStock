package es.iesisidra.datastock.ficheros;

/**
 * Error controlado en una operación con ficheros (no existe, mal formado, etc.).
 * El mensaje está pensado para enseñárselo directamente al usuario.
 */
public class OperacionFicheroException extends Exception {
    private static final long serialVersionUID = 1L;


    public OperacionFicheroException(String mensaje) {
        super(mensaje);
    }

    public OperacionFicheroException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

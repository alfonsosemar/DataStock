package es.iesisidra.datastock.ficheros;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Comprobaciones de rutas que se repiten en varias funcionalidades.
 * Si algo falla lanza una OperacionFicheroException con un mensaje claro.
 */
public final class Rutas {

    private Rutas() { }

    /** Ruta de un fichero que se va a LEER: debe existir, ser fichero y poder leerse. */
    public static Path ficheroLegible(String ruta) throws OperacionFicheroException {
        Path path = convertir(ruta);
        if (!Files.exists(path)) {
            throw new OperacionFicheroException("El fichero no existe: " + path);
        }
        if (Files.isDirectory(path)) {
            throw new OperacionFicheroException("La ruta es un directorio, no un fichero: " + path);
        }
        if (!Files.isReadable(path)) {
            throw new OperacionFicheroException("No hay permisos de lectura sobre: " + path);
        }
        return path;
    }

    /** Ruta de un fichero que se va a ESCRIBIR: su directorio debe existir y no puede ser un directorio. */
    public static Path destinoEscritura(String ruta) throws OperacionFicheroException {
        Path path = convertir(ruta);
        if (Files.isDirectory(path)) {
            throw new OperacionFicheroException("La ruta es un directorio, indica un nombre de fichero: " + path);
        }
        Path carpeta = path.toAbsolutePath().getParent();
        if (carpeta == null || !Files.isDirectory(carpeta)) {
            throw new OperacionFicheroException("El directorio de destino no existe: " + carpeta);
        }
        if (Files.exists(path) && !Files.isWritable(path)) {
            throw new OperacionFicheroException("No hay permisos de escritura sobre: " + path);
        }
        if (!Files.exists(path) && !Files.isWritable(carpeta)) {
            throw new OperacionFicheroException("No hay permisos para crear ficheros en: " + carpeta);
        }
        return path;
    }

    // Quita espacios y comprueba que la ruta no esté vacía ni tenga caracteres inválidos.
    private static Path convertir(String ruta) throws OperacionFicheroException {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new OperacionFicheroException("La ruta está vacía.");
        }
        try {
            return Paths.get(ruta.trim());
        } catch (InvalidPathException e) {
            throw new OperacionFicheroException("La ruta contiene caracteres no válidos: " + ruta.trim());
        }
    }
}

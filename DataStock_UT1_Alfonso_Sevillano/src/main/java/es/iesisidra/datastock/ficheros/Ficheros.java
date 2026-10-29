package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.util.ColoresConsola;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Utilidades comunes de ficheros:
 * 1) comprobar rutas, 2) explorar una ruta (RF-FIC-01), 3) leer XML de forma segura.
 */
public final class Ficheros {

    private Ficheros() { }

    // =====================================================================
    // 1. Comprobaciones de rutas (si falla, lanzan OperacionFicheroException)
    // =====================================================================

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

    // =====================================================================
    // 2. RF-FIC-01: explorar una ruta (fichero o directorio)
    // =====================================================================

    public static void explorar(String ruta) {
        // 1. La ruta no puede estar vacía
        if (ruta == null || ruta.trim().isEmpty()) {
            mostrarError("La ruta está vacía.");
            return;
        }

        try {
            File elemento = new File(ruta.trim());

            // 2. Debe existir
            if (!elemento.exists()) {
                mostrarError("La ruta no existe: " + ruta.trim());
                return;
            }

            // 3. Si es un fichero: nombre, ruta absoluta y tamaño
            if (elemento.isFile()) {
                System.out.println("\n===== EXPLORAR RUTA =====");
                System.out.println("[FICHERO] " + elemento.getName());
                System.out.println("Ruta absoluta: " + elemento.getAbsolutePath());
                System.out.println("Tamaño: " + elemento.length() + " bytes");
                return;
            }

            // 4. Si es un directorio: se listan solo sus hijos inmediatos
            File[] hijos = elemento.listFiles();
            if (hijos == null) {
                mostrarError("No se puede acceder al directorio (¿faltan permisos?): " + ruta.trim());
                return;
            }

            System.out.println("\n===== EXPLORAR RUTA =====");
            System.out.println("Ruta: " + ruta.trim());
            if (hijos.length == 0) {
                System.out.println("El directorio está vacío.");
            }

            Arrays.sort(hijos); // orden alfabético para que la salida sea estable
            int ficheros = 0;
            int directorios = 0;
            for (File hijo : hijos) {
                if (hijo.isDirectory()) {
                    System.out.println("[DIR] " + hijo.getName());
                    directorios++;
                } else {
                    System.out.println("[FICHERO] " + hijo.getName() + " " + hijo.length() + " bytes");
                    ficheros++;
                }
            }
            System.out.println("Total ficheros: " + ficheros + " | Directorios: " + directorios);

        } catch (SecurityException e) {
            mostrarError("Acceso denegado por el sistema: " + e.getMessage());
        }
    }

    private static void mostrarError(String mensaje) {
        System.out.println(ColoresConsola.pintar(mensaje, ColoresConsola.ROJO));
    }

    // =====================================================================
    // 3. Lectura de XML con DOM protegida contra XXE (RF-FIC-03 y RF-FIC-07)
    // =====================================================================

    /** Lee un XML y devuelve su árbol DOM. Rechaza los DTD y las entidades externas. */
    public static Document leerXml(Path ruta) throws OperacionFicheroException {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            return crearConstructorXml().parse(entrada);
        } catch (SAXException e) {
            throw new OperacionFicheroException(
                    "El XML está mal formado o no está permitido (por ejemplo, contiene un DTD): " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new OperacionFicheroException("No se pudo configurar el lector XML: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero XML: " + e.getMessage(), e);
        }
    }

    // Fábrica de DOM con las opciones de seguridad activadas.
    private static DocumentBuilder crearConstructorXml() throws ParserConfigurationException {
        DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
        fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // sin DTD
        fabrica.setFeature("http://xml.org/sax/features/external-general-entities", false);
        fabrica.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        fabrica.setXIncludeAware(false);
        fabrica.setExpandEntityReferences(false);

        DocumentBuilder constructor = fabrica.newDocumentBuilder();
        constructor.setErrorHandler(new ManejadorErrores());
        return constructor;
    }

    // Evita que el parser escriba "[Fatal Error]" por consola: relanza el error para que lo trate leerXml().
    private static class ManejadorErrores implements ErrorHandler {
        @Override
        public void warning(SAXParseException e) { }

        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }
}

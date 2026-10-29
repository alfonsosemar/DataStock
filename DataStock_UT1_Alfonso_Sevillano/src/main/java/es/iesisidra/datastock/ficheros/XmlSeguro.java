package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Lectura de XML con DOM protegida contra XXE: se rechazan los DTD y las
 * entidades externas. La usan la importación XML (RF-FIC-03) y XPath (RF-FIC-07).
 */
public final class XmlSeguro {

    private XmlSeguro() { }

    /** Lee un fichero XML y devuelve su árbol DOM; si algo falla lanza OperacionFicheroException. */
    public static Document leer(Path ruta) throws OperacionFicheroException {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            return crearConstructor().parse(entrada);
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
    private static DocumentBuilder crearConstructor() throws ParserConfigurationException {
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

    // Evita que el parser escriba "[Fatal Error]" por consola: relanza el error para que lo trate leer().
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

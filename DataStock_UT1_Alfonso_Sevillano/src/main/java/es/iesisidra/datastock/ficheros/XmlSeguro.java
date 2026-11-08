package es.iesisidra.datastock.ficheros;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/** Lectura de catálogos XML con DOM. La usan la importación XML (RF-FIC-03) y XPath (RF-FIC-07). */
public final class XmlSeguro {

    private XmlSeguro() { }

    /** Carga el XML en un árbol DOM y comprueba que la raíz sea catalogo. */
    public static Document leerCatalogo(Path ruta) throws OperacionFicheroException {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            // Rechaza los XML con DOCTYPE, para que un fichero no pueda obligar a leer otros ficheros externos
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

            Document documento = fabrica.newDocumentBuilder().parse(entrada);

            String raiz = documento.getDocumentElement().getTagName();
            if (!"catalogo".equals(raiz)) {
                throw new OperacionFicheroException("La raíz del XML debe ser <catalogo> y es <" + raiz + ">.");
            }
            return documento;
        } catch (SAXException e) {
            throw new OperacionFicheroException(
                    "El XML está mal formado o no está permitido (por ejemplo, contiene un DTD): " + e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new OperacionFicheroException("No se pudo configurar el lector XML: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero XML: " + e.getMessage(), e);
        }
    }
}

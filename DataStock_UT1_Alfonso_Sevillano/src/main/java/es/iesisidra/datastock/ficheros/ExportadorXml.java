package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.modelo.Producto;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** RF-FIC-05 (parte XML): construye el documento con DOM y lo guarda indentado. */
public final class ExportadorXml {

    private ExportadorXml() { }

    /** Exporta la lista (ya ordenada por código) en UTF-8. Devuelve cuántos productos escribió. */
    public static int exportar(List<Producto> productos, Path destino) throws OperacionFicheroException {
        try {
            // 1. Crear el documento y la raíz <catalogo>
            Document documento = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element raiz = documento.createElement("catalogo");
            documento.appendChild(raiz);

            // 2. Un <producto> por cada producto, con sus seis campos
            for (Producto p : productos) {
                Element elemento = documento.createElement("producto");
                agregarCampo(documento, elemento, "codigo", p.getCodigo());
                agregarCampo(documento, elemento, "nombre", p.getNombre());
                agregarCampo(documento, elemento, "precio", p.getPrecio().toPlainString());
                agregarCampo(documento, elemento, "stock", String.valueOf(p.getStock()));
                agregarCampo(documento, elemento, "categoriaId", String.valueOf(p.getCategoriaId()));
                agregarCampo(documento, elemento, "proveedorId", String.valueOf(p.getProveedorId()));
                raiz.appendChild(elemento);
            }

            // 3. Guardar el documento en el fichero con indentación
            guardar(documento, destino);
            return productos.size();

        } catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new OperacionFicheroException("No se pudo escribir el XML: " + e.getMessage(), e);
        }
    }

    private static void agregarCampo(Document documento, Element padre, String etiqueta, String valor) {
        Element campo = documento.createElement(etiqueta);
        campo.setTextContent(valor);
        padre.appendChild(campo);
    }

    private static void guardar(Document documento, Path destino) throws IOException, TransformerException {
        TransformerFactory fabrica = TransformerFactory.newInstance();
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        fabrica.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformador = fabrica.newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        try (OutputStream salida = Files.newOutputStream(destino)) {
            transformador.transform(new DOMSource(documento), new StreamResult(salida));
        }
    }
}

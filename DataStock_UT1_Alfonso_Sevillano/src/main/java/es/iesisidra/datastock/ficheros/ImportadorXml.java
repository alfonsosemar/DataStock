package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ProcesadorProductos;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** RF-FIC-03: importa un catálogo XML con DOM (mismas validaciones que el JSON). */
public class ImportadorXml {

    private final ProcesadorProductos procesador;

    public ImportadorXml(Inventario inventario) {
        this.procesador = new ProcesadorProductos(inventario);
    }

    public ResultadoImportacion importar(String ruta) throws OperacionFicheroException {
        Path path = Rutas.ficheroLegible(ruta);

        // 1. Leer con DOM seguro (rechaza DTD y XML mal formado)
        Document documento = XmlSeguro.leer(path);

        // 2. La raíz debe ser <catalogo>
        Element raiz = documento.getDocumentElement();
        if (!"catalogo".equals(raiz.getTagName())) {
            throw new OperacionFicheroException("La raíz del XML debe ser <catalogo> y es <" + raiz.getTagName() + ">.");
        }

        // 3. Recoger los elementos <producto> hijos de la raíz
        List<Element> registros = new ArrayList<>();
        NodeList hijos = raiz.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node hijo = hijos.item(i);
            if (hijo.getNodeType() == Node.ELEMENT_NODE && "producto".equals(hijo.getNodeName())) {
                registros.add((Element) hijo);
            }
        }

        ResultadoImportacion resultado = new ResultadoImportacion();
        resultado.setEncontrados(registros.size());

        // 4. Procesar cada producto con las mismas reglas que el JSON
        for (int i = 0; i < registros.size(); i++) {
            Element registro = registros.get(i);
            procesador.procesar(i + 1,
                    texto(registro, "codigo"), texto(registro, "nombre"), texto(registro, "precio"),
                    texto(registro, "stock"), texto(registro, "categoriaId"), texto(registro, "proveedorId"),
                    resultado);
        }
        return resultado;
    }

    // Texto del hijo directo con ese nombre, o null si la etiqueta no existe.
    private String texto(Element producto, String etiqueta) {
        NodeList hijos = producto.getChildNodes();
        for (int i = 0; i < hijos.getLength(); i++) {
            Node hijo = hijos.item(i);
            if (hijo.getNodeType() == Node.ELEMENT_NODE && etiqueta.equals(hijo.getNodeName())) {
                return hijo.getTextContent().trim();
            }
        }
        return null;
    }
}

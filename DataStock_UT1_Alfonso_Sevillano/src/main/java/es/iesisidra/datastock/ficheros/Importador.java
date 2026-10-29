package es.iesisidra.datastock.ficheros;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ProcesadorProductos;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Importación de catálogos: JSON con Gson (RF-FIC-02) y XML con DOM (RF-FIC-03).
 * Los dos terminan llamando al mismo ProcesadorProductos, así que validan igual.
 */
public class Importador {

    private final ProcesadorProductos procesador;

    public Importador(Inventario inventario) {
        this.procesador = new ProcesadorProductos(inventario);
    }

    // =====================================================================
    // JSON
    // =====================================================================

    /**
     * Lee el JSON y añade al inventario los productos válidos.
     * Si el fichero no se puede interpretar lanza la excepción y NO se añade nada.
     */
    public ResultadoImportacion importarJson(String ruta) throws OperacionFicheroException {
        Path path = Ficheros.ficheroLegible(ruta);
        JsonArray registros = leerArrayProductos(path);

        ResultadoImportacion resultado = new ResultadoImportacion();
        resultado.setEncontrados(registros.size());

        // Se recorre cada registro; los fallos de un producto no afectan a los demás
        for (int i = 0; i < registros.size(); i++) {
            int numero = i + 1;
            JsonElement elemento = registros.get(i);

            if (!elemento.isJsonObject()) {
                resultado.registrarRechazo("Producto " + numero, "el registro no es un objeto JSON");
                continue;
            }

            JsonObject registro = elemento.getAsJsonObject();
            procesador.procesar(numero,
                    textoJson(registro, "codigo"), textoJson(registro, "nombre"), textoJson(registro, "precio"),
                    textoJson(registro, "stock"), textoJson(registro, "categoriaId"),
                    textoJson(registro, "proveedorId"), resultado);
        }
        return resultado;
    }

    // Comprueba la estructura general: objeto raíz con el array "productos".
    private JsonArray leerArrayProductos(Path path) throws OperacionFicheroException {
        try (Reader lector = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement raiz = JsonParser.parseReader(lector);

            if (!raiz.isJsonObject()) {
                throw new OperacionFicheroException("La raíz del JSON debe ser un objeto con el array \"productos\".");
            }
            JsonObject objetoRaiz = raiz.getAsJsonObject();
            if (!objetoRaiz.has("productos") || !objetoRaiz.get("productos").isJsonArray()) {
                throw new OperacionFicheroException("El JSON no contiene el array \"productos\".");
            }
            return objetoRaiz.getAsJsonArray("productos");

        } catch (JsonParseException e) {
            throw new OperacionFicheroException("El JSON está mal formado: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero JSON: " + e.getMessage(), e);
        }
    }

    // Devuelve el valor del campo como texto, o null si falta o es null en el JSON.
    private String textoJson(JsonObject registro, String campo) {
        if (!registro.has(campo) || registro.get(campo).isJsonNull()) {
            return null;
        }
        JsonElement valor = registro.get(campo);
        if (valor.isJsonPrimitive()) {
            return valor.getAsString();
        }
        return valor.toString(); // objeto o array: no será válido y se rechazará el producto
    }

    // =====================================================================
    // XML
    // =====================================================================

    public ResultadoImportacion importarXml(String ruta) throws OperacionFicheroException {
        Path path = Ficheros.ficheroLegible(ruta);

        // 1. Leer con DOM seguro (rechaza DTD y XML mal formado)
        Document documento = Ficheros.leerXml(path);

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
                    textoXml(registro, "codigo"), textoXml(registro, "nombre"), textoXml(registro, "precio"),
                    textoXml(registro, "stock"), textoXml(registro, "categoriaId"),
                    textoXml(registro, "proveedorId"), resultado);
        }
        return resultado;
    }

    // Texto del hijo directo con ese nombre, o null si la etiqueta no existe.
    private String textoXml(Element producto, String etiqueta) {
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

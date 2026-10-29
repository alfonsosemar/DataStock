package es.iesisidra.datastock.ficheros;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import es.iesisidra.datastock.modelo.Producto;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** RF-FIC-05 (parte JSON): escribe el inventario con la raíz "productos". */
public final class ExportadorJson {

    private ExportadorJson() { }

    /** Exporta la lista (ya ordenada por código) en UTF-8. Devuelve cuántos productos escribió. */
    public static int exportar(List<Producto> productos, Path destino) throws OperacionFicheroException {
        // 1. Construir el árbol JSON: { "productos": [ {...}, {...} ] }
        JsonArray array = new JsonArray();
        for (Producto p : productos) {
            JsonObject objeto = new JsonObject();
            objeto.addProperty("codigo", p.getCodigo());
            objeto.addProperty("nombre", p.getNombre());
            objeto.addProperty("precio", p.getPrecio());
            objeto.addProperty("stock", p.getStock());
            objeto.addProperty("categoriaId", p.getCategoriaId());
            objeto.addProperty("proveedorId", p.getProveedorId());
            array.add(objeto);
        }
        JsonObject raiz = new JsonObject();
        raiz.add("productos", array);

        // 2. Escribirlo con formato legible
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            gson.toJson(raiz, escritor);
        } catch (IOException | JsonParseException e) {
            throw new OperacionFicheroException("No se pudo escribir el JSON: " + e.getMessage(), e);
        }
        return productos.size();
    }
}

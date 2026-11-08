package es.iesisidra.datastock.ficheros;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import es.iesisidra.datastock.modelo.Producto;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** RF-FIC-05 (parte JSON): escribe el inventario con la raíz "productos". */
public final class ExportadorJson {

    private ExportadorJson() { }

    /** Exporta la lista (ya ordenada por código) en UTF-8. Devuelve cuántos productos escribió. */
    public static int exportar(List<Producto> productos, Path destino) throws OperacionFicheroException {
        // setPrettyPrinting: el JSON sale con saltos de línea y sangría
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // El mapa aporta la raíz { "productos": [...] }; Gson convierte cada Producto automáticamente
        try (Writer escritor = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            gson.toJson(Map.of("productos", productos), escritor);
        } catch (IOException | JsonIOException e) {
            throw new OperacionFicheroException("No se pudo escribir el JSON: " + e.getMessage(), e);
        }
        return productos.size();
    }
}

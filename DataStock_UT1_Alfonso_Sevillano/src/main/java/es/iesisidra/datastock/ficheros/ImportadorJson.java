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

/** RF-FIC-02: importa un catálogo JSON con Gson. */
public class ImportadorJson {

    private final ProcesadorProductos procesador;

    public ImportadorJson(Inventario inventario) {
        this.procesador = new ProcesadorProductos(inventario);
    }

    /**
     * Lee el fichero y añade al inventario los productos válidos.
     * Si el fichero no se puede interpretar lanza la excepción y NO se añade nada.
     */
    public ResultadoImportacion importar(String ruta) throws OperacionFicheroException {
        Path path = Rutas.ficheroLegible(ruta);
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
                    texto(registro, "codigo"), texto(registro, "nombre"), texto(registro, "precio"),
                    texto(registro, "stock"), texto(registro, "categoriaId"), texto(registro, "proveedorId"),
                    resultado);
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
    private String texto(JsonObject registro, String campo) {
        if (!registro.has(campo) || registro.get(campo).isJsonNull()) {
            return null;
        }
        JsonElement valor = registro.get(campo);
        if (valor.isJsonPrimitive()) {
            return valor.getAsString();
        }
        return valor.toString(); // objeto o array: no será válido y se rechazará el producto
    }
}

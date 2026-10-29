package es.iesisidra.datastock.servicio;

import es.iesisidra.datastock.modelo.Producto;
import java.util.List;

/**
 * Procesa UN registro leído de un catálogo: lo valida, comprueba duplicados y,
 * si es correcto, lo añade al inventario. Lo comparten el importador JSON y el XML.
 */
public class ProcesadorProductos {

    private final Inventario inventario;

    public ProcesadorProductos(Inventario inventario) {
        this.inventario = inventario;
    }

    /**
     * @param numero posición del registro en el fichero (empieza en 1), para las incidencias
     * Los campos llegan como texto (null si faltan en el fichero).
     */
    public void procesar(int numero, String codigo, String nombre, String precio, String stock,
                         String categoriaId, String proveedorId, ResultadoImportacion resultado) {

        List<String> errores = ValidadorProducto.validar(codigo, nombre, precio, stock, categoriaId, proveedorId);

        // Duplicado: solo se mira si el código tiene algo escrito
        boolean hayCodigo = codigo != null && !codigo.trim().isEmpty();
        if (hayCodigo && inventario.existeCodigo(codigo)) {
            errores.add("código duplicado");
        }

        if (errores.isEmpty()) {
            Producto p = ValidadorProducto.crearProducto(codigo, nombre, precio, stock, categoriaId, proveedorId);
            inventario.agregar(p);
            resultado.registrarImportado();
        } else {
            // Si no hay código, se identifica por su posición ("Producto 5")
            String identificador = "Producto " + numero;
            if (hayCodigo) {
                identificador = codigo.trim();
            }
            resultado.registrarRechazo(identificador, unir(errores));
        }
    }

    // Junta los errores en una sola línea separados por "; "
    private String unir(List<String> errores) {
        String texto = "";
        for (int i = 0; i < errores.size(); i++) {
            if (i > 0) {
                texto = texto + "; ";
            }
            texto = texto + errores.get(i);
        }
        return texto;
    }
}

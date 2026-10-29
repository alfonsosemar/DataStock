package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ValidadorProducto;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** RF-FIC-06: guarda y recupera el inventario con serialización Java (.dat). */
public final class PersistenciaInventario {

    // Solo se permite deserializar estas clases; cualquier otra se rechaza (seguridad).
    // Object aparece porque ArrayList guarda sus elementos en un array de Object.
    private static final String CLASES_PERMITIDAS =
            "java.util.ArrayList;es.iesisidra.datastock.modelo.Producto;"
            + "java.math.BigDecimal;java.math.BigInteger;java.lang.Number;java.lang.Object;!*";

    private PersistenciaInventario() { }

    /** Guarda la colección completa en un fichero binario. */
    public static void guardar(List<Producto> productos, Path destino) throws OperacionFicheroException {
        try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(destino))) {
            salida.writeObject(new ArrayList<>(productos)); // se guarda una copia de la lista completa
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo guardar el inventario: " + e.getMessage(), e);
        }
    }

    /**
     * Lee y comprueba el fichero. Devuelve la lista solo si TODO es correcto;
     * así el inventario en memoria no se toca si el fichero falla.
     */
    public static List<Producto> recuperar(Path origen) throws OperacionFicheroException {
        Object leido;
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(origen))) {
            entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(CLASES_PERMITIDAS));
            leido = entrada.readObject();
        } catch (ClassNotFoundException e) {
            throw new OperacionFicheroException("El fichero contiene clases desconocidas: " + e.getMessage(), e);
        } catch (IOException e) {
            // Aquí caen los ficheros dañados, incompatibles o que no son de DataStock
            String detalle = e.getMessage();
            if (detalle == null) {
                detalle = e.getClass().getSimpleName(); // por ejemplo EOFException: el fichero se corta antes de tiempo
            }
            throw new OperacionFicheroException("El fichero está dañado o no es un inventario de DataStock: "
                    + detalle, e);
        }

        // Comprobar que es una lista de productos válidos y sin códigos repetidos
        if (!(leido instanceof List<?>)) {
            throw new OperacionFicheroException("El fichero no contiene una lista de productos.");
        }
        List<?> elementos = (List<?>) leido;
        Inventario temporal = new Inventario();
        for (Object elemento : elementos) {
            if (!(elemento instanceof Producto)) {
                throw new OperacionFicheroException("El fichero contiene un objeto que no es un producto.");
            }
            Producto p = (Producto) elemento;
            if (!ValidadorProducto.validar(p).isEmpty()) {
                throw new OperacionFicheroException("El fichero contiene un producto no válido: " + p.getCodigo());
            }
            if (!temporal.agregar(p)) {
                throw new OperacionFicheroException("El fichero contiene un código repetido: " + p.getCodigo());
            }
        }
        return new ArrayList<>(temporal.getProductos());
    }
}

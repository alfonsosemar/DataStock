package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.modelo.Producto;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * RF-FIC-08: fichero binario de registros de longitud fija (36 bytes cada uno).
 * Registro = código (12 chars = 24 bytes) + precio (double = 8) + stock (int = 4).
 */
public final class StockBinario {

    public static final int TAM_CODIGO = 12;
    public static final int TAM_REGISTRO = 36;
    public static final int OFFSET_STOCK = 32; // 24 bytes de código + 8 de precio

    private StockBinario() { }

    /** Crea (o sobrescribe) el fichero con una copia de los productos recibidos. */
    public static void generar(List<Producto> productos, Path destino) throws OperacionFicheroException {
        try (RandomAccessFile fichero = new RandomAccessFile(destino.toFile(), "rw")) {
            fichero.setLength(0); // vaciar el contenido anterior
            for (Producto p : productos) {
                fichero.writeChars(ajustarCodigo(p.getCodigo())); // 24 bytes
                fichero.writeDouble(p.getPrecio().doubleValue()); // 8 bytes
                fichero.writeInt(p.getStock());                   // 4 bytes
            }
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo generar el fichero binario: " + e.getMessage(), e);
        }
    }

    /** Número de registros del fichero (comprueba que el tamaño sea múltiplo de 36). */
    public static long contarRegistros(Path ruta) throws OperacionFicheroException {
        try (RandomAccessFile fichero = new RandomAccessFile(ruta.toFile(), "r")) {
            return registrosDe(fichero);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero binario: " + e.getMessage(), e);
        }
    }

    /** Lee UN registro saltando directamente a su posición con seek(). */
    public static RegistroStock leerRegistro(Path ruta, long posicion) throws OperacionFicheroException {
        try (RandomAccessFile fichero = new RandomAccessFile(ruta.toFile(), "r")) {
            comprobarPosicion(fichero, posicion);
            fichero.seek(posicion * TAM_REGISTRO);
            return leerRegistroActual(fichero);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero binario: " + e.getMessage(), e);
        }
    }

    /** Cambia SOLO el stock de un registro (seek al campo stock, sin reescribir el registro). */
    public static int modificarStock(Path ruta, long posicion, int nuevoStock) throws OperacionFicheroException {
        if (nuevoStock < 0) {
            throw new OperacionFicheroException("El stock no puede ser negativo.");
        }
        try (RandomAccessFile fichero = new RandomAccessFile(ruta.toFile(), "rw")) {
            comprobarPosicion(fichero, posicion);
            long inicioStock = posicion * TAM_REGISTRO + OFFSET_STOCK;

            fichero.seek(inicioStock);
            int stockAnterior = fichero.readInt();

            fichero.seek(inicioStock); // volver a colocarse, porque readInt avanzó el puntero
            fichero.writeInt(nuevoStock);
            return stockAnterior;
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo modificar el fichero binario: " + e.getMessage(), e);
        }
    }

    /** Lectura secuencial de todos los registros, de principio a fin. */
    public static List<RegistroStock> leerTodos(Path ruta) throws OperacionFicheroException {
        List<RegistroStock> registros = new ArrayList<>();
        try (RandomAccessFile fichero = new RandomAccessFile(ruta.toFile(), "r")) {
            registrosDe(fichero); // solo para validar el tamaño
            while (fichero.getFilePointer() < fichero.length()) {
                registros.add(leerRegistroActual(fichero));
            }
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo leer el fichero binario: " + e.getMessage(), e);
        }
        return registros;
    }

    // --- Métodos auxiliares ---

    // El código ocupa siempre 12 caracteres: se rellena con espacios por la derecha.
    private static String ajustarCodigo(String codigo) {
        String resultado = codigo;
        while (resultado.length() < TAM_CODIGO) {
            resultado = resultado + " ";
        }
        return resultado.substring(0, TAM_CODIGO);
    }

    // Lee un registro desde donde esté el puntero ahora mismo.
    private static RegistroStock leerRegistroActual(RandomAccessFile fichero) throws IOException {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < TAM_CODIGO; i++) {
            codigo.append(fichero.readChar());
        }
        double precio = fichero.readDouble();
        int stock = fichero.readInt();
        return new RegistroStock(codigo.toString().trim(), precio, stock);
    }

    private static long registrosDe(RandomAccessFile fichero) throws IOException, OperacionFicheroException {
        long tamano = fichero.length();
        if (tamano == 0 || tamano % TAM_REGISTRO != 0) {
            throw new OperacionFicheroException("Fichero binario no válido: su tamaño (" + tamano
                    + " bytes) no es un múltiplo de " + TAM_REGISTRO + ".");
        }
        return tamano / TAM_REGISTRO;
    }

    private static void comprobarPosicion(RandomAccessFile fichero, long posicion)
            throws IOException, OperacionFicheroException {
        long total = registrosDe(fichero);
        if (posicion < 0 || posicion >= total) {
            throw new OperacionFicheroException("Posición fuera de rango. Debe estar entre 0 y " + (total - 1) + ".");
        }
    }
}

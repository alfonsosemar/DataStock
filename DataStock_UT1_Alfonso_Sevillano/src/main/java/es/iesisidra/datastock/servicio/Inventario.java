package es.iesisidra.datastock.servicio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Colección común de productos en memoria y sus reglas básicas:
 * alta sin duplicados, búsquedas y sustitución completa.
 */
public class Inventario {

    private final List<Producto> productos = new ArrayList<>();

    /** Lista real (la usa el menú para contar). Para añadir, utiliza agregar(). */
    public List<Producto> getProductos() { return productos; }

    public boolean estaVacio() { return productos.isEmpty(); }

    public int cantidad() { return productos.size(); }

    /** Añade el producto solo si su código no existe ya (sin distinguir mayúsculas). */
    public boolean agregar(Producto p) {
        if (p == null || existeCodigo(p.getCodigo())) {
            return false;
        }
        productos.add(p);
        return true;
    }

    public boolean existeCodigo(String codigo) {
        return buscarPorCodigo(codigo) != null;
    }

    /** Busca por código exacto ignorando mayúsculas y espacios extremos; devuelve null si no existe. */
    public Producto buscarPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        String buscado = codigo.trim();
        for (Producto p : productos) {
            if (p.getCodigo().equalsIgnoreCase(buscado)) {
                return p;
            }
        }
        return null;
    }

    /** Productos cuyo nombre contiene el texto, ignorando mayúsculas. */
    public List<Producto> buscarPorNombre(String texto) {
        List<Producto> encontrados = new ArrayList<>();
        if (texto == null) {
            return encontrados;
        }
        String buscado = texto.trim().toLowerCase();
        for (Producto p : productos) {
            if (p.getNombre().toLowerCase().contains(buscado)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }

    /** Copia de la lista ordenada por código. No altera el orden de la colección original. */
    public List<Producto> listarOrdenado() {
        List<Producto> copia = new ArrayList<>(productos);
        Collections.sort(copia, new ComparadorPorCodigo());
        return copia;
    }

    /** Sustituye todo el inventario por la lista recibida (recuperación de un .dat). */
    public void reemplazarTodo(List<Producto> nuevos) {
        productos.clear();
        productos.addAll(nuevos);
    }

    // Compara dos productos por su código, sin distinguir mayúsculas.
    private static class ComparadorPorCodigo implements Comparator<Producto> {
        @Override
        public int compare(Producto a, Producto b) {
            return a.getCodigo().compareToIgnoreCase(b.getCodigo());
        }
    }
}

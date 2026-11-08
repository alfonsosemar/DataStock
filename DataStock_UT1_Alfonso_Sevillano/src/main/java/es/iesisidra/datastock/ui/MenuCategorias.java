package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.bd.CategoriaBD;
import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.modelo.Categoria;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenú 7.2: mantenimiento de categorías (RF-JDBC-02).
 * Solo pide datos y muestra resultados; el SQL está en {@link CategoriaBD}.
 */
class MenuCategorias {

    private static final int MAX_NOMBRE = 60;

    void iniciar() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE CATEGORÍAS ----------");
            System.out.println("1. Dar de alta una categoría");
            System.out.println("2. Listar categorías");
            System.out.println("3. Modificar el nombre de una categoría");
            System.out.println("4. Eliminar una categoría");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1:
                        alta();
                        break;
                    case 2:
                        listar();
                        break;
                    case 3:
                        modificar();
                        break;
                    case 4:
                        eliminar();
                        break;
                    case 0:
                        break;
                    default:
                        throw new IllegalStateException("Opción inesperada");
                }
            } catch (SQLException e) {
                Mensajes.error(ErroresBD.explicar(e));
            }
        } while (opcion != 0);
    }

    private void alta() throws SQLException {
        Mensajes.titulo("ALTA DE CATEGORÍA");
        Integer id = EntradaDatos.pedirId("ID de categoría");
        if (id == null) {
            Mensajes.aviso("Alta cancelada.");
            return;
        }
        if (CategoriaBD.buscar(id) != null) {
            Mensajes.error("Ya existe una categoría con ID " + id + ".");
            return;
        }
        String nombre = EntradaDatos.pedirTexto("Nombre", 1, MAX_NOMBRE);
        if (nombre == null) {
            Mensajes.aviso("Alta cancelada.");
            return;
        }
        if (CategoriaBD.existeNombre(nombre, id)) {
            Mensajes.error("Ya existe otra categoría llamada \"" + nombre + "\".");
            return;
        }
        if (CategoriaBD.insertar(new Categoria(id, nombre)) == 1) {
            Mensajes.ok("Categoría registrada correctamente.");
        }
    }

    private void listar() throws SQLException {
        Mensajes.titulo("LISTADO DE CATEGORÍAS");
        List<Categoria> categorias = CategoriaBD.listar();
        if (categorias.isEmpty()) {
            Mensajes.aviso("No hay categorías registradas.");
            return;
        }
        System.out.printf("%-6s %s%n", "ID", "NOMBRE");
        System.out.println("----------------------------------------");
        for (Categoria c : categorias) {
            System.out.printf("%-6d %s%n", c.getId(), c.getNombre());
        }
        System.out.println("Total: " + categorias.size());
    }

    private void modificar() throws SQLException {
        Mensajes.titulo("MODIFICAR CATEGORÍA");
        Categoria actual = pedirExistente();
        if (actual == null) {
            return;
        }
        System.out.println("Nombre actual: " + actual.getNombre());
        String nombre = EntradaDatos.pedirTexto("Nuevo nombre", 1, MAX_NOMBRE);
        if (nombre == null) {
            Mensajes.aviso("Modificación cancelada.");
            return;
        }
        if (CategoriaBD.existeNombre(nombre, actual.getId())) {
            Mensajes.error("Ya existe otra categoría llamada \"" + nombre + "\".");
            return;
        }
        if (!LectorConsola.confirmar("[AVISO] ¿Cambiar \"" + actual.getNombre() + "\" por \"" + nombre + "\"?")) {
            Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
            return;
        }
        if (CategoriaBD.cambiarNombre(actual.getId(), nombre) == 1) {
            Mensajes.ok("Categoría " + actual.getId() + " actualizada.");
        } else {
            Mensajes.error("La categoría " + actual.getId() + " ya no existe.");
        }
    }

    private void eliminar() throws SQLException {
        Mensajes.titulo("ELIMINAR CATEGORÍA");
        Categoria actual = pedirExistente();
        if (actual == null) {
            return;
        }
        System.out.println("ID: " + actual.getId() + " | Nombre: " + actual.getNombre());
        if (!LectorConsola.confirmar("[AVISO] ¿Confirmas la eliminación?")) {
            Mensajes.aviso("Eliminación cancelada. No se ha borrado nada.");
            return;
        }
        try {
            if (CategoriaBD.eliminar(actual.getId()) == 1) {
                Mensajes.ok("Categoría " + actual.getId() + " eliminada.");
            } else {
                Mensajes.error("La categoría " + actual.getId() + " ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneDependientes(e)) {
                Mensajes.error("No se puede eliminar la categoría " + actual.getId()
                        + ": tiene productos asociados. Se conservan todos los registros.");
            } else {
                throw e;
            }
        }
    }

    // Pide un id y comprueba que existe. Devuelve null si se cancela o no existe.
    private Categoria pedirExistente() throws SQLException {
        Integer id = EntradaDatos.pedirId("ID de categoría");
        if (id == null) {
            Mensajes.aviso("Operación cancelada.");
            return null;
        }
        Categoria categoria = CategoriaBD.buscar(id);
        if (categoria == null) {
            Mensajes.error("No existe la categoría con ID " + id + ".");
        }
        return categoria;
    }
}

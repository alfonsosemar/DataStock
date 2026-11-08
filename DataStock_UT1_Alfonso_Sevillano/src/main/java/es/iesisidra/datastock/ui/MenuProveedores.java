package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.ProveedorBD;
import es.iesisidra.datastock.modelo.Proveedor;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenú 7.3: mantenimiento de proveedores (RF-JDBC-02).
 * Solo pide datos y muestra resultados; el SQL está en {@link ProveedorBD}.
 */
class MenuProveedores {

    private static final int MAX_NOMBRE = 100;
    private static final int MAX_EMAIL = 120;
    // Formato básico: algo@algo.algo, sin espacios.
    private static final String PATRON_EMAIL = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";
    private static final String SIN_EMAIL = "—";

    void iniciar() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE PROVEEDORES ----------");
            System.out.println("1. Dar de alta un proveedor");
            System.out.println("2. Listar proveedores");
            System.out.println("3. Modificar un proveedor");
            System.out.println("4. Eliminar un proveedor");
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
        Mensajes.titulo("ALTA DE PROVEEDOR");
        Integer id = EntradaDatos.pedirId("ID de proveedor");
        if (id == null) {
            Mensajes.aviso("Alta cancelada.");
            return;
        }
        if (ProveedorBD.buscar(id) != null) {
            Mensajes.error("Ya existe un proveedor con ID " + id + ".");
            return;
        }
        String nombre = EntradaDatos.pedirTexto("Nombre", 1, MAX_NOMBRE);
        if (nombre == null) {
            Mensajes.aviso("Alta cancelada.");
            return;
        }
        String email = pedirEmailAlta();
        if (email != null && ProveedorBD.existeEmail(email, id)) {
            Mensajes.error("El email " + email + " ya está registrado en otro proveedor.");
            return;
        }
        if (ProveedorBD.insertar(new Proveedor(id, nombre, email)) == 1) {
            Mensajes.ok("Proveedor registrado correctamente.");
        }
    }

    private void listar() throws SQLException {
        Mensajes.titulo("LISTADO DE PROVEEDORES");
        List<Proveedor> proveedores = ProveedorBD.listar();
        if (proveedores.isEmpty()) {
            Mensajes.aviso("No hay proveedores registrados.");
            return;
        }
        System.out.printf("%-6s %-35s %s%n", "ID", "NOMBRE", "EMAIL");
        System.out.println("------------------------------------------------------------");
        for (Proveedor p : proveedores) {
            System.out.printf("%-6d %-35s %s%n", p.getId(), p.getNombre(), mostrarEmail(p.getEmail()));
        }
        System.out.println("Total: " + proveedores.size());
    }

    private void modificar() throws SQLException {
        Mensajes.titulo("MODIFICAR PROVEEDOR");
        Proveedor actual = pedirExistente();
        if (actual == null) {
            return;
        }
        mostrar(actual);
        System.out.println("Pulsa Intro para conservar el dato actual. En el email, escribe - para dejarlo sin valor.");
        String nombre = EntradaDatos.pedirTextoOConservar("Nombre", actual.getNombre(), 1, MAX_NOMBRE);
        String email = pedirEmailModificacion(actual.getEmail());
        if (email != null && ProveedorBD.existeEmail(email, actual.getId())) {
            Mensajes.error("El email " + email + " ya está registrado en otro proveedor.");
            return;
        }
        Proveedor nuevo = new Proveedor(actual.getId(), nombre, email);
        System.out.println("Nuevos datos -> Nombre: " + nombre + " | Email: " + mostrarEmail(email));
        if (!LectorConsola.confirmar("[AVISO] ¿Guardar los cambios?")) {
            Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
            return;
        }
        if (ProveedorBD.modificar(nuevo) == 1) {
            Mensajes.ok("Proveedor " + actual.getId() + " actualizado.");
        } else {
            Mensajes.error("El proveedor " + actual.getId() + " ya no existe.");
        }
    }

    private void eliminar() throws SQLException {
        Mensajes.titulo("ELIMINAR PROVEEDOR");
        Proveedor actual = pedirExistente();
        if (actual == null) {
            return;
        }
        mostrar(actual);
        if (!LectorConsola.confirmar("[AVISO] ¿Confirmas la eliminación?")) {
            Mensajes.aviso("Eliminación cancelada. No se ha borrado nada.");
            return;
        }
        try {
            if (ProveedorBD.eliminar(actual.getId()) == 1) {
                Mensajes.ok("Proveedor " + actual.getId() + " eliminado.");
            } else {
                Mensajes.error("El proveedor " + actual.getId() + " ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneDependientes(e)) {
                Mensajes.error("No se puede eliminar el proveedor " + actual.getId()
                        + ": tiene productos asociados (integridad referencial). Se conservan todos los registros.");
            } else {
                throw e;
            }
        }
    }

    // En el alta, Intro deja el email vacío (NULL).
    private String pedirEmailAlta() {
        while (true) {
            String texto = LectorConsola.leerTexto("Email (opcional, Intro para dejarlo vacío): ").trim();
            if (texto.isEmpty()) {
                return null;
            }
            String error = errorEmail(texto);
            if (error == null) {
                return texto;
            }
            Mensajes.error(error);
        }
    }

    // En la modificación, Intro conserva el email actual y "-" lo deja a NULL.
    private String pedirEmailModificacion(String actual) {
        while (true) {
            String texto = LectorConsola.leerTexto("Email [" + mostrarEmail(actual) + "] (Intro conserva, - lo borra): ").trim();
            if (texto.isEmpty()) {
                return actual;
            }
            if (texto.equals("-")) {
                return null;
            }
            String error = errorEmail(texto);
            if (error == null) {
                return texto;
            }
            Mensajes.error(error);
        }
    }

    private String errorEmail(String email) {
        if (email.length() > MAX_EMAIL) {
            return "El email no puede superar los " + MAX_EMAIL + " caracteres.";
        }
        if (!email.matches(PATRON_EMAIL)) {
            return "El email no tiene un formato válido (ejemplo: ventas@empresa.com).";
        }
        return null;
    }

    private Proveedor pedirExistente() throws SQLException {
        Integer id = EntradaDatos.pedirId("ID de proveedor");
        if (id == null) {
            Mensajes.aviso("Operación cancelada.");
            return null;
        }
        Proveedor proveedor = ProveedorBD.buscar(id);
        if (proveedor == null) {
            Mensajes.error("No existe el proveedor con ID " + id + ".");
        }
        return proveedor;
    }

    private void mostrar(Proveedor p) {
        System.out.println("ID: " + p.getId() + " | Nombre: " + p.getNombre() + " | Email: " + mostrarEmail(p.getEmail()));
    }

    private String mostrarEmail(String email) {
        return email == null ? SIN_EMAIL : email;
    }
}

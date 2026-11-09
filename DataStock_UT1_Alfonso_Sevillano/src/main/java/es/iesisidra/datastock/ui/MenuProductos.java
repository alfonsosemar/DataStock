package es.iesisidra.datastock.ui;

import java.math.BigDecimal;
import java.sql.SQLException;

import es.iesisidra.datastock.bd.CategoriaBD;
import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.ProductoBD;
import es.iesisidra.datastock.bd.ProveedorBD;
import es.iesisidra.datastock.modelo.FichaProducto;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.ValidadorProducto;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenú 7.4: alta, consulta, modificación y baja de productos (RF-JDBC-03).
 * <p>Los datos se validan en Java antes de enviar el SQL, con las mismas reglas
 * que los CHECK de la tabla. El stock solo se indica en el alta: después
 * cambia únicamente con el menú de movimientos.</p>
 */
class MenuProductos {

    void iniciar() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE PRODUCTOS ----------");
            System.out.println("1. Dar de alta un producto");
            System.out.println("2. Consultar producto por código");
            System.out.println("3. Modificar un producto");
            System.out.println("4. Eliminar un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1:
                        alta();
                        break;
                    case 2:
                        consultar();
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
        Mensajes.titulo("ALTA DE PRODUCTO");
        String codigo = pedirCampo("Código", null);
        if (codigo == null) {
            Mensajes.aviso("Alta cancelada.");
            return;
        }
        codigo = ValidadorProducto.normalizarCodigo(codigo);
        if (ProductoBD.existe(codigo)) {
            Mensajes.error("El producto " + codigo + " ya existe en MySQL. No se sobrescribe.");
            return;
        }
        // Si el usuario pulsa Intro en un campo, se devuelve null y se cancela el alta.
        String nombre = pedirCampo("Nombre", null);
        if (nombre == null) {
            Mensajes.aviso("Alta cancelada. No se ha insertado nada.");
            return;
        }
        String precio = pedirCampo("Precio", null);
        if (precio == null) {
            Mensajes.aviso("Alta cancelada. No se ha insertado nada.");
            return;
        }
        String stock = pedirCampo("Stock inicial", null);
        if (stock == null) {
            Mensajes.aviso("Alta cancelada. No se ha insertado nada.");
            return;
        }
        Integer categoria = pedirReferencia(true, null);
        if (categoria == null) {
            Mensajes.aviso("Alta cancelada. No se ha insertado nada.");
            return;
        }
        Integer proveedor = pedirReferencia(false, null);
        if (proveedor == null) {
            Mensajes.aviso("Alta cancelada. No se ha insertado nada.");
            return;
        }
        Producto p = new Producto(codigo, nombre, new BigDecimal(precio), Integer.parseInt(stock), categoria, proveedor);
        if (ProductoBD.insertar(p) == 1) {
            Mensajes.ok("Producto " + codigo + " guardado correctamente.");
        }
    }

    private void consultar() throws SQLException {
        Mensajes.titulo("CONSULTA DE PRODUCTO");
        FichaProducto ficha = VistaProducto.pedirExistente();
        if (ficha != null) {
            VistaProducto.mostrarFicha(ficha);
        }
    }

    private void modificar() throws SQLException {
        Mensajes.titulo("MODIFICAR PRODUCTO");
        FichaProducto ficha = VistaProducto.pedirExistente();
        if (ficha == null) {
            return;
        }
        Producto actual = ficha.getProducto();
        VistaProducto.mostrarFicha(ficha);
        System.out.println("Pulsa Intro para conservar cada valor. El código y el stock no se modifican aquí.");
        String nombre = pedirCampo("Nombre", actual.getNombre());
        String precio = pedirCampo("Precio", actual.getPrecio().toPlainString());
        int categoria = pedirReferencia(true, actual.getCategoriaId());
        int proveedor = pedirReferencia(false, actual.getProveedorId());

        Producto nuevo = new Producto(actual.getCodigo(), nombre, new BigDecimal(precio),
                actual.getStock(), categoria, proveedor);
        System.out.println("Nuevos datos -> Nombre: " + nombre + " | Precio: " + precio
                + " € | Categoría: " + categoria + " | Proveedor: " + proveedor);
        if (!LectorConsola.confirmar("[AVISO] ¿Guardar los cambios?")) {
            Mensajes.aviso("Modificación cancelada. No se ha cambiado nada.");
            return;
        }
        if (ProductoBD.modificar(nuevo) == 1) {
            Mensajes.ok("Producto " + actual.getCodigo() + " actualizado.");
        } else {
            Mensajes.error("El producto " + actual.getCodigo() + " ya no existe.");
        }
    }

    private void eliminar() throws SQLException {
        Mensajes.titulo("ELIMINAR PRODUCTO");
        FichaProducto ficha = VistaProducto.pedirExistente();
        if (ficha == null) {
            return;
        }
        String codigo = ficha.getProducto().getCodigo();
        VistaProducto.mostrarFicha(ficha);
        if (!LectorConsola.confirmar("[AVISO] ¿Confirmas la eliminación?")) {
            Mensajes.aviso("Eliminación cancelada. El producto sigue en MySQL.");
            return;
        }
        try {
            if (ProductoBD.eliminar(codigo) == 1) {
                Mensajes.ok("Producto " + codigo + " eliminado.");
            } else {
                Mensajes.error("El producto " + codigo + " ya no existe.");
            }
        } catch (SQLException e) {
            if (ErroresBD.tieneDependientes(e)) {
                Mensajes.error("No se puede eliminar " + codigo
                        + ": tiene movimientos de stock registrados. Se conserva el historial.");
            } else {
                throw e;
            }
        }
    }

    /**
     * Pide un campo del producto y lo valida con {@link ValidadorProducto}.
     * Si {@code actual} es null el campo es obligatorio e Intro cancela (devuelve null);
     * si no, Intro conserva el valor actual.
     */
    private String pedirCampo(String campo, String actual) {
        String mensaje;
        if (actual == null) {
            mensaje = campo + EntradaDatos.CANCELAR;
        } else {
            mensaje = campo + " [" + actual + "] (Intro conserva): ";
        }
        while (true) {
            String texto = LectorConsola.leerTexto(mensaje).trim();
            if (texto.isEmpty()) {
                return actual;
            }
            String error = validarCampo(campo, texto);
            if (error == null) {
                return texto;
            }
            Mensajes.error("Dato no válido: " + error + ".");
        }
    }

    private String validarCampo(String campo, String texto) {
        switch (campo) {
            case "Código":
                return ValidadorProducto.errorCodigo(ValidadorProducto.normalizarCodigo(texto));
            case "Nombre":
                return ValidadorProducto.errorNombre(texto);
            case "Precio":
                return ValidadorProducto.errorPrecio(texto);
            case "Stock inicial":
                return ValidadorProducto.errorStock(texto);
            default:
                throw new IllegalArgumentException("Campo desconocido: " + campo);
        }
    }

    /**
     * Pide el id de una categoría (esCategoria) o de un proveedor y comprueba que existe en MySQL.
     * Con {@code actual} null, Intro cancela; si no, Intro conserva el actual.
     */
    private Integer pedirReferencia(boolean esCategoria, Integer actual) throws SQLException {
        String campo;
        if (esCategoria) {
            campo = "ID de categoría";
        } else {
            campo = "ID de proveedor";
        }
        String mensaje;
        if (actual == null) {
            mensaje = campo + EntradaDatos.CANCELAR;
        } else {
            mensaje = campo + " [" + actual + "] (Intro conserva): ";
        }
        while (true) {
            String texto = LectorConsola.leerTexto(mensaje).trim();
            if (texto.isEmpty()) {
                return actual;
            }
            String error = ValidadorProducto.errorId(texto, campo);
            if (error != null) {
                Mensajes.error("Dato no válido: " + error + ".");
                continue;
            }
            int id = Integer.parseInt(texto);
            if (esCategoria) {
                if (CategoriaBD.buscar(id) != null) {
                    return id;
                }
                Mensajes.error("No existe la categoría con ID " + id + ". Dala de alta antes o indica otra.");
            } else {
                if (ProveedorBD.buscar(id) != null) {
                    return id;
                }
                Mensajes.error("No existe el proveedor con ID " + id + ". Dalo de alta antes o indica otro.");
            }
        }
    }
}

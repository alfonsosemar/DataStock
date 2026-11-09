package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.List;

import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.MovimientoBD;
import es.iesisidra.datastock.bd.ProductoBD;
import es.iesisidra.datastock.modelo.FichaProducto;
import es.iesisidra.datastock.modelo.MovimientoStock;
import es.iesisidra.datastock.modelo.MovimientoStock.TipoMovimiento;
import es.iesisidra.datastock.servicio.ValidadorProducto;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenús 7.6 (entradas y salidas de stock, RF-JDBC-05) y 7.7 (historial, RF-JDBC-06).
 */
class MenuMovimientos {

    /** Menú 7.6: registrar una ENTRADA o una SALIDA. */
    void iniciarMovimientos() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE MOVIMIENTOS ----------");
            System.out.println("1. Registrar una ENTRADA de stock");
            System.out.println("2. Registrar una SALIDA de stock");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            try {
                switch (opcion) {
                    case 1:
                        registrar(TipoMovimiento.ENTRADA);
                        break;
                    case 2:
                        registrar(TipoMovimiento.SALIDA);
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

    /** Menú 7.7: consultar el historial (solo lectura). */
    void iniciarHistorial() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE HISTORIAL ----------");
            System.out.println("1. Consultar todos los movimientos");
            System.out.println("2. Consultar movimientos de un producto");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
            try {
                switch (opcion) {
                    case 1:
                        Mensajes.titulo("HISTORIAL COMPLETO");
                        mostrarTabla(MovimientoBD.listar(), "No hay movimientos registrados.");
                        break;
                    case 2:
                        historialProducto();
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

    private void registrar(TipoMovimiento tipo) throws SQLException {
        Mensajes.titulo("REGISTRAR " + tipo);
        FichaProducto ficha = VistaProducto.pedirExistente();
        if (ficha == null) {
            return;
        }
        String codigo = ficha.getProducto().getCodigo();
        System.out.println("Producto: " + codigo + " - " + ficha.getProducto().getNombre());
        System.out.println("Stock actual: " + ficha.getProducto().getStock());

        Integer cantidad = pedirCantidad();
        if (cantidad == null || !LectorConsola.confirmar("[AVISO] ¿Registrar " + tipo + " de " + cantidad + " unidades?")) {
            Mensajes.aviso("Movimiento cancelado. No se ha modificado nada.");
            return;
        }
        try {
            MovimientoStock m = MovimientoBD.registrar(codigo, tipo, cantidad);
            Mensajes.ok("Movimiento " + m.getId() + " registrado: " + m.getTipo() + " de " + m.getCantidad() + " unidades.");
            System.out.println("Stock anterior: " + m.getStockAnterior() + " -> stock posterior: " + m.getStockPosterior());
        } catch (SQLException e) {
            // El procedimiento ha hecho ROLLBACK: ni el stock ni el historial han cambiado.
            Mensajes.error(ErroresBD.explicar(e) + " El stock de " + codigo + " permanece en "
                    + ProductoBD.buscar(codigo).getProducto().getStock() + ".");
        }
    }

    // Cantidad entera estrictamente positiva. Devuelve null si se cancela.
    private Integer pedirCantidad() {
        while (true) {
            String texto = LectorConsola.leerTexto("Cantidad" + EntradaDatos.CANCELAR).trim();
            if (texto.isEmpty()) {
                return null;
            }
            try {
                int cantidad = Integer.parseInt(texto);
                if (cantidad > 0) {
                    return cantidad;
                }
                Mensajes.error("La cantidad debe ser mayor que 0.");
            } catch (NumberFormatException e) {
                Mensajes.error("\"" + texto + "\" no es un número entero.");
            }
        }
    }

    private void historialProducto() throws SQLException {
        Mensajes.titulo("HISTORIAL DE UN PRODUCTO");
        String codigo = ValidadorProducto.normalizarCodigo(LectorConsola.leerTexto("Código del producto" + EntradaDatos.CANCELAR));
        if (codigo.isEmpty()) {
            Mensajes.aviso("Consulta cancelada.");
            return;
        }
        if (!ProductoBD.existe(codigo)) {
            Mensajes.aviso("No existe ningún producto con el código " + codigo + ".");
            return;
        }
        mostrarTabla(MovimientoBD.listarPorProducto(codigo), "Sin movimientos registrados para " + codigo + ".");
    }

    private void mostrarTabla(List<MovimientoStock> movimientos, String avisoSiVacia) {
        if (movimientos.isEmpty()) {
            Mensajes.aviso(avisoSiVacia);
            return;
        }
        String formato = "%-5s %-19s %-12s %-8s %8s %9s %10s%n";
        System.out.printf(formato, "ID", "FECHA", "PRODUCTO", "TIPO", "CANTIDAD", "ANTERIOR", "POSTERIOR");
        System.out.println("-----------------------------------------------------------------------------");
        for (MovimientoStock m : movimientos) {
            System.out.printf(formato, m.getId(), m.getFecha().toString().replace('T', ' '), m.getProductoCodigo(),
                    m.getTipo(), m.getCantidad(), m.getStockAnterior(), m.getStockPosterior());
        }
        System.out.println("Total de movimientos: " + movimientos.size());
    }
}

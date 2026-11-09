package es.iesisidra.datastock.ui;

import java.sql.SQLException;

import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.ProductoBD;
import es.iesisidra.datastock.modelo.FichaProducto;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Submenú 7.5: consultas de productos en MySQL (RF-JDBC-04).
 * Son operaciones de solo lectura: ninguna modifica registros.
 */
class MenuConsultasBD {

    void iniciar() {
        int opcion;
        do {
            System.out.println("\n---------- MENÚ DE CONSULTAS ----------");
            System.out.println("1. Listar todos los productos");
            System.out.println("2. Buscar producto por código");
            System.out.println("3. Buscar productos por nombre");
            System.out.println("4. Consultar productos con stock inferior a un umbral");
            System.out.println("0. Volver al menú MySQL");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1:
                        Mensajes.titulo("PRODUCTOS ALMACENADOS EN MYSQL");
                        VistaProducto.mostrarTabla(ProductoBD.listar(), "No hay productos almacenados en MySQL.");
                        break;
                    case 2:
                        buscarPorCodigo();
                        break;
                    case 3:
                        buscarPorNombre();
                        break;
                    case 4:
                        stockBajo();
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

    private void buscarPorCodigo() throws SQLException {
        Mensajes.titulo("BUSCAR POR CÓDIGO");
        FichaProducto ficha = VistaProducto.pedirExistente();
        if (ficha != null) {
            VistaProducto.mostrarFicha(ficha);
        }
    }

    private void buscarPorNombre() throws SQLException {
        Mensajes.titulo("BUSCAR POR NOMBRE");
        String texto = LectorConsola.leerTexto("Texto del nombre" + EntradaDatos.CANCELAR).trim();
        if (texto.isEmpty()) {
            Mensajes.aviso("Búsqueda cancelada.");
            return;
        }
        VistaProducto.mostrarTabla(ProductoBD.buscarPorNombre(texto),
                "No hay productos cuyo nombre contenga \"" + texto + "\".");
    }

    private void stockBajo() throws SQLException {
        Mensajes.titulo("STOCK INFERIOR A UN UMBRAL");
        int umbral;
        while (true) {
            umbral = LectorConsola.leerEntero("Umbral (entero mayor o igual que 0): ");
            if (umbral >= 0) {
                break;
            }
            Mensajes.error("El umbral no puede ser negativo.");
        }
        VistaProducto.mostrarTabla(ProductoBD.stockMenorQue(umbral),
                "No hay productos con stock inferior a " + umbral + ".");
    }
}

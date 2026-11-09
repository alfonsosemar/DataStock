package es.iesisidra.datastock.ui;

import java.sql.SQLException;

import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.EsquemaBD;
import es.iesisidra.datastock.bd.InfoConexion;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import es.iesisidra.datastock.util.Mensajes;

/**
 * Menú 7: gestión de la base de datos MySQL (UT2).
 * <p>Trabaja directamente con MySQL. El inventario en memoria de la UT1 solo se
 * usa en la opción 8, que copia sus productos a la base de datos cuando el
 * usuario lo pide; no hay sincronización automática.</p>
 */
public class MenuMySQL {

    private final Inventario inventario;

    /** @param inventario colección en memoria de la UT1 (solo la lee la opción 8). */
    public MenuMySQL(Inventario inventario) {
        this.inventario = inventario;
    }

    /** Muestra el menú hasta que se elige volver al menú principal. */
    public void iniciar() {
        int opcion;
        do {
            System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
            System.out.println(ColoresConsola.pintar("              GESTIÓN MYSQL - UT2", ColoresConsola.NEGRITA));
            System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
            System.out.println("1. Comprobar conexión y mostrar información de la BD");
            System.out.println("2. Gestionar categorías");
            System.out.println("3. Gestionar proveedores");
            System.out.println("4. Gestionar productos (CRUD)");
            System.out.println("5. Consultar y buscar productos");
            System.out.println("6. Registrar entrada o salida de stock");
            System.out.println("7. Consultar historial de movimientos");
            System.out.println("8. Importar productos de memoria a MySQL");
            System.out.println("9. Demostración de transacción con commit/rollback");
            System.out.println("0. Volver al menú principal");
            opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 9);
            switch (opcion) {
                case 1:
                    comprobarConexion();
                    break;
                case 2:
                    new MenuCategorias().iniciar();
                    break;
                case 3:
                    new MenuProveedores().iniciar();
                    break;
                case 4:
                    new MenuProductos().iniciar();
                    break;
                case 5:
                    new MenuConsultasBD().iniciar();
                    break;
                case 6:
                    new MenuMovimientos().iniciarMovimientos();
                    break;
                case 7:
                    new MenuMovimientos().iniciarHistorial();
                    break;
                case 8:
                case 9:
                    Mensajes.aviso("Opción pendiente de implementar.");
                    break;
                case 0:
                    break;
                default:
                    throw new IllegalStateException("Opción inesperada");
            }
        } while (opcion != 0);
    }

    // ---------- RF-JDBC-01 ----------
    private void comprobarConexion() {
        try {
            InfoConexion info = EsquemaBD.comprobar();
            Mensajes.ok("Conexión establecida correctamente.");
            System.out.println("Base de datos: " + info.getBaseDatos());
            System.out.println("Gestor: " + info.getGestor());
            System.out.println("Versión: " + info.getVersion());
            System.out.println("Driver JDBC: " + info.getDriver());
            System.out.println("Productos almacenados: " + info.getProductos());
            if (info.isProcedimientoDisponible()) {
                System.out.println("Procedimiento registrar_movimiento: disponible");
            } else {
                Mensajes.error("No existe el procedimiento registrar_movimiento. Ejecuta DataStock_BD_MySQL.sql.");
            }
        } catch (SQLException e) {
            Mensajes.error("No se ha podido conectar. " + ErroresBD.explicar(e));
        }
    }
}

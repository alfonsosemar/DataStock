package es.iesisidra.datastock.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import es.iesisidra.datastock.bd.DemoTransaccionBD;
import es.iesisidra.datastock.bd.ErroresBD;
import es.iesisidra.datastock.bd.EsquemaBD;
import es.iesisidra.datastock.bd.ImportacionBD;
import es.iesisidra.datastock.bd.InfoConexion;
import es.iesisidra.datastock.bd.ProductoBD;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ValidadorProducto;
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
                    importarDesdeMemoria();
                    break;
                case 9:
                    demostrarTransaccion();
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

    // ---------- RF-JDBC-07 ----------
    private void importarDesdeMemoria() {
        Mensajes.titulo("IMPORTAR PRODUCTOS DE MEMORIA A MYSQL");
        if (inventario.estaVacio()) {
            Mensajes.aviso("No hay productos en memoria para importar. Importa antes un JSON o XML (opciones 2 y 3).");
            return;
        }
        System.out.println("Productos disponibles en memoria: " + inventario.cantidad());
        if (!LectorConsola.confirmar("[AVISO] ¿Deseas importar estos productos a MySQL?")) {
            Mensajes.aviso("Importación cancelada. No se ha modificado MySQL.");
            return;
        }
        try {
            List<Producto> aceptados = new ArrayList<>();
            int omitidos = 0;
            int rechazados = 0;
            System.out.println("Iniciando comprobación de productos...");
            for (Producto p : inventario.listarOrdenado()) {
                System.out.println("\nComprobando " + p.getCodigo() + " - " + p.getNombre() + "...");
                List<String> errores = ValidadorProducto.validar(p);
                if (!errores.isEmpty()) {
                    Mensajes.error("Datos incorrectos: " + String.join("; ", errores) + ". El producto no se puede importar.");
                    rechazados++;
                    continue;
                }
                if (ProductoBD.existe(p.getCodigo())) {
                    Mensajes.aviso("El producto ya existe en MySQL. Se omite.");
                    omitidos++;
                    continue;
                }
                String motivo = ImportacionBD.motivoReferencias(p);
                if (motivo != null) {
                    Mensajes.error(motivo + " El producto no se puede importar.");
                    rechazados++;
                    continue;
                }
                Mensajes.ok("Producto válido para importar.");
                aceptados.add(p);
            }

            int insertados = 0;
            boolean huboRollback = false;
            if (!aceptados.isEmpty()) {
                System.out.println();
                if (!LectorConsola.confirmar("[AVISO] Se van a insertar " + aceptados.size()
                        + " productos en una única transacción. ¿Continuar?")) {
                    Mensajes.aviso("Importación cancelada. No se ha insertado ningún producto.");
                    return;
                }
                try {
                    insertados = ImportacionBD.insertarLote(aceptados);
                } catch (SQLException e) {
                    huboRollback = true;
                    Mensajes.error("Error SQL durante el lote: " + ErroresBD.explicar(e));
                    Mensajes.error("Se ha hecho ROLLBACK: no se ha confirmado ninguna inserción del lote ("
                            + aceptados.size() + " productos).");
                }
            }

            System.out.println("\n------------------------------------------------");
            System.out.println("RESULTADO DE LA IMPORTACIÓN");
            System.out.println("------------------------------------------------");
            System.out.println("Productos analizados:              " + inventario.cantidad());
            System.out.println("Productos insertados:              " + insertados);
            System.out.println("Omitidos por código duplicado:     " + omitidos);
            System.out.println("Rechazados por datos incorrectos:  " + rechazados);
            if (huboRollback) {
                Mensajes.error("Importación revertida.");
            } else {
                Mensajes.ok("Importación finalizada. El inventario en memoria no se ha modificado.");
            }
        } catch (SQLException e) {
            Mensajes.error(ErroresBD.explicar(e) + " No se ha insertado ningún producto.");
        }
    }

    // ---------- RF-JDBC-08 ----------
    private void demostrarTransaccion() {
        Mensajes.titulo("DEMOSTRACIÓN DE TRANSACCIONES JDBC");
        int a1 = DemoTransaccionBD.ID_A1;
        int a2 = DemoTransaccionBD.ID_A2;
        int b = DemoTransaccionBD.ID_B;
        System.out.println("Se usan categorías de prueba que no afectan a los datos reales:");
        System.out.println(" Escenario A (commit):   insertar las categorías " + a1 + " y " + a2 + ".");
        System.out.println(" Escenario B (rollback): insertar la categoría " + b + " dos veces (clave duplicada).");
        if (!LectorConsola.confirmar("[AVISO] ¿Comenzar la demostración?")) {
            Mensajes.aviso("Demostración cancelada.");
            return;
        }
        try {
            // Por si quedaron categorías de una demostración anterior.
            DemoTransaccionBD.eliminarCategoriasDemo();

            System.out.println("\n--- Escenario A: setAutoCommit(false), 2 INSERT y commit() ---");
            DemoTransaccionBD.escenarioCommit();
            if (DemoTransaccionBD.existe(a1) && DemoTransaccionBD.existe(a2)) {
                Mensajes.ok("Commit realizado: el SELECT encuentra las categorías " + a1 + " y " + a2 + ".");
            } else {
                Mensajes.error("Tras el commit no aparecen las dos categorías.");
            }

            System.out.println("\n--- Escenario B: INSERT correcto, INSERT duplicado y rollback() ---");
            SQLException error = DemoTransaccionBD.escenarioRollback();
            if (error == null) {
                Mensajes.error("No se produjo el error esperado.");
            } else {
                Mensajes.error("Error esperado capturado: " + ErroresBD.explicar(error));
                if (DemoTransaccionBD.existe(b)) {
                    Mensajes.error("La categoría " + b + " sigue en MySQL: el rollback no ha funcionado.");
                } else {
                    Mensajes.ok("Rollback correcto: la primera inserción (categoría " + b + ") tampoco está en MySQL.");
                }
            }
            System.out.println("\nautoCommit restaurado a true y conexiones cerradas.");

            if (LectorConsola.confirmar("[AVISO] ¿Eliminar ahora las categorías de demostración?")) {
                int borradas = DemoTransaccionBD.eliminarCategoriasDemo();
                Mensajes.ok("Categorías de demostración eliminadas: " + borradas + ".");
            } else {
                Mensajes.aviso("Se conservan. Se borrarán solas la próxima vez que repitas la demostración.");
            }
        } catch (SQLException e) {
            Mensajes.error(ErroresBD.explicar(e));
        }
    }
}

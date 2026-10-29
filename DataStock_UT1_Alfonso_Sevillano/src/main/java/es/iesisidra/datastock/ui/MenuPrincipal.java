package es.iesisidra.datastock.ui;

import es.iesisidra.datastock.ficheros.ExploradorRutas;
import es.iesisidra.datastock.ficheros.ExportadorJson;
import es.iesisidra.datastock.ficheros.ExportadorXml;
import es.iesisidra.datastock.ficheros.ImportadorJson;
import es.iesisidra.datastock.ficheros.ImportadorXml;
import es.iesisidra.datastock.ficheros.OperacionFicheroException;
import es.iesisidra.datastock.ficheros.PersistenciaInventario;
import es.iesisidra.datastock.ficheros.Rutas;
import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.servicio.ResultadoImportacion;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Presentación de menús y navegación de la primera fase de DataStock.
 * <p>
 * Los métodos marcados TODO son puntos de partida, NO soluciones de los
 * requisitos. El alumnado deberá delegar el acceso a ficheros, la validación y
 * el procesamiento en métodos o clases independientes.</p>
 */


      public class MenuPrincipal {

        private final Inventario inventario;

        /**
         * @param inventario colección común de productos de esta ejecución.
         */
        public MenuPrincipal(Inventario inventario) {
            this.inventario = inventario;
        }

        /**
         * Ejecuta el menú principal hasta que el usuario selecciona salir.
         */
        public void iniciar() {
            int opcion;
            do {
                mostrarCabecera();
                System.out.println("1. Explorar ficheros y directorios");
                System.out.println("2. Importar productos desde JSON");
                System.out.println("3. Importar productos desde XML");
                System.out.println("4. Consultar productos");
                System.out.println("5. Exportar productos");
                System.out.println("6. Guardar y recuperar inventario");
                System.out.println("0. Salir");
                opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 6);
                switch (opcion) {
                    case 1 ->
                        explorarRuta();
                    case 2 ->
                        importarJson();
                    case 3 ->
                        importarXml();
                    case 4 ->
                        menuConsultas();
                    case 5 ->
                        menuExportaciones();
                    case 6 ->
                        menuSerializacion();
                    case 0 ->
                        System.out.println(ColoresConsola.pintar("Hasta pronto.", ColoresConsola.VERDE));
                    default ->
                        throw new IllegalStateException("Opción inesperada");
                }
            } while (opcion != 0);
        }

        private void mostrarCabecera() {
            System.out.println(ColoresConsola.pintar("\n================================================", ColoresConsola.CIAN));
            System.out.println(ColoresConsola.pintar("         DATASTOCK - GESTIÓN DE INVENTARIO", ColoresConsola.NEGRITA));
            System.out.println(ColoresConsola.pintar("                 FASE 1 - UT1", ColoresConsola.CIAN));
            System.out.println(ColoresConsola.pintar("================================================", ColoresConsola.CIAN));
            System.out.println(ColoresConsola.pintar("Productos en memoria: " + inventario.getProductos().size(), ColoresConsola.GRIS));
        }

        private void menuConsultas() {
            int opcion;
            do {
                System.out.println("\n---------- CONSULTA DE PRODUCTOS ----------");
                System.out.println("1. Listar todos los productos");
                System.out.println("2. Buscar producto por código");
                System.out.println("3. Buscar productos por nombre");
                System.out.println("0. Volver al menú principal");
                opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 3);
                switch (opcion) {
                    case 1 ->
                        listarProductos();
                    case 2 ->
                        buscarPorCodigo();
                    case 3 ->
                        buscarPorNombre();
                    case 0 -> {
                    }
                    default ->
                        throw new IllegalStateException("Opción inesperada");
                }
            } while (opcion != 0);
        }

        private void menuExportaciones() {
            int opcion;
            do {
                System.out.println("\n---------- EXPORTAR PRODUCTOS ----------");
                System.out.println("1. Exportar productos a JSON");
                System.out.println("2. Exportar productos a XML");
                System.out.println("0. Volver al menú principal");
                opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
                switch (opcion) {
                    case 1 ->
                        exportar(true);
                    case 2 ->
                        exportar(false);
                    case 0 -> 
                    default ->
                        throw new IllegalStateException("Opción inesperada");
                }
            } while (opcion != 0);
        }

        private void menuSerializacion() {
            int opcion;
            do {
                System.out.println("\n---------- GUARDAR Y RECUPERAR INVENTARIO ----------");
                System.out.println("1. Guardar inventario en fichero .dat");
                System.out.println("2. Recuperar inventario desde fichero .dat");
                System.out.println("0. Volver al menú principal");
                opcion = LectorConsola.leerOpcion("Selecciona una opción: ", 0, 2);
                switch (opcion) {
                    case 1 ->
                        guardarInventario();
                    case 2 ->
                        recuperarInventario();
                    case 0 -> {
                    }
                    default ->
                        throw new IllegalStateException("Opción inesperada");
                }
            } while (opcion != 0);
        }

    }// ---------- RF-FIC-01 ----------

    private void explorarRuta() {
        String ruta = LectorConsola.leerTexto("Ruta: ");
        ExploradorRutas.explorar(ruta);
    }

    // ---------- RF-FIC-02 y RF-FIC-03 ----------
    private void importarJson() {
        String ruta = LectorConsola.leerTexto("Ruta del fichero JSON: ");
        try {
            ResultadoImportacion resultado = new ImportadorJson(inventario).importar(ruta);
            resultado.mostrar("IMPORTAR JSON", ruta.trim());
        } catch (OperacionFicheroException e) {
            error(e.getMessage() + " No se ha importado ningún producto.");
        }
    }

    private void importarXml() {
        String ruta = LectorConsola.leerTexto("Ruta del fichero XML: ");
        try {
            ResultadoImportacion resultado = new ImportadorXml(inventario).importar(ruta);
            resultado.mostrar("IMPORTAR XML", ruta.trim());
        } catch (OperacionFicheroException e) {
            error(e.getMessage() + " No se ha importado ningún producto.");
        }
    }

    // ---------- RF-FIC-04 ----------
    private void listarProductos() {
        if (inventario.estaVacio()) {
            aviso("El inventario está vacío. Importa un catálogo primero.");
            return;
        }
        System.out.println("\n===== LISTADO DE PRODUCTOS =====");
        for (Producto p : inventario.listarOrdenado()) {
            System.out.printf("%-12s %-30s %10s € | stock %5d | cat. %d | prov. %d%n",
                    p.getCodigo(), p.getNombre(), p.getPrecio().toPlainString(), p.getStock(),
                    p.getCategoriaId(), p.getProveedorId());
        }
        System.out.println("Total: " + inventario.cantidad() + " productos");
    }

    private void buscarPorCodigo() {
        String codigo = LectorConsola.leerTexto("Código: ");
        if (codigo.trim().isEmpty()) {
            aviso("Debes escribir un código.");
            return;
        }
        Producto p = inventario.buscarPorCodigo(codigo);
        if (p == null) {
            aviso("No existe ningún producto con el código " + codigo.trim() + ".");
            return;
        }
        System.out.println("Código: " + p.getCodigo());
        System.out.println("Nombre: " + p.getNombre());
        System.out.println("Precio: " + p.getPrecio().toPlainString() + " € | Stock: " + p.getStock());
        System.out.println("Categoría: " + p.getCategoriaId() + " | Proveedor: " + p.getProveedorId());
    }

    private void buscarPorNombre() {
        String texto = LectorConsola.leerTexto("Texto del nombre: ");
        if (texto.trim().isEmpty()) {
            aviso("Debes escribir un texto de búsqueda.");
            return;
        }
        List<Producto> encontrados = inventario.buscarPorNombre(texto);
        if (encontrados.isEmpty()) {
            aviso("No hay productos cuyo nombre contenga \"" + texto.trim() + "\".");
            return;
        }
        for (Producto p : encontrados) {
            System.out.println(p.getCodigo() + " | " + p.getNombre() + " | " + p.getPrecio().toPlainString()
                    + " € | stock " + p.getStock());
        }
        System.out.println("Coincidencias: " + encontrados.size());
    }

    // ---------- RF-FIC-05 ----------
    private void exportar(boolean enJson) {
        if (inventario.estaVacio()) {
            aviso("El inventario está vacío: no hay nada que exportar.");
            return;
        }
        String ruta = LectorConsola.leerTexto("Ruta de destino: ");
        try {
            Path destino = Rutas.destinoEscritura(ruta);
            if (Files.exists(destino) && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribir?")) {
                aviso("Exportación cancelada. El fichero original no se ha modificado.");
                return;
            }
            List<Producto> ordenados = inventario.listarOrdenado();
            int total;
            if (enJson) {
                total = ExportadorJson.exportar(ordenados, destino);
            } else {
                total = ExportadorXml.exportar(ordenados, destino);
            }
            System.out.println(ColoresConsola.pintar("Fichero generado correctamente.", ColoresConsola.VERDE));
            System.out.println("Ruta: " + destino + " | Formato: " + (enJson ? "JSON" : "XML"));
            System.out.println("Productos exportados: " + total);
        } catch (OperacionFicheroException e) {
            error(e.getMessage());
        }
    }

    // ---------- RF-FIC-06 ----------
    private void guardarInventario() {
        if (inventario.estaVacio()) {
            aviso("El inventario está vacío: no se crea ningún fichero.");
            return;
        }
        String ruta = LectorConsola.leerTexto("Ruta de destino (.dat): ");
        try {
            Path destino = Rutas.destinoEscritura(ruta);
            if (Files.exists(destino) && !LectorConsola.confirmar("El fichero ya existe. ¿Sobrescribir?")) {
                aviso("Operación cancelada. El fichero original no se ha modificado.");
                return;
            }
            PersistenciaInventario.guardar(inventario.getProductos(), destino);
            System.out.println(ColoresConsola.pintar("Inventario guardado correctamente.", ColoresConsola.VERDE));
            System.out.println("Productos guardados: " + inventario.cantidad());
        } catch (OperacionFicheroException e) {
            error(e.getMessage());
        }
    }

    private void recuperarInventario() {
        String ruta = LectorConsola.leerTexto("Ruta del fichero .dat: ");
        try {
            Path origen = Rutas.ficheroLegible(ruta);

            if (!inventario.estaVacio()) {
                System.out.println("Actualmente hay " + inventario.cantidad() + " productos en memoria.");
                System.out.println("La recuperación sustituirá el inventario actual.");
                if (!LectorConsola.confirmar("¿Continuar?")) {
                    aviso("Recuperación cancelada. El inventario no ha cambiado.");
                    return;
                }
            }

            // Si recuperar() falla lanza la excepción y el inventario NO se modifica
            List<Producto> recuperados = PersistenciaInventario.recuperar(origen);
            inventario.reemplazarTodo(recuperados);
            System.out.println(ColoresConsola.pintar("Inventario recuperado correctamente.", ColoresConsola.VERDE));
            System.out.println("Productos disponibles: " + inventario.cantidad());
        } catch (OperacionFicheroException e) {
            error(e.getMessage() + " El inventario no ha cambiado.");
        }
    }

    // ---------- Mensajes ----------
    private void error(String mensaje) {
        System.out.println(ColoresConsola.pintar(mensaje, ColoresConsola.ROJO));
    }

    private void aviso(String mensaje) {
        System.out.println(ColoresConsola.pintar(mensaje, ColoresConsola.AMARILLO));
    }
}

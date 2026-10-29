
import es.iesisidra.datastock.ficheros.ImportadorJson;
import es.iesisidra.datastock.ficheros.ImportadorXml;
import es.iesisidra.datastock.ficheros.OperacionFicheroException;
import es.iesisidra.datastock.ficheros.PersistenciaInventario;
import es.iesisidra.datastock.ficheros.RegistroStock;
import es.iesisidra.datastock.ficheros.Rutas;
import es.iesisidra.datastock.ficheros.StockBinario;
import es.iesisidra.datastock.servicio.Inventario;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * RF-FIC-08: acceso aleatorio con RandomAccessFile sobre stock.bin (36 bytes
 * por registro). Actividad independiente del menú principal. stock.bin es una
 * COPIA: modificarlo no cambia ningún producto en memoria.
 */
public final class AccesoAleatorioActividad {

    private static final Path FICHERO = Paths.get("datos", "salidas", "stock.bin");

    private AccesoAleatorioActividad() {
    }

    public static void main(String[] args) {
        int opcion;
        do {
            System.out.println("\n===== ACCESO ALEATORIO (stock.bin) =====");
            System.out.println("1. Generar stock.bin desde un catálogo");
            System.out.println("2. Consultar un registro por posición");
            System.out.println("3. Modificar el stock de un registro");
            System.out.println("4. Mostrar todos los registros");
            System.out.println("0. Salir");
            opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);
            try {
                switch (opcion) {
                    case 1:
                        generar();
                    case 2:
                        consultar();
                    case 3:
                        modificar();
                    case 4:
                        mostrarTodos();
                    case 0:
                        System.out.println("Fin de la actividad.");
                    default:
                        throw new IllegalStateException("Opción inesperada");
                }
            } catch (OperacionFicheroException e) {
                System.out.println(ColoresConsola.pintar(e.getMessage(), ColoresConsola.ROJO));
            }
        } while (opcion != 0);
    }

    // Genera stock.bin con una copia de los productos del catálogo elegido.
    private static void generar() throws OperacionFicheroException {
        Inventario copia = cargarCopia();
        if (copia.estaVacio()) {
            System.out.println(ColoresConsola.pintar("No hay productos: no se genera el fichero.", ColoresConsola.AMARILLO));
            return;
        }
        if (Files.exists(FICHERO) && !LectorConsola.confirmar("stock.bin ya existe. ¿Sobrescribir?")) {
            System.out.println("Operación cancelada.");
            return;
        }
        Path carpeta = FICHERO.toAbsolutePath().getParent();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new OperacionFicheroException("No se pudo crear la carpeta " + carpeta + ": " + e.getMessage(), e);
        }
        StockBinario.generar(copia.listarOrdenado(), FICHERO);
        System.out.println("Fichero generado: " + FICHERO + " (" + copia.cantidad() + " registros, "
                + (copia.cantidad() * StockBinario.TAM_REGISTRO) + " bytes)");
    }

    private static void consultar() throws OperacionFicheroException {
        long total = StockBinario.contarRegistros(exigirFichero());
        long posicion = pedirPosicion(total);
        RegistroStock registro = StockBinario.leerRegistro(FICHERO, posicion);
        System.out.println(registro);
    }

    private static void modificar() throws OperacionFicheroException {
        long total = StockBinario.contarRegistros(exigirFichero());
        long posicion = pedirPosicion(total);
        int nuevoStock = LectorConsola.leerEntero("Nuevo stock: ");
        int anterior = StockBinario.modificarStock(FICHERO, posicion, nuevoStock);
        System.out.println("Actualización correcta: stock " + anterior + " → " + nuevoStock);
    }

    private static void mostrarTodos() throws OperacionFicheroException {
        exigirFichero();
        List<RegistroStock> registros = StockBinario.leerTodos(FICHERO);
        for (int i = 0; i < registros.size(); i++) {
            System.out.println("[" + i + "] " + registros.get(i));
        }
        System.out.println("Registros: " + registros.size() + " | Tamaño: "
                + (registros.size() * StockBinario.TAM_REGISTRO) + " bytes");
    }

    // ---------- Auxiliares ----------
    private static Path exigirFichero() throws OperacionFicheroException {
        return Rutas.ficheroLegible(FICHERO.toString());
    }

    // Se pide la posición tal cual; si está fuera de rango, StockBinario la rechaza con un mensaje.
    private static long pedirPosicion(long total) {
        return LectorConsola.leerEntero("Posición (0-" + (total - 1) + "): ");
    }

    // Carga los productos desde JSON, XML o un .dat para trabajar con una copia.
    private static Inventario cargarCopia() throws OperacionFicheroException {
        System.out.println("Origen de los productos: 1. JSON | 2. XML | 3. Inventario .dat");
        int origen = LectorConsola.leerOpcion("Opción: ", 1, 3);
        String ruta = LectorConsola.leerTexto("Ruta: ");

        Inventario copia = new Inventario();
        if (origen == 1) {
            new ImportadorJson(copia).importar(ruta);
        } else if (origen == 2) {
            new ImportadorXml(copia).importar(ruta);
        } else {
            copia.reemplazarTodo(PersistenciaInventario.recuperar(Rutas.ficheroLegible(ruta)));
        }
        return copia;
    }
}

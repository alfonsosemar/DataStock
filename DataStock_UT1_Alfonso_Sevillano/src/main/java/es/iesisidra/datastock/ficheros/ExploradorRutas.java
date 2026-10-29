package es.iesisidra.datastock.ficheros;

import es.iesisidra.datastock.util.ColoresConsola;
import java.io.File;
import java.util.Arrays;

/** RF-FIC-01: muestra información de una ruta (fichero o directorio). */
public final class ExploradorRutas {

    private ExploradorRutas() { }

    public static void explorar(String ruta) {
        // 1. La ruta no puede estar vacía
        if (ruta == null || ruta.trim().isEmpty()) {
            mostrarError("La ruta está vacía.");
            return;
        }

        try {
            File elemento = new File(ruta.trim());

            // 2. Debe existir
            if (!elemento.exists()) {
                mostrarError("La ruta no existe: " + ruta.trim());
                return;
            }

            // 3. Si es un fichero: nombre, ruta absoluta y tamaño
            if (elemento.isFile()) {
                System.out.println("\n===== EXPLORAR RUTA =====");
                System.out.println("[FICHERO] " + elemento.getName());
                System.out.println("Ruta absoluta: " + elemento.getAbsolutePath());
                System.out.println("Tamaño: " + elemento.length() + " bytes");
                return;
            }

            // 4. Si es un directorio: se listan solo sus hijos inmediatos
            File[] hijos = elemento.listFiles();
            if (hijos == null) {
                mostrarError("No se puede acceder al directorio (¿faltan permisos?): " + ruta.trim());
                return;
            }

            System.out.println("\n===== EXPLORAR RUTA =====");
            System.out.println("Ruta: " + ruta.trim());
            if (hijos.length == 0) {
                System.out.println("El directorio está vacío.");
            }

            Arrays.sort(hijos); // orden alfabético para que la salida sea estable
            int ficheros = 0;
            int directorios = 0;
            for (File hijo : hijos) {
                if (hijo.isDirectory()) {
                    System.out.println("[DIR] " + hijo.getName());
                    directorios++;
                } else {
                    System.out.println("[FICHERO] " + hijo.getName() + " " + hijo.length() + " bytes");
                    ficheros++;
                }
            }
            System.out.println("Total ficheros: " + ficheros + " | Directorios: " + directorios);

        } catch (SecurityException e) {
            mostrarError("Acceso denegado por el sistema: " + e.getMessage());
        }
    }

    private static void mostrarError(String mensaje) {
        System.out.println(ColoresConsola.pintar(mensaje, ColoresConsola.ROJO));
    }
}

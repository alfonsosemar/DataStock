package es.iesisidra.datastock.servicio;
import java.util.ArrayList;
    import java.util.List ;
    /**
     * Contadores e incidencias de una importación (JSON o XML).
     */
    public class ResultadoImportacion {

        private int encontrados;
        private int importados;
        private int rechazados;
        private final List<String> incidencias = new ArrayList<>();

        public void setEncontrados(int encontrados) {
            this.encontrados = encontrados;
        }

        public void registrarImportado() {
            importados++;
        }

        /**
         * Cuenta un rechazo y guarda el motivo, por ejemplo "TEC-004: el precio
         * debe ser mayor que 0".
         */
        public void registrarRechazo(String identificador, String motivo) {
            rechazados++;
            incidencias.add(identificador + ": " + motivo);
        }

        public int getEncontrados() {
            return encontrados;
        }

        public int getImportados() {
            return importados;
        }

        public int getRechazados() {
            return rechazados;
        }

        public List<String> getIncidencias() {
            return incidencias;
        }

        /**
         * Muestra el resumen con el formato del enunciado.
         */
        public void mostrar(String titulo, String ruta) {
            System.out.println("\n===== " + titulo + " =====");
            System.out.println("Ruta: " + ruta);
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);
            if (!incidencias.isEmpty()) {
                System.out.println("INCIDENCIAS:");
                for (String incidencia : incidencias) {
                    System.out.println("- " + incidencia);
                }
                System.out.println("Importación finalizada con incidencias.");
            } else {
                System.out.println("Importación finalizada.");
            }
        }
    }

}

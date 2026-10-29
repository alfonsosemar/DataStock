
package es.iesisidra.datastock.complementarias;

import java.math.BigDecimal;
import java.nio.file.Path;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.iesisidra.datastock.ficheros.OperacionFicheroException;
import es.iesisidra.datastock.ficheros.Rutas;
import es.iesisidra.datastock.ficheros.XmlSeguro;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

/**
 * RF-FIC-07: consultas XPath sobre un catálogo XML, sin importarlo al inventario.
 * Actividad independiente del menú principal (se ejecuta con su propio main).
 */
public final class XPathActividad {

    private XPathActividad() { }

    public static void main(String[] args) {
        System.out.println("===== CONSULTAS XPATH =====");
        String ruta = LectorConsola.leerTexto("Fichero: ");

        try {
            // 1. Abrir el XML (DOM seguro) y comprobar la raíz
            Path path = Rutas.ficheroLegible(ruta);
            Document documento = XmlSeguro.leer(path);
            if (!"catalogo".equals(documento.getDocumentElement().getTagName())) {
                throw new OperacionFicheroException("La raíz del XML debe ser <catalogo>.");
            }

            // 2. Con el documento ya abierto se pueden hacer varias consultas
            XPath xpath = XPathFactory.newInstance().newXPath();
            int opcion;
            do {
                System.out.println("\n1. Por código | 2. Por categoría | 3. Precio superior | 4. Contar | 0. Salir");
                opcion = LectorConsola.leerOpcion("Opción: ", 0, 4);
                try {
                    switch (opcion) {
                        case 1 : consultarPorCodigo(xpath, documento);
                        case 2 : consultarPorCategoria(xpath, documento);
                        case 3 : consultarPrecioSuperior(xpath, documento);
                        case 4 : contarProductos(xpath, documento);
                        case 0 : System.out.println("Fin de las consultas.");
                        default : throw new IllegalStateException("Opción inesperada");
                    }
                } catch (XPathExpressionException e) {
                    System.out.println(ColoresConsola.pintar("Error en la consulta XPath: " + e.getMessage(),
                            ColoresConsola.ROJO));
                }
            } while (opcion != 0);

        } catch (OperacionFicheroException e) {
            System.out.println(ColoresConsola.pintar(e.getMessage(), ColoresConsola.ROJO));
        }
    }

    // Consulta 1: el texto del usuario NO se mete en la expresión XPath; se comparan los nodos en Java.
    private static void consultarPorCodigo(XPath xpath, Document documento) throws XPathExpressionException {
        String buscado = LectorConsola.leerTexto("Código: ").trim();
        if (buscado.isEmpty()) {
            System.out.println("Debes escribir un código.");
            return;
        }
        NodeList nodos = (NodeList) xpath.evaluate("/catalogo/producto", documento, XPathConstants.NODESET);
        int encontrados = 0;
        for (int i = 0; i < nodos.getLength(); i++) {
            Element producto = (Element) nodos.item(i);
            if (buscado.equalsIgnoreCase(textoDe(producto, "codigo"))) {
                mostrarProducto(producto);
                encontrados++;
            }
        }
        System.out.println("Productos encontrados: " + encontrados);
    }

    // Consulta 2: el número ya está validado como entero, así que es seguro ponerlo en la expresión.
    private static void consultarPorCategoria(XPath xpath, Document documento) throws XPathExpressionException {
        int categoria = LectorConsola.leerEntero("Categoría: ");
        if (categoria <= 0) {
            System.out.println("La categoría debe ser un entero positivo.");
            return;
        }
        mostrarNodos(xpath, documento, "/catalogo/producto[categoriaId=" + categoria + "]");
    }

    // Consulta 3: "mayor que" estricto, así que un producto de precio 100 no sale con el mínimo 100.
    private static void consultarPrecioSuperior(XPath xpath, Document documento) throws XPathExpressionException {
        BigDecimal minimo = leerPrecio("Precio mínimo: ");
        mostrarNodos(xpath, documento, "/catalogo/producto[precio > " + minimo.toPlainString() + "]");
    }

    // Consulta 4: count() cuenta los <producto> aunque alguno tenga datos incorrectos.
    private static void contarProductos(XPath xpath, Document documento) throws XPathExpressionException {
        Double total = (Double) xpath.evaluate("count(/catalogo/producto)", documento, XPathConstants.NUMBER);
        System.out.println("Total de productos: " + total.intValue());
    }

    // ---------- Auxiliares ----------

    private static void mostrarNodos(XPath xpath, Document documento, String expresion)
            throws XPathExpressionException {
        NodeList nodos = (NodeList) xpath.evaluate(expresion, documento, XPathConstants.NODESET);
        for (int i = 0; i < nodos.getLength(); i++) {
            mostrarProducto((Element) nodos.item(i));
        }
        System.out.println("Productos encontrados: " + nodos.getLength());
    }

    private static void mostrarProducto(Element producto) {
        System.out.println(textoDe(producto, "codigo") + " " + textoDe(producto, "nombre") + " "
                + textoDe(producto, "precio") + " €");
    }

    // Texto de un hijo del producto; cadena vacía si no existe.
    private static String textoDe(Element producto, String etiqueta) {
        NodeList lista = producto.getElementsByTagName(etiqueta);
        if (lista.getLength() == 0) {
            return "";
        }
        return lista.item(0).getTextContent().trim();
    }

    // Pide un número decimal (>= 0) hasta que sea válido.
    private static BigDecimal leerPrecio(String mensaje) {
        while (true) {
            String texto = LectorConsola.leerTexto(mensaje).trim().replace(',', '.');
            try {
                BigDecimal valor = new BigDecimal(texto);
                if (valor.compareTo(BigDecimal.ZERO) >= 0) {
                    return valor;
                }
                System.out.println("El precio no puede ser negativo.");
            } catch (NumberFormatException e) {
                System.out.println("Introduce un número decimal válido.");
            }
        }
    }
}

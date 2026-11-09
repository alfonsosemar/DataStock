# DataStock – UT1 + UT2

**Alumno:** Alfonso Sevillano  
**Módulo:** Acceso a Datos · 2.º DAM · IES Isidra de Guzmán · Curso 2026–2027  
**Lenguaje:** Java 21 · **Construcción:** Maven · **Interfaz:** consola

## 1. ¿Qué es este proyecto?

DataStock es una aplicación de consola para gestionar un inventario de productos.

- **UT1 – Ficheros:** importar JSON/XML a una colección en memoria, consultar, exportar, serializar, XPath y `RandomAccessFile`.
- **UT2 – JDBC (esta entrega):** se añade una base de datos **MySQL** con categorías, proveedores, productos y movimientos de stock, usando JDBC, sentencias preparadas, un procedimiento almacenado y transacciones. Además, una demo independiente con **SQLite**.

Todo lo de la UT1 se conserva: el menú principal tiene una opción nueva (7) para MySQL.

## 2. Requisitos

- JDK **21** y Maven (o un IDE con Maven).
- **MySQL 8 o superior** en marcha (servidor local en el puerto 3306).
- Dependencias (Maven las descarga solo):
  - Gson 2.11.0 (JSON, UT1).
  - **MySQL Connector/J 9.1.0** (`com.mysql:mysql-connector-j`).
  - **SQLite JDBC 3.46.1.0** (`org.xerial:sqlite-jdbc`), solo para `DemoSQLite`.

## 3. Preparar la base de datos MySQL

El script que nos proporcionaron está en `material_referencia/DataStock_BD_MySQL.sql`. Crea la base de datos `datastock`, las cuatro tablas (`categoria`, `proveedor`, `producto`, `movimiento_stock`) y el procedimiento `registrar_movimiento`.

Se puede ejecutar desde MySQL Workbench (*File → Open SQL Script* y ejecutar) o desde terminal:

```bash
mysql -u root -p < material_referencia/DataStock_BD_MySQL.sql
```

## 4. Configurar la conexión (sin subir la contraseña)

Los datos de conexión se leen de un fichero local que **no se sube a GitHub** (está en `.gitignore`).

1. Copia la plantilla `config/datastock.properties.example` como `config/datastock.properties`.
2. Rellena tu contraseña:

```properties
jdbc.url=jdbc:mysql://localhost:3306/datastock?connectTimeout=5000
jdbc.usuario=root
jdbc.contrasena=TU_CONTRASEÑA
```

También se pueden usar las variables de entorno `JDBC_URL`, `JDBC_USER` y `JDBC_PASSWORD`. Si existen, tienen prioridad sobre el fichero (útil para apuntar a una base de datos de pruebas):

```bash
export JDBC_URL='jdbc:mysql://localhost:3306/datastock_pruebas?connectTimeout=5000'
```

La clase que lee esta configuración es `config/ConexionBD.java`.

## 5. Compilar y ejecutar

Desde la carpeta que contiene `pom.xml`:

```bash
mvn clean compile
mvn exec:java
```

O ejecutando `es.iesisidra.datastock.App` desde el IDE.

Actividades independientes:

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.XPathActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.AccesoAleatorioActividad
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

> Si la consola muestra códigos raros en vez de colores, ejecuta con `-Ddatastock.color=false` o define `NO_COLOR=1`.

## 6. Menús

**Menú principal**

```text
1. Explorar ficheros y directorios        (UT1)
2. Importar productos desde JSON          (UT1)
3. Importar productos desde XML           (UT1)
4. Consultar productos en memoria         (UT1)
5. Exportar productos                     (UT1)
6. Guardar y recuperar inventario         (UT1)
7. Gestionar base de datos MySQL          (UT2)
0. Salir
```

**7. Gestionar base de datos MySQL**

```text
1. Comprobar conexión y mostrar información de la BD   RF-JDBC-01
2. Gestionar categorías                                 RF-JDBC-02
3. Gestionar proveedores                                RF-JDBC-02
4. Gestionar productos (CRUD)                           RF-JDBC-03
5. Consultar y buscar productos                         RF-JDBC-04
6. Registrar entrada o salida de stock                  RF-JDBC-05
7. Consultar historial de movimientos                   RF-JDBC-06
8. Importar productos de memoria a MySQL                RF-JDBC-07
9. Demostración de transacción con commit/rollback      RF-JDBC-08
0. Volver al menú principal
```

- **Categorías / proveedores:** alta, listar, modificar y eliminar. No se puede borrar una categoría o proveedor que tenga productos.
- **Productos:** alta, consulta, modificación (nombre, precio, categoría y proveedor) y baja. El stock **no** se modifica aquí, solo con movimientos.
- **Consultas:** listar todos, por código, por nombre (contiene el texto) y stock inferior a un umbral.
- **Movimientos:** se llama al procedimiento `registrar_movimiento` con `CallableStatement`. Es el procedimiento el que actualiza el stock y guarda el movimiento; si una salida deja el stock negativo, MySQL la rechaza.
- **Importar de memoria (8):** copia a MySQL los productos cargados con las opciones 2 y 3. Los que ya existen se omiten y los que tienen una categoría o proveedor inexistente se rechazan. Los válidos se insertan en **una sola transacción**: o entran todos o ninguno. Para probarlo se puede usar `datos/ejemplos/catalogo_ut2.json`.
- **Demostración de transacción (9):** usa las categorías de prueba 9001, 9002 y 9003.
  - Escenario A: inserta 9001 y 9002 y hace `commit()`.
  - Escenario B: inserta 9003 dos veces, falla por clave duplicada y hace `rollback()`, así que la 9003 no queda guardada.
  - Al final pregunta si borrarlas; si no, se borran solas al repetir la demo.

## 7. Memoria (UT1) frente a MySQL (UT2)

| | Inventario en memoria (UT1) | MySQL (UT2) |
|---|---|---|
| Dónde están los datos | En una lista Java mientras el programa se ejecuta | En la base de datos `datastock`, en el servidor MySQL |
| Al cerrar el programa | Se pierden (salvo que se guarde un `.dat`) | Se conservan |
| Categorías y proveedores | Solo se comprueba que el id sea un número positivo | Deben existir en sus tablas (claves ajenas) |
| Stock | Se escribe directamente | Solo cambia con movimientos (procedimiento almacenado) |
| Relación entre ambos | — | No hay sincronización automática: solo la opción 8 copia de memoria a MySQL |

## 8. Estructura del proyecto

```text
DataStock_UT1_Alfonso_Sevillano/
├── pom.xml
├── README.md
├── config/
│   ├── datastock.properties.example   # Plantilla (sí se sube)
│   └── datastock.properties           # Tus credenciales (NO se sube)
├── datos/
│   ├── ejemplos/                      # JSON/XML de prueba (incluye catalogo_ut2.json)
│   ├── salidas/                       # Exportaciones (UT1)
│   └── sqlite/                        # productos_demo.db (lo crea DemoSQLite, no se sube)
├── material_referencia/               # Script SQL y diagrama E-R
└── src/main/java/es/iesisidra/datastock/
    ├── App.java
    ├── config/        ConexionBD (lee la configuración y abre conexiones)
    ├── bd/            Clases JDBC: CategoriaBD, ProveedorBD, ProductoBD, MovimientoBD,
    │                  ImportacionBD, DemoTransaccionBD, EsquemaBD, ErroresBD
    ├── modelo/        Producto, Categoria, Proveedor, MovimientoStock, FichaProducto
    ├── servicio/      Inventario en memoria y validación de productos
    ├── ficheros/      Lectura/escritura de JSON, XML, .dat y binario (UT1)
    ├── ui/            Menús de consola (solo piden datos y muestran resultados)
    ├── util/          Lectura de consola, colores y mensajes [OK]/[ERROR]/[AVISO]
    └── complementarias/ XPathActividad, AccesoAleatorioActividad, DemoSQLite
```

Los menús (`ui`) no contienen SQL: llaman a las clases del paquete `bd`, que son las que usan JDBC.

## 9. Decisiones técnicas

- Todas las consultas con datos del usuario usan `PreparedStatement` (evita inyección SQL).
- `Connection`, `PreparedStatement` y `ResultSet` se cierran con **try-with-resources**.
- Cada operación abre una conexión corta y la cierra al terminar.
- Los errores de MySQL se traducen a mensajes claros en `ErroresBD` (clave duplicada, clave ajena, contraseña incorrecta, base de datos inexistente…). Nunca se muestra la contraseña.
- Antes de modificar o borrar se pide confirmación.
- Transacciones (opciones 8 y 9): `setAutoCommit(false)`, operaciones, `commit()`; si hay error, `rollback()`; y en `finally` se vuelve a poner `setAutoCommit(true)`.

## 10. Actividad SQLite (DemoSQLite)

Clase independiente `es.iesisidra.datastock.complementarias.DemoSQLite`. No está en ningún menú; se ejecuta con:

```bash
mvn exec:java -Dexec.mainClass=es.iesisidra.datastock.complementarias.DemoSQLite
```

Qué hace:

1. Crea la carpeta `datos/sqlite` si no existe.
2. Abre `jdbc:sqlite:datos/sqlite/productos_demo.db` con `DriverManager.getConnection(url)`, sin usuario ni contraseña. Si el fichero no existe, SQLite lo crea.
3. Crea la tabla `producto_demo` con `CREATE TABLE IF NOT EXISTS`.
4. Inserta SQL-001 y SQL-002 con `INSERT OR IGNORE`, de modo que al repetir la ejecución no se duplican. Muestra cuántas filas ha insertado y cuántas ya existían.
5. Hace `SELECT codigo, nombre FROM producto_demo ORDER BY codigo` y las muestra.

Salida en la primera ejecución:

```text
[OK] Conexión SQLite abierta: datos/sqlite/productos_demo.db
[OK] Tabla producto_demo disponible.
Insertados: 2 | Ya existentes: 0
SQL-001 | Producto demo A
SQL-002 | Producto demo B
```

En la segunda ejecución sale `Insertados: 0 | Ya existentes: 2`.

> Al ejecutarla puede aparecer un aviso `SLF4J: Failed to load class...`. Lo escribe la librería de SQLite porque no hay sistema de logs configurado; no es un error.

## 11. Comparativa MySQL y SQLite

| Pregunta | MySQL | SQLite |
|---|---|---|
| ¿Necesita servidor? | Sí, es un gestor **independiente**: un servidor que tiene que estar arrancado | No, es un gestor **embebido**: funciona dentro de la propia aplicación Java |
| ¿Dónde se guardan los datos? | En los ficheros que gestiona el servidor MySQL | En un único fichero `.db` local (`datos/sqlite/productos_demo.db`) |
| ¿Cómo se conecta por JDBC? | `jdbc:mysql://localhost:3306/datastock` con usuario y contraseña | `jdbc:sqlite:ruta/al/fichero.db`, sin usuario ni contraseña |
| Dependencia Maven | `com.mysql:mysql-connector-j` 9.1.0 | `org.xerial:sqlite-jdbc` 3.46.1.0 |
| Ventajas | Varios usuarios a la vez, usuarios y permisos, procedimientos almacenados, red | No hay que instalar nada, muy ligero, la BD es un fichero fácil de copiar |
| Inconvenientes | Hay que instalarlo, configurarlo y tenerlo arrancado | Pensado para un solo usuario o pocos; menos tipos y funciones; no tiene procedimientos almacenados |
| ¿Para qué lo elegiría? | Aplicaciones con varios usuarios o en red, como DataStock | Aplicaciones de escritorio o móviles, pruebas o configuraciones locales |

**Pool de conexiones.** Abrir una conexión con la base de datos tarda un poco. Un *pool* abre varias conexiones al principio y las va prestando: cuando el programa termina con una, no se cierra de verdad, sino que vuelve al pool para reutilizarla. Es útil en aplicaciones con muchos usuarios a la vez, como un servidor web con cientos de peticiones.

DataStock no lo necesita: es una aplicación de consola con un solo usuario que hace pocas operaciones. Por eso abre una conexión corta en cada operación y la cierra enseguida con try-with-resources. Así no quedan conexiones abiertas ni transacciones a medias.

## 12. Pruebas

Las pruebas se han hecho en una base de datos aparte, `datastock_pruebas`, apuntando a ella con la variable `JDBC_URL` para no ensuciar `datastock`. El registro de pruebas (CP-JDBC-01 a CP-JDBC-20 y CP-SQLITE-01 a CP-SQLITE-03) se guarda en PDF en la carpeta `Documento` y se entrega también en Moodle.

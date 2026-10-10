# Sistema de Gestión de Biblioteca Escolar

Aplicación de escritorio desarrollada en Java para gestionar libros, estudiantes, préstamos, devoluciones y reportes de una biblioteca escolar.

## Autor

- **Nombre:** Nicolás Sánchez Bustos
- **Carrera:** Analista Programador Computacional
- **Evaluación:** Evaluación Final Transversal

## Tecnologías

- Java JDK 25.
- Java Swing: JFrame, JPanel, JTable y SwingWorker.
- MySQL y JDBC.
- MySQL Connector/J.
- IntelliJ IDEA.

## Funcionalidades

### Autenticación

Ingreso con correo y contraseña, con permisos según el rol:

- **Bibliotecario:** gestión de libros y estudiantes, préstamos, devoluciones y reportes.
- **Estudiante:** consulta del catálogo, préstamos y devoluciones propios y consulta de su historial.

### Gestión de libros

Registro, listado, modificación y eliminación de libros, incluyendo título, autor, ISBN, editorial, categoría y stock disponible.

### Gestión de estudiantes

Registro, modificación y eliminación de estudiantes y de sus usuarios asociados. Ambas operaciones se realizan dentro de una transacción.

Los registros vinculados a préstamos se conservan mediante las restricciones de integridad de la base de datos.

### Préstamos y devoluciones

- Verificación de stock antes de registrar un préstamo.
- Vencimiento calculado a siete días.
- Descuento de stock al prestar.
- Incremento de stock al devolver.
- Registro de la fecha real de devolución.
- Detección de atrasos.
- Prevención de devoluciones repetidas.

### Reportes

- Libros más prestados.
- Historial por estudiante.
- Préstamos activos.
- Estado del préstamo y días de atraso.

## Organización del código

| Paquete | Responsabilidad |
|---------|-----------------|
| `app` | Inicio de la aplicación y pruebas manuales |
| `config` | Configuración y conexión a MySQL |
| `model` | Entidades del sistema |
| `dao` | Consultas y operaciones JDBC |
| `service` | Lógica de negocio y transacciones |
| `controller` | Coordinación entre vistas y servicios, con control de permisos |
| `view` | Interfaz gráfica Swing |

## Patrones y concurrencia

- **MVC:** separación de modelos, vistas y controladores.
- **DAO:** acceso a la base de datos separado de la interfaz.
- **Singleton:** `DatabaseConnection.getInstance()` proporciona una única instancia para administrar la conexión.
- **SwingWorker:** ejecuta operaciones de base de datos en segundo plano y actualiza la interfaz en el hilo de eventos.
- **Sincronización:** las operaciones comparten el bloqueo de `DatabaseConnection` mediante `synchronized`.
- **Transacciones:** `commit()` confirma las operaciones y `rollback()` revierte los cambios si ocurre un error.
- **Control de stock:** la consulta de descuento exige `stock > 0`.

## Requisitos

- JDK 25 instalado.
- MySQL instalado y en ejecución.
- Base de datos configurada con los scripts incluidos.
- Credenciales de MySQL definidas mediante variables de entorno.

El JAR distribuido incluye MySQL Connector/J. MySQL debe estar disponible por separado.

## Configuración de la base de datos

En una instalación nueva, ejecutar los archivos de la carpeta `sql` en este orden:

1. `PRY2203_EFT_S9_Script_crea_tablas_biblioteca.sql`
2. `PRY2203_EFT_S9_Script_poblado_tablas_biblioteca.sql`
3. `Script_actualizacion_biblioteca.sql`

El tercer script agrega `fecha_devolucion_real` a la tabla `prestamos`. Ejecutarlo una sola vez, después de crear las tablas.

`fecha_devolucion` representa el vencimiento; `fecha_devolucion_real` registra cuándo se devolvió el libro.

Los préstamos devueltos del script original no incluyen una fecha real de devolución. Para ellos, el reporte puede mostrar esa fecha y los días de atraso sin información.

## Variables de entorno

| Variable | Descripción |
|----------|-------------|
| `BIBLIOTECA_DB_URL` | URL JDBC, por ejemplo `jdbc:mysql://localhost:3306/biblioteca` |
| `BIBLIOTECA_DB_USER` | Usuario de MySQL |
| `BIBLIOTECA_DB_PASSWORD` | Contraseña de MySQL |

La contraseña de MySQL es distinta de la contraseña de los usuarios de la aplicación.

## Ejecución desde IntelliJ IDEA

1. Abrir el proyecto.
2. Seleccionar JDK 25.
3. Agregar MySQL Connector/J como dependencia si no está configurado.
4. Crear una configuración de ejecución para `app.Main`.
5. Definir las tres variables de entorno en esa configuración.
6. Ejecutar `app.Main`.

## Ejecución del JAR en Windows

Abrir PowerShell en la carpeta raíz del proyecto.

Ejecutar las siguientes instrucciones una por una:

```powershell
$env:BIBLIOTECA_DB_URL = "jdbc:mysql://localhost:3306/biblioteca"
$env:BIBLIOTECA_DB_USER = "root"
$clave = Read-Host "Contraseña de MySQL" -AsSecureString
```

Ingresar la contraseña de MySQL y pulsar Enter. Después ejecutar:

```powershell
$env:BIBLIOTECA_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $clave).Password
java -jar ".\dist\BibliotecaEscolar.jar"
```

Si `java` no está disponible en PATH, reemplazarlo por la ruta del ejecutable instalado. Ejemplo:

```powershell
& "C:\Program Files\Java\jdk-25.0.2\bin\java.exe" -jar ".\dist\BibliotecaEscolar.jar"
```

Las variables configuradas en IntelliJ no se transfieren automáticamente a PowerShell. Deben configurarse en la misma sesión desde la que se ejecuta el JAR.

## Cuenta de ejemplo

Cuenta de bibliotecario incluida en el script de poblamiento:

- **Correo:** `antonia@correo.cl`
- **Contraseña:** `clave123`

Se pueden registrar nuevas cuentas de estudiante desde la pestaña Estudiantes.

## Pruebas realizadas

- Conexión a MySQL.
- Autenticación válida e inválida.
- Permisos según el rol.
- Registro, modificación y eliminación de libros.
- Registro, modificación y eliminación de estudiantes.
- Registro conjunto de estudiante y usuario.
- Rollback ante un usuario duplicado.
- Préstamos y devoluciones con actualización de stock.
- Rechazo de préstamos sin stock.
- Bloqueo de devoluciones repetidas.
- Consulta de los tres reportes.
- Dos hilos compitiendo por un libro con stock 1: un préstamo registrado, otro rechazado y stock final 0.
- Ejecución y conexión a MySQL desde el JAR fuera de IntelliJ.

## Prueba manual de concurrencia

La clase `app.PruebaConcurrencia` utiliza dos hilos y un `CountDownLatch` para liberar ambas tareas con una misma señal de inicio.

Antes de ejecutarla:

1. Revisar los IDs de libro y estudiante definidos en la clase.
2. Usar un libro de prueba con stock 1.
3. Configurar las variables de entorno en su configuración de ejecución.

La prueba modifica la base de datos. El resultado esperado es un solo préstamo registrado y stock final 0. Después se puede devolver el préstamo desde la interfaz.

## Archivos de entrega

- Código fuente en `src`.
- Scripts SQL en `sql`.
- JAR ejecutable en `dist/BibliotecaEscolar.jar`.
- Instrucciones en `README.md`.
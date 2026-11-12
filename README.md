# movilidad-urbana-POO

Proyecto Maven (Java 17+, JUnit 5).

## Estructura

```
pom.xml
config.properties, usuarios.txt, vehiculos.txt, servicios.txt   <- configuración y datos de entrada
src/main/java/md/
  aplicacion/       puntos de entrada
  config/           carga de parámetros y selección de DAO
  datos/            DAO, implementaciones TXT/BD y DTO
  logica/           reglas de negocio y coordinación de viajes
  modelo/           entidades y tipos de dominio
  rutas/api/        contratos de planificación de rutas (estructura preparada)
  rutas/impl/       implementaciones de planificación de rutas (estructura preparada)
  simulacion/       simulación de ubicaciones y escenarios (estructura preparada)
  ui/               interfaz de consola y JavaFX
    api/             contratos de interfaz y visualización
    impl/javafx/     interfaz JavaFX, controladores y adaptadores de mapa
src/main/java/net/datastructures/  estructuras de datos
src/test/java/md/test/             pruebas JUnit 5
```

Los paquetes de `rutas`, `simulacion` y `ui/impl/javafx/map` están creados como estructura;
sus clases todavía no están implementadas.

> **Importante:** el código abre `config.properties` y los `.txt` con rutas relativas
> (`new FileInputStream("config.properties")`), por lo que hay que ejecutar **siempre desde
> la raíz del proyecto** (la carpeta donde está el `pom.xml`).

## Comandos

```bash
mvn clean compile            # compilar
mvn test                     # correr los tests
mvn clean package            # compilar + tests + generar target/movilidad-urbana-poo-1.0-SNAPSHOT.jar
mvn compile exec:java        # ejecutar el menú de consola (md.aplicacion.AplicacionConsulta)
java -jar target/movilidad-urbana-poo-1.0-SNAPSHOT.jar   # idem, con el jar generado
```

Para ejecutar un comprobador manual con `main`:

```bash
mvn test-compile exec:java -Dmain.class=md.test.PruebaDAO -Dexec.classpathScope=test
```

## Importar en el IDE

- **Eclipse:** File → Import → Maven → *Existing Maven Projects* (m2e genera `.classpath`/`.project`).
- **VS Code / IntelliJ:** abrir la carpeta; detectan el `pom.xml` solos.

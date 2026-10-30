# movilidad-urbana-POO

Proyecto Maven (Java 17+, JUnit 5).

## Estructura

```
pom.xml
config.properties, usuarios.txt, vehiculos.txt, servicios.txt   <- datos (se leen desde la raíz)
src/main/java/   datos, logica, modelo, pruebas, md.*, net.datastructures
src/test/java/   md.test (tests JUnit 5)
```

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

Para ejecutar otra clase con `main` (por ejemplo `md.pruebas.PruebaDAO`):

```bash
mvn compile exec:java -Dexec.mainClass=md.pruebas.PruebaDAO
```

## Importar en el IDE

- **Eclipse:** File → Import → Maven → *Existing Maven Projects* (m2e genera `.classpath`/`.project`).
- **VS Code / IntelliJ:** abrir la carpeta; detectan el `pom.xml` solos.

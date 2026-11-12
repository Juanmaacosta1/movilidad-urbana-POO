package md.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Properties;

import md.datos.dao.DAOFactory;
import md.datos.dao.bd.BdDAOFactory;
import md.datos.dao.bd.ConexionBD;
import md.datos.dao.txt.TxtDAOFactory;

public class Loader {

    private static DAOFactory instanciaFabrica;

    public static synchronized DAOFactory getFactory() throws IOException {
        if (instanciaFabrica == null) {
            Properties config = new Properties();
            Path ruta = Path.of("config.properties");
            if (Files.exists(ruta)) {
                try (InputStream in = Files.newInputStream(ruta)) {
                    config.load(in);
                }
            }
            String origen = config.getProperty("origen.datos", "BD").trim();
            if ("TXT".equalsIgnoreCase(origen)) {
                instanciaFabrica = crearFactoryTxt();
            } else if ("BD".equalsIgnoreCase(origen)) {
                instanciaFabrica = crearFactoryDesdeJdbc();
            } else {
                throw new IllegalStateException("origen.datos debe ser BD o TXT: " + origen);
            }
        }
        return instanciaFabrica;
    }

    private static DAOFactory crearFactoryDesdeJdbc() throws IOException {
        Path ruta = Path.of("jdbc.properties");
        if (!Files.exists(ruta)) {
            throw new IllegalStateException("No existe jdbc.properties para usar la base de datos.");
        }

        Properties prop = new Properties();
        try (InputStream input = Files.newInputStream(ruta)) {
            prop.load(input);
        }

        String driver = prop.getProperty("driver");
        String url = prop.getProperty("url");
        String usuario = prop.getProperty("usr");
        String password = prop.getProperty("pwd");
        String schema = prop.getProperty("schema", "movilidad_digital").trim();

        if (driver == null || driver.isBlank() || url == null || url.isBlank()
                || usuario == null || usuario.isBlank() || password == null) {
            throw new IllegalStateException("jdbc.properties incompleto para conectar a PostgreSQL.");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC no encontrado: " + driver, e);
        }

        ConexionBD conexion = new ConexionBD(url, usuario, password, schema);
        try (Connection c = conexion.obtenerConexion()) {
            try (java.sql.Statement st = c.createStatement();
                 java.sql.ResultSet rs = st.executeQuery("SELECT current_schema()")) {
                if (!rs.next() || !schema.equals(rs.getString(1))) {
                    throw new IllegalStateException("Schema incorrecto. Esperado: " + schema);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("No se pudo conectar a la base de datos configurada en jdbc.properties.", e);
        }

        return new BdDAOFactory(conexion);
    }

    private static DAOFactory crearFactoryTxt() throws IOException {
        CargaParametros.parametros();
        return new TxtDAOFactory(
                CargaParametros.getArchivoServicios(),
                CargaParametros.getArchivoVehiculos(),
                CargaParametros.getArchivoUsuarios());
    }
}
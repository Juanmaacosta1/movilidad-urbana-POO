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

    public static DAOFactory getFactory() throws IOException {
        if (instanciaFabrica == null) {
            try {
                instanciaFabrica = crearFactoryTxt();
            } catch (IllegalStateException e) {
                instanciaFabrica = crearFactoryDesdeJdbc();
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

        if (driver == null || driver.isBlank() || url == null || url.isBlank()
                || usuario == null || usuario.isBlank() || password == null) {
            throw new IllegalStateException("jdbc.properties incompleto para conectar a PostgreSQL.");
        }

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Driver JDBC no encontrado: " + driver, e);
        }

        ConexionBD conexion = new ConexionBD(url, usuario, password);
        try (Connection c = conexion.obtenerConexion()) {
            // Validar que la cadena de conexión sea real antes de elegir BD.
        } catch (Exception e) {
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
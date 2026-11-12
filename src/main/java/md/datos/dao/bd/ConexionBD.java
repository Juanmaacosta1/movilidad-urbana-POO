package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private final String url;
    private final String usuario;
    private final String password;
    private final String schema;

    public ConexionBD(String url, String usuario, String password, String schema) {
        this.url = url;
        this.usuario = usuario;
        this.password = password;
        this.schema = schema;
    }

    public Connection obtenerConexion() throws SQLException {
        Connection c = DriverManager.getConnection(url, usuario, password);
        try {
            c.setSchema(schema);
            return c;
        } catch (SQLException e) {
            c.close();
            throw e;
        }
    }
}

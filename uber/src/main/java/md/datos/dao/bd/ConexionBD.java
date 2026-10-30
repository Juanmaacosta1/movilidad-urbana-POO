package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

	private final String url;
	private final String usuario;
	private final String password;

	public ConexionBD(String url, String usuario, String password) {
		super();
		this.url = url;
		this.usuario = usuario;
		this.password = password;
	}

	public Connection obtenerConexion() throws SQLException {
		return DriverManager.getConnection(url, usuario, password);
	}

}

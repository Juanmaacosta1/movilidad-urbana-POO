package md.datos.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import md.datos.dao.txt.TxtDAOFactory;

public class DAOLoader {

	private static final String ARCHIVO_CONFIG = "config.properties";
	private static DAOFactory instanciaFabrica;

	private DAOLoader() {
	}

	public static DAOFactory getFactory() throws IOException {
		if (instanciaFabrica == null) {
			Properties prop = new Properties();
			try (InputStream input = new FileInputStream(ARCHIVO_CONFIG)) {
				prop.load(input);
			}
			instanciaFabrica = crearFactory(prop);
		}
		return instanciaFabrica;
	}

	public static DAOFactory crearFactory(Properties prop) {
		String origen = prop.getProperty("origen.datos", "TXT").trim().toUpperCase();
		if ("TXT".equals(origen)) {
			return new TxtDAOFactory(requerida(prop, "servicio"), requerida(prop, "vehiculo"),
					requerida(prop, "usuario"));
		}
		throw new IllegalArgumentException("origen.datos desconocido: " + origen + " (valores válidos: TXT)");
	}

	private static String requerida(Properties prop, String clave) {
		String valor = prop.getProperty(clave);
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("Falta '" + clave + "' en " + ARCHIVO_CONFIG);
		}
		return valor.trim();
	}
}
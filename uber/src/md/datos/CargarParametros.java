package datos;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CargarParametros {

	private static String archivoUsuarios;
	private static String archivoServicios;
	private static String archivoVehiculos;

	private static double latitud1;
	private static double longitud1;
	private static double latitud2;
	private static double longitud2;
	
	public static void parametros() throws IOException {
		Properties prop = new Properties();
		try(InputStream input = new FileInputStream("config.properties")) {
			
			prop.load(input);
			archivoUsuarios = prop.getProperty("usuario");
			archivoServicios = prop.getProperty("servicio");
			archivoVehiculos = prop.getProperty("vehiculo");
 
			latitud1 = Double.parseDouble(prop.getProperty("latitud1"));
			longitud1 = Double.parseDouble(prop.getProperty("longitud1"));
			latitud2 = Double.parseDouble(prop.getProperty("latitud2"));
			longitud2 = Double.parseDouble(prop.getProperty("longitud2"));
			
		}
	}
	
	public static String getArchivoUsuarios() {
		return archivoUsuarios;
	}
 
	public static String getArchivoServicios() {
		return archivoServicios;
	}
 
	public static String getArchivoVehiculos() {
		return archivoVehiculos;
	}
 
	public static double getLatitud1() {
		return latitud1;
	}
 
	public static double getLongitud1() {
		return longitud1;
	}
 
	public static double getLatitud2() {
		return latitud2;
	}
 
	public static double getLongitud2() {
		return longitud2;
	}
}
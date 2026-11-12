package md.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class CargaParametros {

	private static String archivoUsuarios;
	private static String archivoServicios;
	private static String archivoVehiculos;

	private static String rutasMotor;
	private static String rutasOsm;
	private static String rutasCache;

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
 
			rutasMotor = prop.getProperty("rutas.motor", "graphhopper").trim();
			rutasOsm = prop.getProperty("rutas.osm", "mapas/puerto_madryn_ampliado.osm").trim();
			rutasCache = prop.getProperty("rutas.cache", "mapas/graphhopper-cache-madryn-ampliado").trim();

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
 
	public static String getRutasMotor() {
		return rutasMotor;
	}

	public static String getRutasOsm() {
		return rutasOsm;
	}

	public static String getRutasCache() {
		return rutasCache;
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
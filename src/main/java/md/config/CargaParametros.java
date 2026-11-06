package md.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Lee config.properties. Se busca primero como recurso del classpath
 * (src/main/resources/config.properties) y, si no está, como archivo en el
 * directorio de ejecución. Los getters cargan la configuración la primera vez
 * que se usan: ya no devuelven 0.0 / null si nadie llamó antes a parametros().
 */
public class CargaParametros {

	private static final String ARCHIVO = "config.properties";

	private static Properties config;

	private static String archivoUsuarios;
	private static String archivoServicios;
	private static String archivoVehiculos;

	private static double latitud1;
	private static double longitud1;
	private static double latitud2;
	private static double longitud2;

	private CargaParametros() {
	}

	/** Carga (o recarga) la configuración. Se mantiene por compatibilidad. */
	public static synchronized void parametros() throws IOException {
		Properties prop = leerArchivo();

		archivoUsuarios = prop.getProperty("usuario");
		archivoServicios = prop.getProperty("servicio");
		archivoVehiculos = prop.getProperty("vehiculo");

		latitud1 = Double.parseDouble(requerida(prop, "latitud1"));
		longitud1 = Double.parseDouble(requerida(prop, "longitud1"));
		latitud2 = Double.parseDouble(requerida(prop, "latitud2"));
		longitud2 = Double.parseDouble(requerida(prop, "longitud2"));

		config = prop;
	}

	private static Properties leerArchivo() throws IOException {
		try (InputStream in = CargaParametros.class.getResourceAsStream("/" + ARCHIVO)) {
			if (in != null) {
				return cargar(in);
			}
		}
		Path local = Path.of(ARCHIVO);
		if (Files.exists(local)) {
			try (InputStream in = Files.newInputStream(local)) {
				return cargar(in);
			}
		}
		throw new IOException("No se encontró " + ARCHIVO + " en el classpath ni en el directorio de ejecución");
	}

	private static Properties cargar(InputStream in) throws IOException {
		Properties prop = new Properties();
		try (Reader lector = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			prop.load(lector);
		}
		return prop;
	}

	private static String requerida(Properties prop, String clave) {
		String valor = prop.getProperty(clave);
		if (valor == null || valor.isBlank()) {
			throw new IllegalStateException("Falta la propiedad '" + clave + "' en " + ARCHIVO);
		}
		return valor.trim();
	}

	private static synchronized Properties asegurar() {
		if (config == null) {
			try {
				parametros();
			} catch (IOException e) {
				throw new IllegalStateException("No se pudo leer la configuración: " + e.getMessage(), e);
			}
		}
		return config;
	}

	private static double decimal(String clave, double porDefecto) {
		String valor = asegurar().getProperty(clave);
		return valor == null || valor.isBlank() ? porDefecto : Double.parseDouble(valor.trim());
	}

	// ---- Interfaz de usuario ----

	/** Valor normalizado de app.ui: "javafx" (por defecto) o "cli". */
	public static String ui() {
		return asegurar().getProperty("app.ui", "javafx").trim().toLowerCase(Locale.ROOT);
	}

	/** Valor normalizado de origen.datos: "TXT" por defecto. */
	public static String origenDatos() {
		return asegurar().getProperty("origen.datos", "TXT").trim().toUpperCase(Locale.ROOT);
	}

	// ---- Archivos de datos ----

	public static String getArchivoUsuarios() {
		asegurar();
		return archivoUsuarios;
	}

	public static String getArchivoServicios() {
		asegurar();
		return archivoServicios;
	}

	public static String getArchivoVehiculos() {
		asegurar();
		return archivoVehiculos;
	}

	// ---- Área de simulación ----

	public static double getLatitud1() {
		asegurar();
		return latitud1;
	}

	public static double getLongitud1() {
		asegurar();
		return longitud1;
	}

	public static double getLatitud2() {
		asegurar();
		return latitud2;
	}

	public static double getLongitud2() {
		asegurar();
		return longitud2;
	}

	// ---- Mapa ----

	public static String getCiudadNombre() {
		return asegurar().getProperty("ciudad.nombre", "Puerto Madryn").trim();
	}

	public static double getMapaCentroLatitud() {
		return decimal("mapa.centro.latitud", (latitud1 + latitud2) / 2);
	}

	public static double getMapaCentroLongitud() {
		return decimal("mapa.centro.longitud", (longitud1 + longitud2) / 2);
	}

	public static int getMapaZoom() {
		return (int) decimal("mapa.zoom", 13);
	}
}

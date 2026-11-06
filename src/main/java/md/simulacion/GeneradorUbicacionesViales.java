package md.simulacion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import md.config.CargaParametros;
import md.modelo.Ubicacion;

public class GeneradorUbicacionesViales {

	private final Random random;

	public GeneradorUbicacionesViales() {
		this(new Random());
	}

	public GeneradorUbicacionesViales(long seed) {
		this.random = new Random(seed);
	}

	public GeneradorUbicacionesViales(Random random) {
		if (random == null) {
			throw new IllegalArgumentException("El generador aleatorio no puede ser nulo");
		}
		this.random = random;
	}

	public Ubicacion generar() {
		double latMin = Math.min(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		double latMax = Math.max(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		double lonMin = Math.min(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());
		double lonMax = Math.max(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());
		return generarEnArea(latMin, latMax, lonMin, lonMax);
	}

	public Ubicacion generarEnArea(double latMin, double latMax, double lonMin, double lonMax) {
		if (latMin > latMax) {
			double aux = latMin;
			latMin = latMax;
			latMax = aux;
		}
		if (lonMin > lonMax) {
			double aux = lonMin;
			lonMin = lonMax;
			lonMax = aux;
		}
		double latitud = latMin + (latMax - latMin) * random.nextDouble();
		double longitud = lonMin + (lonMax - lonMin) * random.nextDouble();
		return new Ubicacion(latitud, longitud);
	}

	public List<Ubicacion> generarVarias(int cantidad) {
		if (cantidad < 0) {
			throw new IllegalArgumentException("La cantidad no puede ser negativa");
		}
		List<Ubicacion> ubicaciones = new ArrayList<>();
		for (int i = 0; i < cantidad; i++) {
			ubicaciones.add(generar());
		}
		return ubicaciones;
	}

	public static Ubicacion generarUbicacionAleatoria() {
		return new GeneradorUbicacionesViales().generar();
	}

	public static List<Ubicacion> generarUbicacionesAleatorias(int cantidad) {
		return new GeneradorUbicacionesViales().generarVarias(cantidad);
	}
}
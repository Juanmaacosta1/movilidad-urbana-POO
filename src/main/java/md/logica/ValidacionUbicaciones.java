package md.logica;

import md.config.CargaParametros;
import md.modelo.Ubicacion;

/** Reglas para aceptar coordenadas: compartidas por la CLI y JavaFX. */
public final class ValidacionUbicaciones {

	private ValidacionUbicaciones() {
	}

	/** @throws IllegalArgumentException si la ubicación es nula o cae fuera del área de simulación */
	public static void validarEnArea(Ubicacion ubicacion, String nombre) {
		if (ubicacion == null) {
			throw new IllegalArgumentException(nombre + " es obligatorio");
		}
		double latMin = Math.min(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		double latMax = Math.max(CargaParametros.getLatitud1(), CargaParametros.getLatitud2());
		double lonMin = Math.min(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());
		double lonMax = Math.max(CargaParametros.getLongitud1(), CargaParametros.getLongitud2());

		if (ubicacion.getLatitud() < latMin || ubicacion.getLatitud() > latMax
				|| ubicacion.getLongitud() < lonMin || ubicacion.getLongitud() > lonMax) {
			throw new IllegalArgumentException(String.format(java.util.Locale.ROOT,
					"%s está fuera del área de simulación (lat %.5f a %.5f, lng %.5f a %.5f)",
					nombre, latMin, latMax, lonMin, lonMax));
		}
	}
}

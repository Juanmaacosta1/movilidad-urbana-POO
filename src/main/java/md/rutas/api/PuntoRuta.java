package md.rutas.api;

/** Punto geográfico de la geometría de una ruta. Se conserva el orden del recorrido. */
public record PuntoRuta(double latitud, double longitud) {

	public PuntoRuta {
		if (!Double.isFinite(latitud) || latitud < -90 || latitud > 90) {
			throw new IllegalArgumentException("Latitud fuera de rango: " + latitud);
		}
		if (!Double.isFinite(longitud) || longitud < -180 || longitud > 180) {
			throw new IllegalArgumentException("Longitud fuera de rango: " + longitud);
		}
	}
}

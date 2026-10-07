package modelo;

public class Ubicacion {
	private static final double RADIO_TIERRA_KM = 6371.0;

	private final double latitud;
	private final double longitud;

	public Ubicacion(double latitud, double longitud) {
		if (latitud < -90 || latitud > 90) {
			throw new IllegalArgumentException("Latitud fuera de rango: " + latitud);
		}
		if (longitud < -180 || longitud > 180) {
			throw new IllegalArgumentException("Longitud fuera de rango: " + longitud);
		}
		this.latitud = latitud;
		this.longitud = longitud;
	}

	public double getLatitud() {
		return latitud;
	}

	public double getLongitud() {
		return longitud;
	}

	/**
	 * Distancia en kilómetros entre esta ubicación y otra (fórmula de Haversine).
	 */
	public double calcularDistancia(Ubicacion ubicacion) {
		if (ubicacion == null) {
			throw new IllegalArgumentException("La ubicación no puede ser nula");
		}
		double dLat = Math.toRadians(ubicacion.latitud - latitud);
		double dLon = Math.toRadians(ubicacion.longitud - longitud);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(latitud))
				* Math.cos(Math.toRadians(ubicacion.latitud)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
		return RADIO_TIERRA_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	@Override
	public String toString() {
		return "Ubicacion [latitud=" + latitud + ", longitud=" + longitud + "]";
	}
}
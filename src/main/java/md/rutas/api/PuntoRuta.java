package md.rutas.api;

import md.modelo.Ubicacion;

public class PuntoRuta {

	private final Ubicacion ubicacion;
	private final int orden;
	private final double distanciaDesdeInicio;

	public PuntoRuta(Ubicacion ubicacion, int orden, double distanciaDesdeInicio) {
		if (ubicacion == null) {
			throw new IllegalArgumentException("La ubicación del punto de ruta no puede ser nula");
		}
		if (orden < 0) {
			throw new IllegalArgumentException("El orden no puede ser negativo");
		}
		if (distanciaDesdeInicio < 0) {
			throw new IllegalArgumentException("La distancia acumulada no puede ser negativa");
		}
		this.ubicacion = ubicacion;
		this.orden = orden;
		this.distanciaDesdeInicio = distanciaDesdeInicio;
	}

	public Ubicacion getUbicacion() {
		return ubicacion;
	}

	public int getOrden() {
		return orden;
	}

	public double getDistanciaDesdeInicio() {
		return distanciaDesdeInicio;
	}
}
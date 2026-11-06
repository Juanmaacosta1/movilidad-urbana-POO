package md.rutas.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import md.modelo.Ubicacion;

public class RutaCalculada {

	private final Ubicacion origen;
	private final Ubicacion destino;
	private final List<PuntoRuta> puntos;
	private final double distanciaKm;
	private final double duracionMinutos;

	public RutaCalculada(Ubicacion origen, Ubicacion destino, List<PuntoRuta> puntos, double distanciaKm,
			double duracionMinutos) {
		if (origen == null) {
			throw new IllegalArgumentException("El origen no puede ser nulo");
		}
		if (destino == null) {
			throw new IllegalArgumentException("El destino no puede ser nulo");
		}
		if (puntos == null) {
			throw new IllegalArgumentException("La lista de puntos no puede ser nula");
		}
		if (distanciaKm < 0) {
			throw new IllegalArgumentException("La distancia no puede ser negativa");
		}
		if (duracionMinutos < 0) {
			throw new IllegalArgumentException("La duración no puede ser negativa");
		}
		this.origen = origen;
		this.destino = destino;
		this.puntos = new ArrayList<>(puntos);
		this.distanciaKm = distanciaKm;
		this.duracionMinutos = duracionMinutos;
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public List<PuntoRuta> getPuntos() {
		return Collections.unmodifiableList(puntos);
	}

	public double getDistanciaKm() {
		return distanciaKm;
	}

	public double getDuracionMinutos() {
		return duracionMinutos;
	}
}
package md.rutas.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import md.modelo.Ubicacion;

public class RutasViaje {

	private final Ubicacion origen;
	private final Ubicacion destino;
	private final List<RutaCalculada> rutas;

	public RutasViaje(Ubicacion origen, Ubicacion destino) {
		if (origen == null) {
			throw new IllegalArgumentException("El origen no puede ser nulo");
		}
		if (destino == null) {
			throw new IllegalArgumentException("El destino no puede ser nulo");
		}
		this.origen = origen;
		this.destino = destino;
		this.rutas = new ArrayList<>();
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public List<RutaCalculada> getRutas() {
		return Collections.unmodifiableList(rutas);
	}

	public void agregarRuta(RutaCalculada ruta) {
		if (ruta == null) {
			throw new IllegalArgumentException("La ruta no puede ser nula");
		}
		if (!ruta.getOrigen().equals(origen) || !ruta.getDestino().equals(destino)) {
			throw new IllegalArgumentException("La ruta no corresponde al origen y destino del viaje");
		}
		rutas.add(ruta);
	}

	public RutaCalculada getRutaMasCorta() {
		if (rutas.isEmpty()) {
			throw new IllegalStateException("No hay rutas disponibles para este viaje");
		}
		RutaCalculada mejor = rutas.get(0);
		for (int i = 1; i < rutas.size(); i++) {
			RutaCalculada actual = rutas.get(i);
			if (actual.getDistanciaKm() < mejor.getDistanciaKm()) {
				mejor = actual;
			}
		}
		return mejor;
	}
}
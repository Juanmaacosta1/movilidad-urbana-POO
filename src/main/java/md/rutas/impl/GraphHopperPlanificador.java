package md.rutas.impl;

import java.util.ArrayList;
import java.util.List;

import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.PuntoRuta;
import md.rutas.api.RutaCalculada;

public class GraphHopperPlanificador implements PlanificadorRutas {

	@Override
	public RutaCalculada calcularRuta(Ubicacion origen, Ubicacion destino) {
		if (origen == null) {
			throw new IllegalArgumentException("El origen no puede ser nulo");
		}
		if (destino == null) {
			throw new IllegalArgumentException("El destino no puede ser nulo");
		}

		double distanciaKm = origen.calcularDistancia(destino);
		double duracionMin = distanciaKm / 30.0 * 60.0;

		List<PuntoRuta> puntos = new ArrayList<>();
		puntos.add(new PuntoRuta(origen, 0, 0.0));
		puntos.add(new PuntoRuta(destino, 1, distanciaKm));

		return new RutaCalculada(origen, destino, puntos, distanciaKm, duracionMin);
	}
}
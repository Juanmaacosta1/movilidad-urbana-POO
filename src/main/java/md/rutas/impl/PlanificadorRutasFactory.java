package md.rutas.impl;

import md.rutas.api.PlanificadorRutas;

public final class PlanificadorRutasFactory {

	private PlanificadorRutasFactory() {
	}

	public static PlanificadorRutas crearPlanificador() {
		return new GraphHopperPlanificador();
	}
}
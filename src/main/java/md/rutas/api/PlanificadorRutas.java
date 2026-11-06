package md.rutas.api;

import md.modelo.Ubicacion;

public interface PlanificadorRutas {

	RutaCalculada calcularRuta(Ubicacion origen, Ubicacion destino);

	default boolean soportaRutas() {
		return true;
	}
}
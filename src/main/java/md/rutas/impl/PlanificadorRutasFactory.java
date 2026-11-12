package md.rutas.impl;

import java.util.Locale;

import md.rutas.api.PlanificadorRutas;

/** Elige qué implementación de {@link PlanificadorRutas} instanciar según el valor de rutas.motor. */
public final class PlanificadorRutasFactory {

	private PlanificadorRutasFactory() {
	}

	/** Crea el planificador con los valores de rutas.motor, rutas.osm y rutas.cache de config.properties. */
	public static PlanificadorRutas crearDesdeConfiguracion() throws java.io.IOException {
		md.config.CargaParametros.parametros();
		return crear(md.config.CargaParametros.getRutasMotor(), md.config.CargaParametros.getRutasOsm(),
				md.config.CargaParametros.getRutasCache());
	}

	public static PlanificadorRutas crear(String motor, String archivoOsm, String directorioCache) {
		if (motor == null || motor.isBlank()) {
			throw new IllegalArgumentException("Falta indicar el motor de rutas (rutas.motor)");
		}
		return switch (motor.trim().toLowerCase(Locale.ROOT)) {
		case "graphhopper" -> new GraphHopperPlanificador(archivoOsm, directorioCache);
		default -> throw new IllegalArgumentException("Motor de rutas no soportado: " + motor);
		};
	}
}

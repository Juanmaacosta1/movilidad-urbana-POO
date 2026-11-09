package md.simulacion;

import java.util.Optional;
import java.util.Random;

import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;

/**
 * Genera posiciones aleatorias dentro del área de simulación y comprueba, con el planificador,
 * que caigan cerca de una calle transitable.
 */
public class GeneradorUbicacionesViales {

	private static final int MAX_INTENTOS = 200;

	private final PlanificadorRutas planificador;
	private final double latitudMinima;
	private final double latitudMaxima;
	private final double longitudMinima;
	private final double longitudMaxima;
	private final double toleranciaMetros;
	private final Random azar;

	public GeneradorUbicacionesViales(PlanificadorRutas planificador, double latitud1, double latitud2,
			double longitud1, double longitud2, double toleranciaMetros) {
		this(planificador, latitud1, latitud2, longitud1, longitud2, toleranciaMetros, new Random());
	}

	/** Permite inyectar el generador de números aleatorios (útil para pruebas reproducibles). */
	public GeneradorUbicacionesViales(PlanificadorRutas planificador, double latitud1, double latitud2,
			double longitud1, double longitud2, double toleranciaMetros, Random azar) {
		if (planificador == null || azar == null) {
			throw new IllegalArgumentException("El planificador y el generador aleatorio son obligatorios");
		}
		if (toleranciaMetros <= 0) {
			throw new IllegalArgumentException("La tolerancia debe ser mayor a cero");
		}
		this.planificador = planificador;
		this.latitudMinima = Math.min(latitud1, latitud2);
		this.latitudMaxima = Math.max(latitud1, latitud2);
		this.longitudMinima = Math.min(longitud1, longitud2);
		this.longitudMaxima = Math.max(longitud1, longitud2);
		this.toleranciaMetros = toleranciaMetros;
		this.azar = azar;
	}

	/** @throws IllegalStateException si no se encuentra una posición vial en un número razonable de intentos */
	public Ubicacion generar() {
		for (int intento = 0; intento < MAX_INTENTOS; intento++) {
			Ubicacion candidata = new Ubicacion(
					latitudMinima + azar.nextDouble() * (latitudMaxima - latitudMinima),
					longitudMinima + azar.nextDouble() * (longitudMaxima - longitudMinima));
			Optional<Ubicacion> sobreCalle = planificador.ajustarACalle(candidata, toleranciaMetros);
			if (sobreCalle.isPresent()) {
				return sobreCalle.get();
			}
		}
		throw new IllegalStateException("No se encontró una posición sobre una calle en " + MAX_INTENTOS + " intentos");
	}
}

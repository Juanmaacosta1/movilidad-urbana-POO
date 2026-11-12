package md.simulacion;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import md.logica.Logica;
import md.logica.PlanificacionViajeService;
import md.modelo.CalificacionViaje;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutasViaje;

/**
 * Recorre el ciclo de vida de un viaje (aceptar, iniciar, finalizar) SIN esperar: usa un reloj
 * simulado. Cada transición se registra con la hora que habría tenido en la realidad, calculada
 * con la duración de los tramos (por calles si hay motor de rutas; si no, estimada en línea recta).
 *
 * Respeta las reglas de Logica y del modelo: el conductor tiene que ser compatible, y si nadie
 * puede tomar el pedido en el tiempo de espera, el viaje queda RECHAZADO.
 */
public class SimuladorViaje {

	/** Tiempo que espera la aplicación a que algún conductor acepte antes de rechazar el pedido. */
	public static final Duration ESPERA_MAXIMA_ACEPTACION = Duration.ofMinutes(2);

	private static final double VELOCIDAD_PROMEDIO_KMH = 30.0;
	private static final double FACTOR_APROXIMACION_CALLES = 1.3;
	private static final double RADIO_POSICION_INICIAL_GRADOS = 0.02; // unos 2 km
	private static final double TOLERANCIA_AJUSTE_METROS = 800;
	private static final int ESPERA_MINIMA_ACEPTACION_S = 5;
	private static final int ESPERA_MAXIMA_ACEPTACION_S = 30;
	private static final int ABORDAJE_S = 30;

	private final Logica logica;
	private final PlanificadorRutas planificador; // puede ser null
	private final PlanificacionViajeService planificacion; // null si no hay planificador
	private final Random azar;

	/** @param planificador motor de rutas, o null para estimar los tiempos en línea recta */
	public SimuladorViaje(Logica logica, PlanificadorRutas planificador) {
		this(logica, planificador, new Random());
	}

	public SimuladorViaje(Logica logica, PlanificadorRutas planificador, Random azar) {
		if (logica == null || azar == null) {
			throw new IllegalArgumentException("Logica y el generador aleatorio son obligatorios");
		}
		this.logica = logica;
		this.planificador = planificador;
		this.planificacion = planificador == null ? null : new PlanificacionViajeService(planificador);
		this.azar = azar;
	}

	// ------------------------------------------------------------------ API

	/** Lleva el viaje hasta que termine (FINALIZADO) o sea rechazado. */
	public ResultadoSimulacion simularCompleto(Viaje viaje) {
		List<String> pasos = new ArrayList<>();
		EstadoViaje estado = exigirActivo(viaje);
		while (estaActivo(estado)) {
			pasos.addAll(avanzarUnPaso(viaje).pasos());
			estado = viaje.estadoActual();
		}
		return new ResultadoSimulacion(estado, pasos);
	}

	/** SOLICITADO -> ACEPTADO (o RECHAZADO si no hay quién), ACEPTADO -> INICIADO, INICIADO -> FINALIZADO. */
	public ResultadoSimulacion avanzarUnPaso(Viaje viaje) {
		EstadoViaje estado = exigirActivo(viaje);
		List<String> pasos = new ArrayList<>();
		switch (estado) {
		case SOLICITADO -> aceptar(viaje, pasos);
		case ACEPTADO -> iniciar(viaje, pasos);
		case INICIADO -> finalizar(viaje, pasos);
		default -> throw new IllegalStateException("El viaje ya terminó: " + estado);
		}
		return new ResultadoSimulacion(viaje.estadoActual(), pasos);
	}

	/** Simula que pasó el tiempo de espera sin que ningún conductor aceptara: el pedido queda RECHAZADO. */
	public ResultadoSimulacion simularRechazoPorFaltaDeConductor(Viaje viaje) {
		EstadoViaje estado = exigirActivo(viaje);
		if (estado != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("Solo se puede rechazar por falta de conductor un pedido SOLICITADO");
		}
		List<String> pasos = new ArrayList<>();
		rechazarPorTiempo(viaje, pasos);
		return new ResultadoSimulacion(viaje.estadoActual(), pasos);
	}

	/**
	 * Kilómetros que, de forma estimada, ya recorrió el conductor: sirve para cobrar una cancelación
	 * posterior a la aceptación (costo proporcional a lo recorrido).
	 */
	public double kmRecorridosEstimados(Viaje viaje) {
		EstadoViaje estado = viaje.estadoActual();
		if (estado != EstadoViaje.ACEPTADO && estado != EstadoViaje.INICIADO) {
			return 0;
		}
		Tramos tramos = estimarTramos(viaje);
		return estado == EstadoViaje.ACEPTADO ? tramos.kmAcercamiento / 2
				: tramos.kmAcercamiento + tramos.kmTraslado / 2;
	}

	/** Si el conductor todavía no tiene posición, le asigna una cercana a la referencia (ajustada a calle si se puede). */
	public void asegurarPosicion(Usuario conductor, Ubicacion referencia) {
		Conductor datos = conductor.getConductor();
		if (datos == null || datos.getUbicacion() != null) {
			return;
		}
		double latitud = limitar(referencia.getLatitud() + (azar.nextDouble() * 2 - 1) * RADIO_POSICION_INICIAL_GRADOS, 90);
		double longitud = limitar(referencia.getLongitud() + (azar.nextDouble() * 2 - 1) * RADIO_POSICION_INICIAL_GRADOS, 180);
		Ubicacion posicion = new Ubicacion(latitud, longitud);
		if (planificador != null) {
			try {
				Optional<Ubicacion> sobreCalle = planificador.ajustarACalle(posicion, TOLERANCIA_AJUSTE_METROS);
				if (sobreCalle.isPresent()) {
					posicion = sobreCalle.get();
				}
			} catch (RuntimeException e) {
				// Sin motor utilizable: se queda con la posición sorteada
			}
		}
		datos.setUbicacion(posicion);
	}

	// ------------------------------------------------------------------ pasos

	private void aceptar(Viaje viaje, List<String> pasos) {
		Usuario conductor = elegirConductor(viaje, pasos);
		if (conductor == null) {
			rechazarPorTiempo(viaje, pasos);
			return;
		}
		LocalDateTime solicitado = viaje.getRegistroViaje().get(0).getFechaHora();
		LocalDateTime aceptado = relojSimulado(viaje).plusSeconds(
				ESPERA_MINIMA_ACEPTACION_S + azar.nextInt(ESPERA_MAXIMA_ACEPTACION_S - ESPERA_MINIMA_ACEPTACION_S + 1));
		logica.aceptarViaje(viaje, conductor, aceptado);
		pasos.add("Aceptado por " + conductor.getNombre() + " (" + viaje.getVehiculo().getModelo() + " "
				+ viaje.getVehiculo().getPatente() + ") a los " + Duration.between(solicitado, aceptado).getSeconds()
				+ " s de la solicitud.");
	}

	private void iniciar(Viaje viaje, List<String> pasos) {
		Tramos tramos = estimarTramos(viaje);
		LocalDateTime inicio = relojSimulado(viaje).plus(tramos.duracionAcercamiento).plusSeconds(ABORDAJE_S);
		logica.iniciarViaje(viaje, inicio);
		pasos.add("El conductor llegó al origen tras " + describir(tramos.duracionAcercamiento) + " ("
				+ String.format(java.util.Locale.ROOT, "%.1f", tramos.kmAcercamiento) + " km, "
				+ tramos.fuente() + ") y el viaje comenzó.");
	}

	private void finalizar(Viaje viaje, List<String> pasos) {
		Tramos tramos = estimarTramos(viaje);
		LocalDateTime fin = relojSimulado(viaje).plus(tramos.duracionTraslado);
		CalificacionViaje alConductor = calificacionAleatoria();
		CalificacionViaje alCliente = calificacionAleatoria();
		logica.finalizarViaje(viaje, alConductor, alCliente, fin);
		Conductor datos = viaje.getConductor().getConductor();
		datos.setUbicacion(viaje.getDestino()); // queda esperando donde terminó el viaje
		pasos.add("Llegaron al destino tras " + describir(tramos.duracionTraslado) + " ("
				+ String.format(java.util.Locale.ROOT, "%.1f", tramos.kmTraslado) + " km, " + tramos.fuente()
				+ "). Viaje finalizado y calificaciones registradas.");
	}

	private void rechazarPorTiempo(Viaje viaje, List<String> pasos) {
		LocalDateTime limite = relojSimulado(viaje).plus(ESPERA_MAXIMA_ACEPTACION);
		logica.rechazarViaje(viaje,
				"Ningún conductor aceptó el pedido en " + ESPERA_MAXIMA_ACEPTACION.toMinutes() + " minutos", limite);
		pasos.add("Ningún conductor aceptó el pedido en " + ESPERA_MAXIMA_ACEPTACION.toMinutes()
				+ " minutos: el pedido quedó rechazado y se le avisó al cliente.");
	}

	// ------------------------------------------------------------------ conductores

	private Usuario elegirConductor(Viaje viaje, List<String> pasos) {
		Usuario cliente = viaje.getCliente();
		List<Usuario> candidatos = new ArrayList<>();
		for (Usuario usuario : logica.buscarConductoresDisponibles(viaje)) {
			if (usuario != cliente) {
				candidatos.add(usuario);
			}
		}
		if (!candidatos.isEmpty()) {
			return masCercano(candidatos, viaje.getOrigen());
		}

		// Para que la simulación no dependa de haber entrado como chofer: se pone en servicio
		// al conductor compatible más cercano que esté fuera de servicio.
		for (Usuario usuario : logica.buscarConductoresCompatibles(viaje)) {
			if (usuario != cliente
					&& usuario.getConductor().getEstadoConductor() == EstadoConductor.FUERA_DE_SERVICIO) {
				candidatos.add(usuario);
			}
		}
		if (candidatos.isEmpty()) {
			return null;
		}
		Usuario elegido = masCercano(candidatos, viaje.getOrigen());
		Conductor datos = elegido.getConductor();
		logica.ponerConductorEnServicio(elegido, datos.getVehiculoActivo(), datos.getCategoriaVehiculoActivo());
		pasos.add(elegido.getNombre() + " estaba fuera de servicio: se lo puso Disponible para la simulación.");
		return elegido;
	}

	private Usuario masCercano(List<Usuario> candidatos, Ubicacion origen) {
		Usuario mejor = null;
		double menor = Double.MAX_VALUE;
		for (Usuario candidato : candidatos) {
			asegurarPosicion(candidato, origen);
			double distancia = candidato.getConductor().getUbicacion().calcularDistancia(origen);
			if (distancia < menor) {
				menor = distancia;
				mejor = candidato;
			}
		}
		return mejor;
	}

	// ------------------------------------------------------------------ tiempos y distancias

	private record Tramos(double kmAcercamiento, Duration duracionAcercamiento, double kmTraslado,
			Duration duracionTraslado, boolean porCalles) {

		String fuente() {
			return porCalles ? "por calles" : "estimado en línea recta";
		}
	}

	private Tramos estimarTramos(Viaje viaje) {
		Ubicacion posicion = posicionDelConductor(viaje);
		if (planificacion != null) {
			try {
				TipoVehiculo tipo = viaje.getVehiculo() == null ? TipoVehiculo.AUTO
						: viaje.getVehiculo().getTipoVehiculo();
				RutasViaje rutas = planificacion.planificar(posicion, viaje.getOrigen(), viaje.getDestino(), tipo);
				return new Tramos(rutas.acercamiento().distanciaKm(),
						Duration.ofMillis(rutas.acercamiento().duracionMilisegundos()), rutas.traslado().distanciaKm(),
						Duration.ofMillis(rutas.traslado().duracionMilisegundos()), true);
			} catch (RuntimeException e) {
				// Sin ruta por calles (fuera de cobertura, perfil no implementado, etc.): se estima
			}
		}
		double kmAcercamiento = estimarKm(posicion, viaje.getOrigen());
		double kmTraslado = estimarKm(viaje.getOrigen(), viaje.getDestino());
		return new Tramos(kmAcercamiento, duracionPara(kmAcercamiento), kmTraslado, duracionPara(kmTraslado), false);
	}

	private Ubicacion posicionDelConductor(Viaje viaje) {
		Usuario conductor = viaje.getConductor();
		if (conductor != null && conductor.getConductor().getUbicacion() != null) {
			return conductor.getConductor().getUbicacion();
		}
		return viaje.getOrigen();
	}

	private double estimarKm(Ubicacion desde, Ubicacion hasta) {
		return desde.calcularDistancia(hasta) * FACTOR_APROXIMACION_CALLES;
	}

	private Duration duracionPara(double km) {
		return Duration.ofSeconds(Math.round(km / VELOCIDAD_PROMEDIO_KMH * 3600));
	}

	// ------------------------------------------------------------------ utilidades

	/** La hora "actual" de la simulación es la del último evento del viaje. */
	private LocalDateTime relojSimulado(Viaje viaje) {
		List<md.modelo.RegistroViaje> registros = viaje.getRegistroViaje();
		return registros.get(registros.size() - 1).getFechaHora();
	}

	private EstadoViaje exigirActivo(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		EstadoViaje estado = viaje.estadoActual();
		if (!estaActivo(estado)) {
			throw new IllegalStateException("El viaje ya terminó o no fue solicitado: " + estado);
		}
		return estado;
	}

	private boolean estaActivo(EstadoViaje estado) {
		return estado == EstadoViaje.SOLICITADO || estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO;
	}

	private CalificacionViaje calificacionAleatoria() {
		int n = azar.nextInt(100);
		if (n < 5) {
			return CalificacionViaje.MALO;
		} else if (n < 15) {
			return CalificacionViaje.REGULAR;
		} else if (n < 40) {
			return CalificacionViaje.BUENO;
		} else if (n < 70) {
			return CalificacionViaje.MUY_BUENO;
		}
		return CalificacionViaje.EXCELENTE;
	}

	private double limitar(double valor, double maximo) {
		return Math.max(-maximo, Math.min(maximo, valor));
	}

	private String describir(Duration duracion) {
		long segundos = duracion.getSeconds();
		return segundos < 60 ? segundos + " s" : (segundos / 60) + " min " + (segundos % 60) + " s";
	}
}

package md.rutas.impl;

import java.util.Optional;
import java.util.function.Supplier;

import md.logica.PlanificacionViajeService;
import md.rutas.api.PlanificadorRutas;

/**
 * Administra el motor de rutas de toda la aplicación. Importar el mapa puede tardar minutos, así
 * que se carga en segundo plano; mientras no esté LISTO (o si falta el .osm) la aplicación sigue
 * funcionando con estimaciones en línea recta.
 */
public final class MotorRutas {

	public enum Estado {
		SIN_INICIAR, CARGANDO, LISTO, NO_DISPONIBLE
	}

	private static final MotorRutas INSTANCIA = new MotorRutas(() -> {
		try {
			return PlanificadorRutasFactory.crearDesdeConfiguracion();
		} catch (java.io.IOException e) {
			throw new IllegalStateException("No se pudo leer config.properties: " + e.getMessage(), e);
		}
	});

	private final Supplier<PlanificadorRutas> fabrica;
	private volatile Estado estado = Estado.SIN_INICIAR;
	private volatile String detalle = "El motor de rutas todavía no se inició.";
	private volatile PlanificadorRutas planificador;

	/** Para pruebas: permite inyectar cómo se crea el planificador. */
	MotorRutas(Supplier<PlanificadorRutas> fabrica) {
		this.fabrica = fabrica;
	}

	public static MotorRutas getInstance() {
		return INSTANCIA;
	}

	/** Inicia la carga en un hilo aparte (no bloquea la interfaz). Si ya se inició, no hace nada. */
	public void iniciarCargaEnSegundoPlano() {
		if (!reservarCarga()) {
			return;
		}
		Thread hilo = new Thread(this::cargar, "carga-motor-rutas");
		hilo.setDaemon(true);
		hilo.start();
	}

	/** Carga el motor en el hilo actual (bloquea hasta terminar). Si ya se inició, no hace nada. */
	public void cargarAhora() {
		if (reservarCarga()) {
			cargar();
		}
	}

	private synchronized boolean reservarCarga() {
		if (estado != Estado.SIN_INICIAR) {
			return false;
		}
		estado = Estado.CARGANDO;
		detalle = "Importando el mapa vial (la primera vez puede tardar varios minutos)...";
		return true;
	}

	private void cargar() {
		try {
			PlanificadorRutas creado = fabrica.get();
			synchronized (this) {
				planificador = creado;
				estado = Estado.LISTO;
				detalle = "Mapa vial cargado: las rutas se calculan por calles.";
			}
		} catch (Exception | LinkageError | OutOfMemoryError e) {
			synchronized (this) {
				estado = Estado.NO_DISPONIBLE;
				detalle = "Sin rutas por calles (se usan estimaciones en línea recta): " + e.getMessage();
			}
			System.err.println("[MotorRutas] " + detalle);
		}
	}

	public Estado getEstado() {
		return estado;
	}

	public String getDetalle() {
		return detalle;
	}

	public Optional<PlanificadorRutas> planificador() {
		return estado == Estado.LISTO ? Optional.ofNullable(planificador) : Optional.empty();
	}

	public Optional<PlanificacionViajeService> servicioPlanificacion() {
		return planificador().map(PlanificacionViajeService::new);
	}

	public synchronized void cerrar() {
		if (planificador != null) {
			planificador.close();
			planificador = null;
		}
		estado = Estado.SIN_INICIAR;
		detalle = "El motor de rutas fue cerrado.";
	}
}

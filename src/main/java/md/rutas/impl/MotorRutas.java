package md.rutas.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import md.config.CargaParametros;
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
	private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		Thread hilo = new Thread(r, "carga-motor-rutas");
		hilo.setDaemon(true);
		return hilo;
	});
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
		if (!mapaDisponible()) {
			estado = Estado.NO_DISPONIBLE;
			detalle = "Sin rutas por calles (se usan estimaciones en línea recta): falta el mapa OSM o la caché del proyecto.";
			return;
		}
		executor.submit(this::cargar);
	}

	/** Carga el motor en el hilo actual (bloquea hasta terminar). Si ya se inició, no hace nada. */
	public void cargarAhora() {
		if (reservarCarga()) {
			if (!mapaDisponible()) {
				estado = Estado.NO_DISPONIBLE;
				detalle = "Sin rutas por calles (se usan estimaciones en línea recta): falta el mapa OSM o la caché del proyecto.";
				return;
			}
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

	private boolean mapaDisponible() {
		try {
			CargaParametros.parametros();
			String osm = CargaParametros.getRutasOsm();
			String cache = CargaParametros.getRutasCache();
			return (osm != null && !osm.isBlank() && Files.isRegularFile(Path.of(osm)))
					|| (cache != null && !cache.isBlank() && Files.isDirectory(Path.of(cache)));
		} catch (IOException e) {
			return false;
		}
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
		executor.shutdownNow();
		estado = Estado.SIN_INICIAR;
		detalle = "El motor de rutas fue cerrado.";
	}
}

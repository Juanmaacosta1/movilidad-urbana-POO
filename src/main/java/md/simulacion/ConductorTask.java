package md.simulacion;

import java.util.concurrent.ThreadLocalRandom;

import md.logica.Logica;
import md.modelo.CalificacionViaje;
import md.modelo.EstadoViaje;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class ConductorTask implements Runnable {

	private final Usuario conductor;
	private final ColaSolicitudes cola;
	private final Logica logica;
	private static final double VELOCIDAD_PROMEDIO = 30.0; // km/h
	private Ubicacion ubicacionActual;
	private double gananciaTotal = 0;
	private int viajesFinalizados = 0;
	private int sumaCalificaciones = 0;

	public ConductorTask(Usuario conductor, ColaSolicitudes cola, Logica logica) {

		if (conductor == null || !conductor.esConductor()) {
			throw new IllegalArgumentException("El usuario debe ser conductor");
		}

		if (cola == null || logica == null) {
			throw new IllegalArgumentException("La cola y la logica son obligatorias");
		}

		this.conductor = conductor;
		this.cola = cola;
		this.logica = logica;

		double latitud = ThreadLocalRandom.current().nextDouble(-42.795, -42.740);

		double longitud = ThreadLocalRandom.current().nextDouble(-65.080, -65.040);

		this.ubicacionActual = new Ubicacion(latitud, longitud);
	}

	public double getGananciaTotal() {
		return gananciaTotal;
	}

	public int getViajesFinalizados() {
		return viajesFinalizados;
	}

	public Usuario getConductor() {
		return conductor;
	}
	
	public double getCalificacionPromedio() {

	    if (viajesFinalizados == 0) {
	        return 0;
	    }

	    return (double) sumaCalificaciones / viajesFinalizados;
	}

	@Override
	public void run() {

		System.out.println("Conductor " + conductor.getNombre() + " inicia su jornada.");
		Viaje viajeEnCurso = null;

		try {

			while (true) {

				Viaje viaje = cola.tomarSolicitud(conductor, ubicacionActual);

				if (viaje == null) {
					break;
				}

				viajeEnCurso = viaje;

				System.out.println("Conductor " + conductor.getNombre() + " recibió una solicitud del cliente "
						+ viaje.getCliente().getNombre());

				// ETAPA 1: Aceptar el viaje
				if (ThreadLocalRandom.current().nextInt(100) < 5) {

					logica.rechazarViaje(viaje, "Rechazo aleatorio del conductor");

					System.out.println(conductor.getNombre() + " rechazó una solicitud.");

					viajeEnCurso = null;
					continue;
				}

				try {
					logica.aceptarViaje(viaje, conductor);
					System.out.println("Conductor " + conductor.getNombre() + " aceptó el viaje.");

				} catch (IllegalStateException | IllegalArgumentException e) {

					System.out.println(
							"Conductor " + conductor.getNombre() + " no pudo aceptar el viaje: " + e.getMessage());

					if (viaje.estadoActual() == EstadoViaje.SOLICITADO) {
						logica.rechazarViaje(viaje, "No se pudo asignar");
					}

					viajeEnCurso = null;
					continue;
				}
				if (ThreadLocalRandom.current().nextInt(100) < 3) {

					logica.cancelarViaje(viaje, conductor, "Cancelación aleatoria del conductor");

					System.out.println(conductor.getNombre() + " canceló un viaje.");

					viajeEnCurso = null;
					continue;
				}

				// Simular el traslado hasta el cliente
				// Distancia entre el conductor y el cliente
				double distanciaAlCliente = ubicacionActual.calcularDistancia(viaje.getOrigen());

				// Tiempo estimado en minutos, a 30 km/h
				double minutosAproximacion = (distanciaAlCliente / VELOCIDAD_PROMEDIO) * 60;

				// Aceleramos el tiempo para la simulación
				long esperaMs = Math.max(500L, Math.round(minutosAproximacion * 500));

				Thread.sleep(esperaMs);

				// El conductor llegó al origen del cliente
				ubicacionActual = viaje.getOrigen();

				// ETAPA 2: Iniciar el viaje
				logica.iniciarViaje(viaje);

				System.out.println("Conductor " + conductor.getNombre() + " recogió al cliente "
						+ viaje.getCliente().getNombre() + " e inició el viaje.");
				// ETAPA 3: Traslado hacia el destino
				System.out.println("Conductor " + conductor.getNombre() + " está trasladando al cliente "
						+ viaje.getCliente().getNombre());

				Thread.sleep(6000);

				// ETAPA 4: Finalizar el viaje
				int probabilidad = ThreadLocalRandom.current().nextInt(100);

				CalificacionViaje calificacionConductor;

				if (probabilidad < 2) {
					calificacionConductor = CalificacionViaje.MALO;
				} else if (probabilidad < 10) {
					calificacionConductor = CalificacionViaje.REGULAR;
				} else if (probabilidad < 35) {
					calificacionConductor = CalificacionViaje.BUENO;
				} else if (probabilidad < 75) {
					calificacionConductor = CalificacionViaje.MUY_BUENO;
				} else {
					calificacionConductor = CalificacionViaje.EXCELENTE;
				}

				logica.finalizarViaje(viaje, calificacionConductor, CalificacionViaje.BUENO);

				switch (calificacionConductor) {
				case MALO -> sumaCalificaciones += 1;
				case REGULAR -> sumaCalificaciones += 2;
				case BUENO -> sumaCalificaciones += 3;
				case MUY_BUENO -> sumaCalificaciones += 4;
				case EXCELENTE -> sumaCalificaciones += 5;
				default -> {
				}
				}

				System.out.println("Calificación recibida por " + conductor.getNombre() + ": " + calificacionConductor);
				ubicacionActual = viaje.getDestino();

				// Calcular distancia y duración estimada

				double kilometros = viaje.getOrigen().calcularDistancia(viaje.getDestino());

				double minutos = kilometros / VELOCIDAD_PROMEDIO * 60;

				// Convertir a minutos y segundos
				long segundosTotales = Math.round(minutos * 60);

				long minutosEnteros = segundosTotales / 60;

				long segundos = segundosTotales % 60;

				// Calcular importe
				double importe = viaje.getServicio().calcularCosto(kilometros, minutos);

				// Acumular ganancias
				gananciaTotal += importe;
				viajesFinalizados++;

				System.out.println("Conductor " + conductor.getNombre() + " finalizó el viaje del cliente "
						+ viaje.getCliente().getNombre());

				System.out.printf("[Conductor %s] Viaje completado: %.2f km | %d min %02d s | $%.2f%n",
						conductor.getNombre(), kilometros, minutosEnteros, segundos, importe);

				System.out.println("Conductor " + conductor.getNombre() + " vuelve a estar disponible.");
				viajeEnCurso = null;
			}

		} catch (InterruptedException e) {

			if (viajeEnCurso != null) {

				try {
					EstadoViaje estado = viajeEnCurso.estadoActual();

					if (estado == EstadoViaje.SOLICITADO) {

						logica.rechazarViaje(viajeEnCurso, "Simulación interrumpida");

					} else if (estado == EstadoViaje.ACEPTADO || estado == EstadoViaje.INICIADO) {

						logica.cancelarViaje(viajeEnCurso, conductor, "Simulación interrumpida", 0);
					}

				} catch (RuntimeException error) {
					System.err.println("No se pudo cerrar el viaje interrumpido: " + error.getMessage());
				}
			}

			Thread.currentThread().interrupt();

			System.out.println("Conductor " + conductor.getNombre() + " fue interrumpido.");
		}

		System.out.println("Conductor " + conductor.getNombre() + " termina su jornada.");
	}
}

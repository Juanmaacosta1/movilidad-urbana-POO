package md.simulacion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import md.logica.Logica;
import md.modelo.Usuario;
import java.util.HashMap;
import java.util.Map;
import md.modelo.Ubicacion;

import md.modelo.Viaje;

public class ColaSolicitudes {

	private final List<Viaje> solicitudes = new ArrayList<>();

	private final Map<Usuario, Ubicacion> conductoresEnEspera = new HashMap<>();

	private final Lock lock = new ReentrantLock();

	private final Condition haySolicitudes = lock.newCondition();

	private boolean cerrada = false;

	public void agregarSolicitud(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		lock.lock();

		try {
			if (cerrada) {
				throw new IllegalStateException("No se pueden agregar solicitudes: jornada finalizada");
			}
			solicitudes.add(viaje);

			haySolicitudes.signalAll();

		} finally {
			lock.unlock();
		}
	}

	public Viaje tomarSolicitud(Usuario conductor, Ubicacion ubicacion) throws InterruptedException {

		lock.lock();

		try {
			// Registrar al conductor como disponible para recibir solicitudes
			conductoresEnEspera.put(conductor, ubicacion);
			haySolicitudes.signalAll();

			while (true) {

				for (int i = 0; i < solicitudes.size(); i++) {

					Viaje viaje = solicitudes.get(i);

					Usuario masCercano = null;
					double menorDistancia = Double.MAX_VALUE;

					// Buscar conductores compatibles con el viaje
					for (Usuario disponible : Logica.getInstance().buscarConductoresDisponibles(viaje)) {

						Ubicacion posicion = conductoresEnEspera.get(disponible);

						// Solo considerar conductores esperando solicitudes
						if (posicion == null) {
							continue;
						}

						double distancia = posicion.calcularDistancia(viaje.getOrigen());

						if (distancia < menorDistancia) {
							menorDistancia = distancia;
							masCercano = disponible;
						}
					}

					// Solo el conductor más cercano puede tomar este viaje
					if (masCercano == conductor) {

						conductoresEnEspera.remove(conductor);

						Viaje asignado = solicitudes.remove(i);

						// Avisar que cambió la disponibilidad
						haySolicitudes.signalAll();

						return asignado;
					}
				}

				if (cerrada) {

					boolean tieneCompatible = false;

					for (Viaje pendiente : solicitudes) {

						for (Usuario disponible : Logica.getInstance().buscarConductoresDisponibles(pendiente)) {

							if (disponible == conductor) {
								tieneCompatible = true;
								break;
							}
						}

						if (tieneCompatible) {
							break;
						}
					}

					if (!tieneCompatible) {
						return null;
					}
				}

				haySolicitudes.await();
			}

		} finally {
			conductoresEnEspera.remove(conductor);
			haySolicitudes.signalAll();
			lock.unlock();
		}
	}

	public void cerrar() {

		lock.lock();

		try {
			cerrada = true;
			haySolicitudes.signalAll();

		} finally {
			lock.unlock();
		}
	}

	public int cantidadPendientes() {

		lock.lock();

		try {
			return solicitudes.size();

		} finally {
			lock.unlock();
		}
	}
}

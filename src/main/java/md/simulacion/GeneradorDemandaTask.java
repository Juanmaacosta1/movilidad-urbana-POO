package md.simulacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import md.logica.Logica;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class GeneradorDemandaTask implements Runnable {

	private final Logica logica;
	private final ColaSolicitudes cola;
	private final List<Usuario> clientes;
	private final int cantidadSolicitudes;

	public GeneradorDemandaTask(Logica logica, ColaSolicitudes cola, List<Usuario> clientes, int cantidadSolicitudes) {

		this.logica = logica;
		this.cola = cola;
		this.clientes = clientes;
		this.cantidadSolicitudes = cantidadSolicitudes;
	}

	@Override
	public void run() {

		List<Usuario> clientesMezclados = new ArrayList<>(clientes);

		Collections.shuffle(clientesMezclados);

		int cantidad = Math.min(cantidadSolicitudes, clientesMezclados.size());

		try {

			for (int i = 0; i < cantidad; i++) {

				Usuario cliente = clientesMezclados.get(i);

				int indiceServicio = ThreadLocalRandom.current().nextInt(logica.getServicios().size());

				Servicio servicio = logica.getServicios().get(indiceServicio);

				Ubicacion origen = generarUbicacion();
				Ubicacion destino = generarUbicacion();

				Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

				cola.agregarSolicitud(viaje);

				System.out.println("[Cliente " + cliente.getNombre() + "] Solicita un viaje: " + servicio.getNombre());

				Thread.sleep(2000);
			}

		} catch (InterruptedException e) {

			Thread.currentThread().interrupt();
			System.out.println("Generador de demanda interrumpido.");
		}
	}

	private Ubicacion generarUbicacion() {

		double latitud = ThreadLocalRandom.current().nextDouble(-42.795, -42.740);

		double longitud = ThreadLocalRandom.current().nextDouble(-65.080, -65.040);

		return new Ubicacion(latitud, longitud);
	}
}

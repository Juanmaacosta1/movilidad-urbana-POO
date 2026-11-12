package md.simulacion;

import md.logica.Logica;
import java.util.ArrayList;
import java.util.List;
import md.modelo.Usuario;
import md.modelo.Conductor;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Viaje;
import md.modelo.CategoriaVehiculo;

public class MainSimulacion {

	public static void main(String[] args) throws InterruptedException {

		Logica logica = Logica.getInstance();

		System.out.println("===== MOVILIDAD DIGITAL =====");
		System.out.println("Usuarios cargados: " + logica.getUsuarios().size());

		System.out.println("Conductores cargados: " + logica.getConductores().size());

		System.out.println("Servicios cargados: " + logica.getServicios().size());
		List<Usuario> clientes = new ArrayList<>();

		for (Usuario usuario : logica.getUsuarios()) {

			if (!usuario.esConductor()) {
				clientes.add(usuario);
			}
		}

		int conductoresHabilitados = 0;

		for (Usuario usuario : logica.getConductores()) {

			Conductor conductor = usuario.getConductor();

			logica.ponerConductorEnServicio(usuario, conductor.getVehiculoActivo(), CategoriaVehiculo.ESTANDAR);

			conductoresHabilitados++;
		}

		System.out.println("Conductores en servicio: " + conductoresHabilitados);

		System.out.println("Clientes para simulación: " + clientes.size());

		System.out.println("\n===== COMPATIBILIDAD DE SERVICIOS =====");

		Ubicacion origen = new Ubicacion(-42.7692, -65.0385);
		Ubicacion destino = new Ubicacion(-42.7750, -65.0450);

		for (Servicio servicio : logica.getServicios()) {

			Viaje viajePrueba = new Viaje(clientes.get(0), origen, destino, servicio);

			int disponibles = logica.buscarConductoresDisponibles(viajePrueba).size();

			System.out.println(servicio.getNombre() + ": " + disponibles + " conductores compatibles");
		}

		System.out.println("\n===== INICIO DE SIMULACIÓN =====");

// Crear la cola compartida
		ColaSolicitudes cola = new ColaSolicitudes();

// Lista para almacenar los hilos conductores
		List<Thread> hilosConductores = new ArrayList<>();
		List<ConductorTask> tareasConductores = new ArrayList<>();

// Iniciar un hilo por conductor
		for (Usuario usuario : logica.getConductores()) {

			ConductorTask tarea = new ConductorTask(usuario, cola, logica);

			Thread hilo = new Thread(tarea, "Conductor-" + usuario.getNombre());

			hilosConductores.add(hilo);
			tareasConductores.add(tarea);
			hilo.start();
		}

// Crear el generador de solicitudes
		GeneradorDemandaTask generador = new GeneradorDemandaTask(logica, cola, clientes, 20);

		Thread hiloGenerador = new Thread(generador, "GeneradorDemanda");

		try {
			hiloGenerador.start();

			// Esperar que termine de generar solicitudes
			hiloGenerador.join();

		} finally {
			// Avisar que no llegarán nuevas solicitudes
			cola.cerrar();
		}

// Esperar que terminen los conductores
		for (Thread hilo : hilosConductores) {
			hilo.join();
		}

		System.out.println("Solicitudes pendientes: " + cola.cantidadPendientes());

		System.out.println("\n========== FIN DE JORNADA ==========");

		double totalEmpresa = 0;
		int totalViajes = 0;

		for (ConductorTask tarea : tareasConductores) {

			System.out.println("\nConductor: " + tarea.getConductor().getNombre());

			System.out.println("Viajes realizados: " + tarea.getViajesFinalizados());

			if (tarea.getViajesFinalizados() > 0) {
				System.out.printf("Calificación promedio: %.2f / 5%n", tarea.getCalificacionPromedio());
			} else {
				System.out.println("Calificación: Sin calificaciones");
			}

			System.out.printf("Ganancia: $%.2f%n", tarea.getGananciaTotal());

			totalEmpresa += tarea.getGananciaTotal();
			totalViajes += tarea.getViajesFinalizados();
		}

		System.out.println("\n--------------------------------");
		System.out.println("Total de viajes: " + totalViajes);

		System.out.printf("TOTAL EMPRESA: $%.2f%n", totalEmpresa);

		System.out.println("================================");

	}
}

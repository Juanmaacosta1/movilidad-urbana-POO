package pruebas;

import java.util.Random;

import datos.CargarParametros;
import logica.Logica;
import modelo.CalificacionViaje;
import modelo.CategoriaVehiculo;
import modelo.Conductor;
import modelo.Servicio;
import modelo.Ubicacion;
import modelo.Usuario;
import modelo.Vehiculo;
import modelo.Viaje;

public class TestLogica {

	public static void main(String[] args) {

		try {
			Logica logica = Logica.getInstance();

			CargarParametros.parametros();

			Ubicacion origen = new Ubicacion(CargarParametros.getLatitud1(), CargarParametros.getLongitud1());

			Ubicacion destino = new Ubicacion(CargarParametros.getLatitud2(), CargarParametros.getLongitud2());

			Usuario cliente = null;

			for (Usuario usuario : logica.getUsuarios()) {
				if (!usuario.esConductor()) {
					cliente = usuario;
					break;
				}
			}

			if (cliente == null) {
				throw new IllegalStateException("No se encontró un cliente");
			}

			Usuario conductorUsuario = null;
			Vehiculo vehiculo = null;

			for (Usuario usuario : logica.getUsuarios()) {

				if (usuario.esConductor()) {

					Conductor conductor = usuario.getConductor();

					if (!conductor.getVehiculos().isEmpty()) {

						conductorUsuario = usuario;
						vehiculo = conductor.getVehiculos().get(0);

						break;
					}
				}
			}

			if (conductorUsuario == null) {
				throw new IllegalStateException("No se encontró un conductor");
			}

			// Buscar un servicio compatible
			Servicio servicioElegido = null;

			for (Servicio servicio : logica.getServicios()) {

				if (vehiculo.presta(servicio.getTipoServicio())
						&& vehiculo.getTipoVehiculo() == servicio.getTipoVehiculo()
						&& servicio.getCategoriaVehiculo().getValor() <= vehiculo.getCategoriaVehiculo().getValor()) {

					servicioElegido = servicio;
					break;
				}
			}

			if (servicioElegido == null) {
				throw new IllegalStateException("No se encontró un servicio compatible");
			}

			// Poner conductor en servicio
			logica.ponerConductorEnServicio(conductorUsuario, vehiculo, CategoriaVehiculo.ESTANDAR);

			// Solicitar viaje
			Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicioElegido);

			// Buscar conductor disponible
			Usuario conductorDisponible = null;

			for (Usuario usuario : logica.buscarConductoresDisponibles(viaje)) {

				conductorDisponible = usuario;
				break;
			}

			if (conductorDisponible == null) {
				throw new IllegalStateException("No hay conductores disponibles");
			}

			Random random = new Random();

			CalificacionViaje[] calificaciones = { CalificacionViaje.MALO, CalificacionViaje.REGULAR,
					CalificacionViaje.BUENO, CalificacionViaje.MUY_BUENO, CalificacionViaje.EXCELENTE };

			CalificacionViaje calificacionConductor = calificaciones[random.nextInt(calificaciones.length)];

			CalificacionViaje calificacionCliente = calificaciones[random.nextInt(calificaciones.length)];

			// Ciclo de vida
			logica.aceptarViaje(viaje, conductorDisponible);

			logica.iniciarViaje(viaje);

			logica.finalizarViaje(viaje, calificacionConductor, calificacionCliente);

			// Salida por pantalla
			System.out.println("=======================================================");
			System.out.println("                  RESUMEN DE VIAJE");
			System.out.println("=======================================================");

			System.out.println("Cliente:            " + cliente);

			System.out.println("Origen:             " + origen);

			System.out.println("Destino:            " + destino);

			System.out.println("Servicio:           " + servicioElegido.getNombre());

			System.out.println("Tipo de servicio:   " + servicioElegido.getTipoServicio());

			System.out.println("Categoría:          " + servicioElegido.getCategoriaVehiculo());

			System.out.println("Tarifa base:        $" + servicioElegido.getTarifaBase());

			System.out.println("Conductor:          " + conductorDisponible);

			System.out.println("Vehículo:           " + vehiculo);

			System.out.println("Estado final:       " + viaje.estadoActual());

			System.out.println("Calif. cliente:     " + calificacionCliente);

			System.out.println("Calif. conductor:   " + calificacionConductor);

			System.out.println("=======================================================");

		} catch (Exception e) {

			System.out.println("ERROR EN LA PRUEBA: " + e.getMessage());

			e.printStackTrace();
		}
	}
}
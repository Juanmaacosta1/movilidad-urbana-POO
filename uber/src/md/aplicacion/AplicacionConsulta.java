package md.aplicacion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import md.datos.*;
import md.logica.Logica;
import md.modelo.*;


public class AplicacionConsulta {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		try {
			// 1) rutas de los archivos (config.properties)
			CargarParametros.parametros();

			// 2) datos: primero los vehículos, porque los usuarios conductores los usan
			List<Servicio> servicios = CargarDatos.cargarServicios(CargarParametros.getArchivoServicios());
			Set<Vehiculo> vehiculos = CargarDatos.cargarVehiculos(CargarParametros.getArchivoVehiculos());
			Set<Usuario> usuarios = CargarDatos.cargarUsuarios(CargarParametros.getArchivoUsuarios(), vehiculos);

			// 3) mostrar por pantalla
			System.out.println("=======================================================");
			System.out.println("               DATOS CARGADOS                          ");
			System.out.println("=======================================================");
			System.out.println("Servicios: " + servicios.size());
			System.out.println("Vehículos: " + vehiculos.size());
			System.out.println("Usuarios:  " + usuarios.size());

			System.out.println();
			System.out.println("---------------- Usuarios ----------------");
			List<Usuario> ordenados = new ArrayList<>(usuarios);
			ordenados.sort((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()));
			for (Usuario u : ordenados) {
				String rol = "Cliente";
				if (u.esConductor()) {
					rol = "Cliente y conductor (" + u.getConductor().getVehiculos().size() + " vehículo/s)";
				}
				System.out.println(u.getNombre() + " | " + u.getTelefono() + " | " + u.getEmail() + " | " + rol);
			}

		} catch (IOException e) {
			System.out.println("No se pudo leer un archivo: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println("Hay un dato inválido en los archivos: " + e.getMessage());
		}
	
	}

	
	   private static void consultarServicios(Logica logica) {

	        System.out.println("\n===== SERVICIOS =====");

	        for (Servicio servicio : logica.getServicios()) {

	            System.out.println(
	                    servicio.getNombre()
	                    + " | Tarifa base: $" + servicio.getTarifaBase()
	                    + " | Precio/km: $" + servicio.getPrecioKm()
	                    + " | Precio/minuto: $" + servicio.getPrecioMinuto()
	                    + " | Vehículo: " + servicio.getTipoVehiculo()
	                    + " | Categoría: " + servicio.getCategoriaVehiculo()
	                    + " | Tipo: " + servicio.getTipoServicio());
	        }
	    }

	

	    private static void consultarConductoresEnServicio(Logica logica) {

	        System.out.println("\n===== CONDUCTORES EN SERVICIO =====");

	        for (Usuario usuario : logica.getConductoresEnServicio()) {

	            System.out.println(
	                    usuario.getNombre()
	                    + " | " + usuario.getEmail()
	                    + " | Estado: "
	                    + usuario.getConductor().getEstadoConductor());
	        }
	    }

	    // --------------------------------------------------
	    // CONSULTAR VIAJES
	    // --------------------------------------------------

	    private static void consultarViajes(Logica logica) {

	        System.out.println("\n===== VIAJES =====");

	        if (logica.getViajes().isEmpty()) {
	            System.out.println("Todavía no hay viajes registrados.");
	            return;
	        }

	        for (Viaje viaje : logica.getViajes()) {

	            System.out.println(
	                    "ID: " + viaje.getId()
	                    + " | Cliente: " + viaje.getCliente().getNombre()
	                    + " | Servicio: " + viaje.getServicio().getNombre()
	                    + " | Estado: " + viaje.estadoActual());
	        }
	    }
	    
	    // --------------------------------------------------
	    // NUEVA CONSULTA 2: HISTORIAL DE UN CLIENTE
	    // --------------------------------------------------

	    private static void consultarHistorialCliente(
	            Logica logica, Scanner entrada) {

	        System.out.print("Ingrese el email del cliente: ");
	        String email = entrada.nextLine().trim();

	        Usuario usuario = logica.buscarUsuario(email);

	        if (usuario == null) {
	            System.out.println("No existe un usuario con ese email.");
	            return;
	        }

	        java.util.List<Viaje> viajes = usuario.getCliente().getViajes();

	        System.out.println("\n===== HISTORIAL DE VIAJES =====");
	        System.out.println("Cliente: " + usuario.getNombre());

	        if (viajes.isEmpty()) {
	            System.out.println("El cliente todavía no tiene viajes registrados.");
	            return;
	        }

	        for (Viaje viaje : viajes) {

	            System.out.println("\n--------------------------------");
	            System.out.println("ID: " + viaje.getId());
	            System.out.println("Servicio: " + viaje.getServicio().getNombre());
	            System.out.println("Estado actual: " + viaje.estadoActual());

	            if (viaje.getConductor() != null) {
	                System.out.println(
	                        "Conductor: " + viaje.getConductor().getNombre());
	            } else {
	                System.out.println("Conductor: Sin asignar");
	            }

	            System.out.println("Historial de estados:");

	            for (RegistroViaje registro : viaje.getRegistroViaje()) {

	                System.out.println(
	                        "  " + registro.getFechaHora()
	                        + " | " + registro.getEstadoViaje());
	            }
	        }
	    }

	    // --------------------------------------------------
	    // NUEVA CONSULTA 3: ESTADÍSTICAS DE UN CONDUCTOR
	    // --------------------------------------------------

	    private static void consultarEstadisticasConductor(
	            Logica logica, Scanner entrada) {

	        System.out.print("Ingrese el email del conductor: ");
	        String email = entrada.nextLine().trim();

	        Usuario usuario = logica.buscarUsuario(email);

	        if (usuario == null) {
	            System.out.println("No existe un usuario con ese email.");
	            return;
	        }

	        if (!usuario.esConductor()) {
	            System.out.println("El usuario no está registrado como conductor.");
	            return;
	        }

	        Conductor conductor = usuario.getConductor();

	        int viajesFinalizados =
	                logica.cantidadDeViajesFinalizados(usuario);

	        double promedio =
	                logica.calcularPromedioEstrellas(usuario);

	        System.out.println("\n===== ESTADÍSTICAS DEL CONDUCTOR =====");
	        System.out.println("Nombre: " + usuario.getNombre());
	        System.out.println("Email: " + usuario.getEmail());
	        System.out.println("Licencia: " + conductor.getLicenciaConducir());
	        System.out.println("Estado: " + conductor.getEstadoConductor());
	        System.out.println("Viajes finalizados: " + viajesFinalizados);
	        System.out.printf("Promedio de calificaciones: %.2f / 5%n", promedio);

	        System.out.println("Vehículos registrados:");

	        for (Vehiculo vehiculo : conductor.getVehiculos()) {
	            System.out.println("  " + vehiculo);
	        }
	    }
}

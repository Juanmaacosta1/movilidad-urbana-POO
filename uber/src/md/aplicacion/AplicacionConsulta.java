package aplicacion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import datos.*;
import modelo.*;


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

}

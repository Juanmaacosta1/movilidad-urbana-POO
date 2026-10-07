package md.datos;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import md.modelo.Vehiculo;

public class CargarDatos {

	public static ArrayList<Servicio> cargarServicios(String fileName) throws FileNotFoundException, IOException {
		ArrayList<Servicio> servicios = new ArrayList<>();
		try (BufferedReader bf = new BufferedReader(new FileReader(fileName))) {
			String linea;
			while ((linea = bf.readLine()) != null) {

				if (linea.isBlank() || linea.trim().startsWith("#")) {
					continue;
				}
				String[] partes = linea.split(";");
				String nombre = partes[0].trim();
				double tarifa = Double.parseDouble(partes[1].trim());
				double precioKm = Double.parseDouble(partes[2].trim());
				double precioMinuto = Double.parseDouble(partes[3].trim());
				TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[4].trim().toUpperCase());
				CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[5].trim().toUpperCase());
				TipoServicio tipoServicio = TipoServicio.valueOf(partes[6].trim().toUpperCase());

				Servicio servicio = new Servicio(nombre, tarifa, precioKm, precioMinuto, tipoVehiculo,
						categoriaVehiculo, tipoServicio);
				servicios.add(servicio);
			}

		}
		return servicios;

	}

	public static Set<Vehiculo> cargarVehiculos(String fileName) throws FileNotFoundException, IOException {
		Set<Vehiculo> vehiculos = new HashSet<>();
		try (BufferedReader bf = new BufferedReader(new FileReader(fileName))) {
			String linea;
			while ((linea = bf.readLine()) != null) {
				if (linea.isBlank() || linea.trim().startsWith("#")) {
					continue; // ignora lineas vacias y el encabezado
				}
				String[] partes = linea.split(";");

				String patente = partes[0].trim();
				String modelo = partes[1].trim();
				int capacidadPasajeros = Integer.parseInt(partes[2].trim());
				TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(partes[3].trim().toUpperCase());
				CategoriaVehiculo categoriaVehiculo = CategoriaVehiculo.valueOf(partes[4].trim().toUpperCase());
				TipoServicio tipoServicio1 = TipoServicio.valueOf(partes[5].trim().toUpperCase());

				Vehiculo vehiculo = new Vehiculo(patente, modelo, capacidadPasajeros, tipoVehiculo, categoriaVehiculo,
						tipoServicio1);

				if (partes.length >= 7 && !partes[6].isBlank()) {
					TipoServicio tipoServicio2 = TipoServicio.valueOf(partes[6].trim().toUpperCase());
					vehiculo.agregarTipoServicio(tipoServicio2);
				}

				boolean agregado = vehiculos.add(vehiculo);
				if (!agregado) {
					System.out.println("Patente duplicada, se ignoro: " + patente);
				}
			}
		}
		return vehiculos;
	}

	public static Set<Usuario> cargarUsuarios(String fileName, Set<Vehiculo> vehiculos)
			throws FileNotFoundException, IOException {

		Set<Usuario> usuarios = new HashSet<>();

		try (BufferedReader bf = new BufferedReader(new FileReader(fileName))) {

			String linea;

			while ((linea = bf.readLine()) != null) {

				if (linea.isBlank() || linea.trim().startsWith("#")) {
					continue;
				}

				String[] partes = linea.split(";");

				String nombre = partes[0].trim();
				String telefono = partes[1].trim();
				String email = partes[2].trim();

				Usuario usuario = new Usuario(nombre, telefono, email);

				// Si tiene licencia, también es conductor
				if (partes.length >= 5 && !partes[3].isBlank()) {

					String licencia = partes[3].trim();

					// El primer vehículo es obligatorio para dar de alta al conductor
					Vehiculo primerVehiculo = buscarVehiculo(vehiculos, partes[4].trim());

					if (primerVehiculo == null) {
						throw new IllegalArgumentException("No existe el vehículo con patente: " + partes[4].trim());
					}

					usuario.altaConductor(licencia, primerVehiculo);

					// Resto de los vehículos del conductor
					for (int i = 5; i < partes.length; i++) {

						if (!partes[i].isBlank()) {

							Vehiculo vehiculo = buscarVehiculo(vehiculos, partes[i].trim());

							if (vehiculo == null) {
								throw new IllegalArgumentException(
										"No existe el vehículo con patente: " + partes[i].trim());
							}

							usuario.getConductor().agregarVehiculo(vehiculo);
						}
					}
				}

				if (!usuarios.add(usuario)) {
					System.out.println("Usuario duplicado, se ignoro: " + email);
				}
			}
		}

		return usuarios;
	}

	public static Vehiculo buscarVehiculo(Set<Vehiculo> vehiculos, String patente) {

		for (Vehiculo vehiculo : vehiculos) {
			if (vehiculo.getPatente().equalsIgnoreCase(patente)) {
				return vehiculo;
			}
		}

		return null;
	}
}
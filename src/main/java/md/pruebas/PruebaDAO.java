package md.pruebas;

import java.util.List;

import md.datos.dao.DAOFactory;
import md.datos.dao.DAOLoader;
import md.datos.dto.ServicioDTO;
import md.datos.dto.UsuarioDTO;
import md.datos.dto.VehiculoDTO;

public class PruebaDAO {

	public static void main(String[] args) {

		try {
			DAOFactory fabrica = DAOLoader.getFactory();

			List<ServicioDTO> servicios = fabrica.getServicioDAO().obtenerTodos();

			List<VehiculoDTO> vehiculos = fabrica.getVehiculoDAO().obtenerTodos();

			List<UsuarioDTO> usuarios = fabrica.getUsuarioDAO().obtenerTodos();

			System.out.println("===== DATOS LEÍDOS POR LOS DAO =====");

			System.out.println("Servicios: " + servicios.size());
			System.out.println("Vehículos: " + vehiculos.size());
			System.out.println("Usuarios: " + usuarios.size());

			System.out.println("\n===== SERVICIOS =====");

			for (ServicioDTO s : servicios) {
				System.out.println(s.getNombre() + " | " + s.getTarifaBase() + " | " + s.getPrecioKm() + " | "
						+ s.getPrecioMinuto() + " | " + s.getTipoVehiculo() + " | " + s.getCategoriaVehiculo() + " | "
						+ s.getTipoServicio());
			}

			System.out.println("\n===== VEHÍCULOS =====");

			for (VehiculoDTO v : vehiculos) {
				System.out.println(v.getPatente() + " | " + v.getModelo() + " | " + v.getTipoVehiculo() + " | "
						+ v.getCategoriaVehiculo() + " | " + v.getTiposServicio());
			}

			System.out.println("\n===== USUARIOS =====");

			for (UsuarioDTO u : usuarios) {
				System.out.println(u.getNombre() + " | " + u.getEmail() + " | "
						+ (u.getLicenciaConducir().isBlank() ? "Cliente" : "Conductor") + " | "
						+ u.getPatentesVehiculos());
			}

		} catch (Exception e) {
			System.out.println("Error al probar los DAO: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
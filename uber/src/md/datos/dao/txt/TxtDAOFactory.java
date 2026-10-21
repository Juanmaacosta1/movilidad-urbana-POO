package md.datos.dao.txt;

import md.datos.dao.DAOFactory;
import md.datos.dao.ServicioDAO;
import md.datos.dao.UsuarioDAO;
import md.datos.dao.VehiculoDAO;

/** Fábrica de DAO que leen archivos de texto. Las rutas entran desde afuera. */
public class TxtDAOFactory implements DAOFactory {

	private final String rutaServicios;
	private final String rutaVehiculos;
	private final String rutaUsuarios;

	public TxtDAOFactory(String rutaServicios, String rutaVehiculos, String rutaUsuarios) {
		this.rutaServicios = rutaServicios;
		this.rutaVehiculos = rutaVehiculos;
		this.rutaUsuarios = rutaUsuarios;
	}

	@Override
	public ServicioDAO getServicioDAO() {
		return new ServicioDaoTxt(rutaServicios);
	}

	@Override
	public VehiculoDAO getVehiculoDAO() {
		return new VehiculoDaoTxt(rutaVehiculos);
	}

	@Override
	public UsuarioDAO getUsuarioDAO() {
		return new UsuarioDaoTxt(rutaUsuarios);
	}
}
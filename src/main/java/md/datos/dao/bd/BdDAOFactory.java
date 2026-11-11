package md.datos.dao.bd;

import md.datos.dao.ConductorVehiculoDAO;
import md.datos.dao.DAOFactory;
import md.datos.dao.ServicioDAO;
import md.datos.dao.UsuarioDAO;
import md.datos.dao.VehiculoDAO;

public class BdDAOFactory implements DAOFactory {

	private final ConexionBD conexion;

	public BdDAOFactory(ConexionBD conexion) {
		this.conexion = conexion;
	}

	@Override
	public ServicioDAO getServicioDAO() {
		return new ServicioDaoBd(conexion);
	}

	@Override
	public VehiculoDAO getVehiculoDAO() {
		return new VehiculoDaoBd(conexion);
	}

	@Override
	public UsuarioDAO getUsuarioDAO() {
		return new UsuarioDaoBd(conexion);
	}

	@Override
	public ConductorVehiculoDAO getConductorVehiculoDAO() {
		return new ConductorVehiculoDaoBd(conexion);
	}
}
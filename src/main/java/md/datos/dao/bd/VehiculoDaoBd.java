package md.datos.dao.bd;

import java.util.List;

import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;

public class VehiculoDaoBd implements VehiculoDAO {

	private final ConexionBD conexion;

	public VehiculoDaoBd(ConexionBD conexion) {
		super();
		this.conexion = conexion;
	}

	@Override
	public List<VehiculoDTO> obtenerTodos() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}

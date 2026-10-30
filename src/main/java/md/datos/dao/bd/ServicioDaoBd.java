package md.datos.dao.bd;

import java.util.List;

import md.datos.dao.ServicioDAO;
import md.datos.dto.ServicioDTO;

public class ServicioDaoBd implements ServicioDAO {

	private final ConexionBD conexion;
	
	
	public ServicioDaoBd(ConexionBD conexion) {
		super();
		this.conexion = conexion;
	}


	@Override
	public List<ServicioDTO> obtenerTodos() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}

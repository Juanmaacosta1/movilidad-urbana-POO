package md.datos.dao;

import java.util.List;

import md.datos.dto.ConductorVehiculoDTO;

public interface ConductorVehiculoDAO {
	
	List<ConductorVehiculoDTO> obtenerTodos() throws Exception;

	List<ConductorVehiculoDTO> obtenerPorEmail(String email) throws Exception;
}

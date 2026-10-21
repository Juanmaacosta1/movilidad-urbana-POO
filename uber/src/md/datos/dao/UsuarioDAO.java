package md.datos.dao;

import java.util.List;

import md.datos.dto.UsuarioDTO;

public interface UsuarioDAO {
	List<UsuarioDTO> obtenerTodos() throws Exception;
}
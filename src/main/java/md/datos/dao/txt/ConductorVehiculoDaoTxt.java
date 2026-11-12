package md.datos.dao.txt;

import java.util.ArrayList;
import java.util.List;

import md.datos.dao.ConductorVehiculoDAO;
import md.datos.dto.ConductorVehiculoDTO;
import md.datos.dto.UsuarioDTO;

public class ConductorVehiculoDaoTxt implements ConductorVehiculoDAO {

	private final String rutaUsuarios;

	public ConductorVehiculoDaoTxt(String rutaUsuarios) {
		this.rutaUsuarios = rutaUsuarios;
	}

	@Override
	public List<ConductorVehiculoDTO> obtenerTodos() throws Exception {
		List<ConductorVehiculoDTO> lista = new ArrayList<>();
		for (UsuarioDTO u : new UsuarioDaoTxt(rutaUsuarios).obtenerTodos()) {
			for (String patente : u.getPatentesVehiculos()) {
				lista.add(new ConductorVehiculoDTO(u.getEmail(), patente));
			}
		}
		return lista;
	}

	@Override
	public List<ConductorVehiculoDTO> obtenerPorEmail(String email) throws Exception {
		List<ConductorVehiculoDTO> lista = new ArrayList<>();
		for (ConductorVehiculoDTO dto : obtenerTodos()) {
			if (dto.getEmail().equalsIgnoreCase(email)) {
				lista.add(dto);
			}
		}
		return lista;
	}
}
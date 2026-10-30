package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.ConductorVehiculoDTO;
import md.datos.dto.UsuarioDTO;

public class UsuarioDaoBd implements UsuarioDAO {

	private final ConexionBD conexion;

	public UsuarioDaoBd(ConexionBD conexion) {
		super();
		this.conexion = conexion;
	}

	@Override
	public List<UsuarioDTO> obtenerTodos() throws Exception {

		// 1. Patentes de cada conductor, agrupadas por email
		Map<String, List<String>> patentesPorEmail = new HashMap<>();
		for (ConductorVehiculoDTO cv : new ConductorVehiculoDaoBd(conexion).obtenerTodos()) {
			patentesPorEmail.computeIfAbsent(cv.getEmail(), k -> new ArrayList<>()).add(cv.getPatente());
		}

		// 2. Usuarios
		List<UsuarioDTO> lista = new ArrayList<>();
		String sql = "SELECT email, nombre, telefono, licencia_conducir FROM usuario";
		try (Connection c = conexion.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String email = rs.getString("email");
				List<String> patentes = patentesPorEmail.getOrDefault(email, new ArrayList<>());
				lista.add(new UsuarioDTO(rs.getString("nombre"), rs.getString("telefono"), email,
						rs.getString("licencia_conducir"), patentes));
			}
		}
		return lista;
	}
}
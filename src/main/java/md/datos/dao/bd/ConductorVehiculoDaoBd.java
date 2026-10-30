package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import md.datos.dao.ConductorVehiculoDAO;
import md.datos.dto.ConductorVehiculoDTO;

public class ConductorVehiculoDaoBd implements ConductorVehiculoDAO {

	private final ConexionBD conexion;

	public ConductorVehiculoDaoBd(ConexionBD conexion) {
		this.conexion = conexion;
	}

	@Override
	public List<ConductorVehiculoDTO> obtenerTodos() throws Exception {
		String sql = "SELECT email, patente FROM conductor_vehiculo ORDER BY email";
		List<ConductorVehiculoDTO> lista = new ArrayList<>();
		try (Connection c = conexion.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(new ConductorVehiculoDTO(rs.getString("email"), rs.getString("patente")));
			}
		}
		return lista;
	}

	@Override
	public List<ConductorVehiculoDTO> obtenerPorEmail(String email) throws Exception {
		String sql = "SELECT email, patente FROM conductor_vehiculo WHERE email = ?";
		List<ConductorVehiculoDTO> lista = new ArrayList<>();
		try (Connection c = conexion.obtenerConexion(); PreparedStatement ps = c.prepareStatement(sql)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					lista.add(new ConductorVehiculoDTO(rs.getString("email"), rs.getString("patente")));
				}
			}
		}
		return lista;
	}
}
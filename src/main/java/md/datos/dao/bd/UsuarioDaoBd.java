package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;

public class UsuarioDaoBd implements UsuarioDAO {

	private final ConexionBD conexion;

	public UsuarioDaoBd(ConexionBD conexion) {
		super();
		this.conexion = conexion;
	}

	@Override
	public List<UsuarioDTO> obtenerTodos() throws Exception {
		// TODO Auto-generated method stub
		List<UsuarioDTO> lista = new ArrayList<>();
		String sql = "SELECT email, nombre, telefono, licencia_conducir FROM usuario";
		try (Connection c = conexion.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(new UsuarioDTO(rs.getString("email"), rs.getString("nombre"), rs.getString("telefono"),
						rs.getString("licencia_conducir")));
			}

		}

		return lista;
	}
}

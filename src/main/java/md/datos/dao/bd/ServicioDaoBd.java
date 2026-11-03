package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
		List<ServicioDTO> lista = new ArrayList<>();
		String sql = "SELECT nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, categoria_vehiculo, tipo_servicio FROM servicio ORDER BY nombre";
		try (Connection c = conexion.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				lista.add(new ServicioDTO(rs.getString("nombre"), rs.getInt("tarifa_base"), rs.getInt("precio_km"),
						rs.getDouble("precio_minuto"), rs.getString("tipo_vehiculo"),
						rs.getString("categoria_vehiculo"), rs.getString("tipo_servicio")));

			}
		}
		return lista;
	}
}

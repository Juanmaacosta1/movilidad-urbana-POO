package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
		List<VehiculoDTO> lista = new ArrayList<>();
		String sql = "SELECT patente, modelo, capacidad_pasajero, tipo_vehiculo, categoria_vehiculo, tipo_servicios FROM vehiculo ORDER BY patente";
		try (Connection c = conexion.obtenerConexion();
				PreparedStatement ps = c.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
			
			}

		}

		return lista;

	}

}

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
        this.conexion = conexion;
    }

    @Override
    public List<ServicioDTO> obtenerTodos() throws Exception {
        String sql = "SELECT nombre, tarifa_base, precio_km, precio_minuto, tipo_vehiculo, categoria_vehiculo, tipo_servicio FROM servicio";
        List<ServicioDTO> lista = new ArrayList<>();
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ServicioDTO(
                        rs.getString("nombre"),
                        rs.getDouble("tarifa_base"),
                        rs.getDouble("precio_km"),
                        rs.getDouble("precio_minuto"),
                        String.valueOf(rs.getInt("tipo_vehiculo")),
                        String.valueOf(rs.getInt("categoria_vehiculo")),
                        String.valueOf(rs.getInt("tipo_servicio"))
                ));
            }
        }
        return lista;
    }
}
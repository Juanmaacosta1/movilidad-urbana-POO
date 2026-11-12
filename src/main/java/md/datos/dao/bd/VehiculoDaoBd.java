package md.datos.dao.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import md.datos.dao.VehiculoDAO;
import md.modelo.TipoVehiculo;
import md.modelo.CategoriaVehiculo;
import md.datos.dto.VehiculoDTO;

public class VehiculoDaoBd implements VehiculoDAO {

    private final ConexionBD conexion;

    public VehiculoDaoBd(ConexionBD conexion) {
        super();
        this.conexion = conexion;
    }

    @Override
    public List<VehiculoDTO> obtenerTodos() throws Exception {
        String sql = "SELECT patente, modelo, capacidad_pasajeros, tipo_vehiculo, categoria_vehiculo, tipo_servicios FROM vehiculo";
        List<VehiculoDTO> lista = new ArrayList<>();
        try (Connection c = conexion.obtenerConexion();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                List<String> tipos = new ArrayList<>();
                int mascara = rs.getInt("tipo_servicios");
                if ((mascara & 1) == 1) {
                    tipos.add("PASAJEROS");
                }
                if ((mascara & 2) == 2) {
                    tipos.add("ENVIOS");
                }
                lista.add(new VehiculoDTO(
                        rs.getString("patente"),
                        rs.getString("modelo"),
                        rs.getInt("capacidad_pasajeros"),
                        EnumBd.nombre(TipoVehiculo.class, rs.getInt("tipo_vehiculo")),
                        EnumBd.nombre(CategoriaVehiculo.class, rs.getInt("categoria_vehiculo")),
                        tipos
                ));
            }
        }
        return lista;
    }
}
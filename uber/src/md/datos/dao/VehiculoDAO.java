package md.datos.dao;
import java.util.List;
import md.datos.dto.VehiculoDTO;
public interface VehiculoDAO {

	List<VehiculoDTO> obtenerTodos() throws Exception;
}

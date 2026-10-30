package md.datos.dao;
import java.util.List;
import md.datos.dto.ServicioDTO;

public interface ServicioDAO {

	List<ServicioDTO> obtenerTodos() throws Exception;
}

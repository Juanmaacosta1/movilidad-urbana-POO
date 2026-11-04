package md.datos.dao;
import java.util.List;
import md.datos.dto.ServicioDTO;

public interface ServicioDAO {

	List<ServicioDTO> obtenerTodos() throws Exception;

	default void guardarTodos(List<ServicioDTO> servicios) throws Exception {
		throw new UnsupportedOperationException("Este origen de datos no permite guardar servicios");
	}
}

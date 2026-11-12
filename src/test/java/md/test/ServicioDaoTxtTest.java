package md.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import md.datos.dao.txt.ServicioDaoTxt;
import md.datos.dto.ServicioDTO;

class ServicioDaoTxtTest {

	@TempDir
	Path directorioTemporal;

	@Test
	void guardaYVuelveACargarServicios() throws Exception {
		ServicioDaoTxt dao = new ServicioDaoTxt(directorioTemporal.resolve("servicios.txt").toString());
		List<ServicioDTO> servicios = List.of(
				new ServicioDTO("Viaje urbano", 1000, 120, 35, "AUTO", "ESTANDAR", "PASAJEROS"),
				new ServicioDTO("Mensajería", 500, 60, 20, "MOTO", "ESTANDAR", "ENVIOS"));

		dao.guardarTodos(servicios);

		List<ServicioDTO> cargados = dao.obtenerTodos();
		assertEquals(2, cargados.size());
		assertEquals("Viaje urbano", cargados.get(0).getNombre());
		assertEquals(1000, cargados.get(0).getTarifaBase());
		assertEquals("AUTO", cargados.get(0).getTipoVehiculo());
		assertEquals("Mensajería", cargados.get(1).getNombre());
		assertEquals("ENVIOS", cargados.get(1).getTipoServicio());
	}
}

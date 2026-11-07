package md.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import md.datos.dao.txt.UsuarioDaoTxt;
import md.datos.dto.UsuarioDTO;

class UsuarioDaoTxtTest {

	@TempDir
	Path directorioTemporal;

	@Test
	void actualizaUsuarioYConservaLosVehiculosDelConductor() throws Exception {
		Path archivo = directorioTemporal.resolve("usuarios.txt");
		Files.writeString(archivo,
				"Maria;111;maria@example.com;;\n"
				+ "Juan;222;juan@example.com;LIC-1;ABC123;DEF456;\n");
		UsuarioDaoTxt dao = new UsuarioDaoTxt(archivo.toString());

		dao.actualizar(new UsuarioDTO("Juan Actualizado", "333", "nuevo@example.com",
				"LIC-2", List.of("ABC123", "DEF456")), "juan@example.com");

		List<UsuarioDTO> usuarios = dao.obtenerTodos();
		assertEquals(2, usuarios.size());
		assertEquals("Maria", usuarios.get(0).getNombre());
		assertEquals("Juan Actualizado", usuarios.get(1).getNombre());
		assertEquals("nuevo@example.com", usuarios.get(1).getEmail());
		assertEquals("LIC-2", usuarios.get(1).getLicenciaConducir());
		assertEquals(List.of("ABC123", "DEF456"), usuarios.get(1).getPatentesVehiculos());
	}

	@Test
	void noPermiteCambiarEmailPorUnoYaRegistrado() throws Exception {
		Path archivo = directorioTemporal.resolve("usuarios.txt");
		Files.writeString(archivo,
				"Maria;111;maria@example.com;;\n"
				+ "Juan;222;juan@example.com;LIC-1;ABC123;\n");
		UsuarioDaoTxt dao = new UsuarioDaoTxt(archivo.toString());

		assertThrows(IllegalArgumentException.class,
				() -> dao.actualizar(new UsuarioDTO("Juan", "222", "maria@example.com",
						"LIC-1", List.of("ABC123")), "juan@example.com"));
		assertEquals("juan@example.com", dao.obtenerTodos().get(1).getEmail());
	}

	@Test
	void agregaYEliminaUsuarios() throws Exception {
		Path archivo = directorioTemporal.resolve("usuarios.txt");
		Files.writeString(archivo, "Maria;111;maria@example.com;;\n");
		UsuarioDaoTxt dao = new UsuarioDaoTxt(archivo.toString());

		dao.agregar(new UsuarioDTO("Juan", "222", "juan@example.com", "LIC-1", List.of("ABC123")));
		assertEquals(2, dao.obtenerTodos().size());

		dao.eliminar("JUAN@example.com");
		assertEquals(List.of("maria@example.com"),
				dao.obtenerTodos().stream().map(UsuarioDTO::getEmail).toList());
	}

	@Test
	void noAgregaUnEmailDuplicado() throws Exception {
		Path archivo = directorioTemporal.resolve("usuarios.txt");
		Files.writeString(archivo, "Maria;111;maria@example.com;;\n");
		UsuarioDaoTxt dao = new UsuarioDaoTxt(archivo.toString());

		assertThrows(IllegalArgumentException.class,
				() -> dao.agregar(new UsuarioDTO("Otra Maria", "222", "MARIA@example.com", "", List.of())));
		assertEquals(1, dao.obtenerTodos().size());
	}
}

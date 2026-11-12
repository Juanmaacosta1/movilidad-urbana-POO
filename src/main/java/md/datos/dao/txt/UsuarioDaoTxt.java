package md.datos.dao.txt;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;

public class UsuarioDaoTxt implements UsuarioDAO {

	private final String rutaArchivo;

	public UsuarioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<UsuarioDTO> obtenerTodos() throws Exception {

		List<UsuarioDTO> lista = new ArrayList<>();

		try (Scanner read = new Scanner(new File(rutaArchivo))) {

			int numeroLinea = 0;

			while (read.hasNextLine()) {

				numeroLinea++;

				String lineaTexto = read.nextLine().trim();

				// Ignorar líneas vacías y encabezados
				if (lineaTexto.isEmpty() || lineaTexto.startsWith("#")) {
					continue;
				}

				String[] campos = lineaTexto.split(";", -1);

				// Todo usuario debe tener nombre, teléfono y email
				if (campos.length < 3) {
					throw new IllegalArgumentException("Formato inválido en usuarios.txt, línea " + numeroLinea);
				}

				String nombre = campos[0].trim();
				String telefono = campos[1].trim();
				String email = campos[2].trim();

				String licencia = "";
				List<String> patentesVehiculos = new ArrayList<>();

				// Si tiene licencia, también es conductor
				if (campos.length >= 4 && !campos[3].isBlank()) {

					licencia = campos[3].trim();

					// El primer vehículo es obligatorio para un conductor
					if (campos.length < 5 || campos[4].isBlank()) {
						throw new IllegalArgumentException(
								"Falta la patente del primer vehículo en la línea " + numeroLinea);
					}

					// Recorrer las patentes asociadas al conductor
					for (int i = 4; i < campos.length; i++) {

						String patente = campos[i].trim();

						if (!patente.isEmpty()) {
							patentesVehiculos.add(patente);
						}
					}
				}

				UsuarioDTO usuario = new UsuarioDTO(nombre, telefono, email, licencia, patentesVehiculos);

				lista.add(usuario);
			}
		}

		return lista;
	}

	@Override
	public void agregar(UsuarioDTO nuevoUsuario) throws Exception {
		if (nuevoUsuario == null) {
			throw new IllegalArgumentException("El usuario es obligatorio");
		}
		validarUsuario(nuevoUsuario);
		List<UsuarioDTO> usuarios = obtenerTodos();
		for (UsuarioDTO existente : usuarios) {
			if (existente.getEmail().equalsIgnoreCase(nuevoUsuario.getEmail())) {
				throw new IllegalArgumentException("Ya existe un usuario con el email ingresado");
			}
		}
		usuarios.add(nuevoUsuario);
		guardarTodos(usuarios);
	}

	@Override
	public void actualizar(UsuarioDTO usuarioActualizado, String emailAnterior) throws Exception {
		if (usuarioActualizado == null || emailAnterior == null || emailAnterior.isBlank()) {
			throw new IllegalArgumentException("El usuario y su email actual son obligatorios");
		}
		validarUsuario(usuarioActualizado);

		List<UsuarioDTO> usuarios = obtenerTodos();
		int indiceUsuario = -1;
		for (int i = 0; i < usuarios.size(); i++) {
			UsuarioDTO existente = usuarios.get(i);
			if (existente.getEmail().equalsIgnoreCase(emailAnterior)) {
				indiceUsuario = i;
			} else if (existente.getEmail().equalsIgnoreCase(usuarioActualizado.getEmail())) {
				throw new IllegalArgumentException("Ya existe un usuario con el email ingresado");
			}
		}
		if (indiceUsuario < 0) {
			throw new IllegalArgumentException("No se encontró el usuario que querés editar");
		}

		List<String> patentes = usuarioActualizado.getPatentesVehiculos();
		if (patentes == null) {
			throw new IllegalArgumentException("La lista de vehículos no puede ser nula");
		}
		for (String patente : patentes) {
			validarCampo(patente, "patente");
		}
		usuarios.set(indiceUsuario, usuarioActualizado);
		guardarTodos(usuarios);
	}

	@Override
	public void eliminar(String email) throws Exception {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("El email del usuario es obligatorio");
		}
		List<UsuarioDTO> usuarios = obtenerTodos();
		boolean eliminado = usuarios.removeIf(usuario -> usuario.getEmail().equalsIgnoreCase(email.trim()));
		if (!eliminado) {
			throw new IllegalArgumentException("No se encontró el usuario que querés eliminar");
		}
		guardarTodos(usuarios);
	}

	private void validarUsuario(UsuarioDTO usuario) {
		validarCampo(usuario.getNombre(), "nombre");
		validarCampo(usuario.getTelefono(), "teléfono");
		validarCampo(usuario.getEmail(), "email");
		String licencia = usuario.getLicenciaConducir();
		List<String> patentes = usuario.getPatentesVehiculos();
		if (licencia != null && !licencia.isBlank()) {
			validarCampo(licencia, "licencia");
			if (patentes == null || patentes.isEmpty()) {
				throw new IllegalArgumentException("Un conductor debe tener al menos un vehículo");
			}
		} else if (patentes != null && !patentes.isEmpty()) {
			throw new IllegalArgumentException("Un usuario que no es conductor no puede tener vehículos");
		}
		if (patentes != null) {
			for (String patente : patentes) {
				validarCampo(patente, "patente");
			}
		}
	}

	private void validarCampo(String valor, String nombreCampo) {
		if (valor == null || valor.isBlank() || valor.indexOf(';') >= 0
				|| valor.contains("\n") || valor.contains("\r")) {
			throw new IllegalArgumentException("El campo " + nombreCampo + " no es válido");
		}
	}

	private void guardarTodos(List<UsuarioDTO> usuarios) throws Exception {
		Path archivo = Path.of(rutaArchivo).toAbsolutePath();
		Path directorio = archivo.getParent();
		Path temporal = Files.createTempFile(directorio, "usuarios-", ".tmp");
		try {
			StringBuilder contenido = new StringBuilder(
					"#nombre;telefono;email;licenciaConducir;patentesVehiculos;\n");
			for (UsuarioDTO usuario : usuarios) {
				contenido.append(usuario.getNombre()).append(';')
						.append(usuario.getTelefono()).append(';')
						.append(usuario.getEmail()).append(';')
						.append(usuario.getLicenciaConducir() == null ? "" : usuario.getLicenciaConducir())
						.append(';');
				for (String patente : usuario.getPatentesVehiculos()) {
					contenido.append(patente).append(';');
				}
				contenido.append('\n');
			}
			Files.writeString(temporal, contenido, StandardCharsets.UTF_8);
			try {
				Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING,
						StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException e) {
				Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
			}
		} finally {
			Files.deleteIfExists(temporal);
		}
	}
}
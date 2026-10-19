package md.datos.dao.txt;

import java.io.File;
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
}
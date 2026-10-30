package md.datos.dao.txt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;

public class VehiculoDaoTxt implements VehiculoDAO {

	private final String rutaArchivo;

	public VehiculoDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<VehiculoDTO> obtenerTodos() throws Exception {

		List<VehiculoDTO> lista = new ArrayList<>();

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

				if (campos.length < 6) {
					throw new IllegalArgumentException("Formato inválido en vehiculos.txt, línea " + numeroLinea);
				}

				String patente = campos[0].trim();
				String modelo = campos[1].trim();
				int capacidadPasajeros = Integer.parseInt(campos[2].trim());

				String tipoVehiculo = campos[3].trim().toUpperCase();

				String categoriaVehiculo = campos[4].trim().toUpperCase();

				List<String> tiposServicio = new ArrayList<>();

				String tipoServicio1 = campos[5].trim().toUpperCase();

				if (!tipoServicio1.isEmpty()) {
					tiposServicio.add(tipoServicio1);
				}

				// El segundo tipo de servicio es opcional
				if (campos.length >= 7 && !campos[6].isBlank()) {
					String tipoServicio2 = campos[6].trim().toUpperCase();

					tiposServicio.add(tipoServicio2);
				}

				VehiculoDTO vehiculo = new VehiculoDTO(patente, modelo, capacidadPasajeros, tipoVehiculo,
						categoriaVehiculo, tiposServicio);

				lista.add(vehiculo);
			}
		}

		return lista;
	}
}
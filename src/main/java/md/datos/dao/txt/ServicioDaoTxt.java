package md.datos.dao.txt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import md.datos.dao.ServicioDAO;
import md.datos.dto.ServicioDTO;

public class ServicioDaoTxt implements ServicioDAO {

	private final String rutaArchivo;

	public ServicioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<ServicioDTO> obtenerTodos() throws Exception {

		List<ServicioDTO> lista = new ArrayList<>();

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

				if (campos.length < 7) {
					throw new IllegalArgumentException("Formato inválido en servicios.txt, línea " + numeroLinea);
				}

				String nombre = campos[0].trim();
				double tarifaBase = Double.parseDouble(campos[1].trim());
				double precioKm = Double.parseDouble(campos[2].trim());
				double precioMinuto = Double.parseDouble(campos[3].trim());

				String tipoVehiculo = campos[4].trim().toUpperCase();
				String categoriaVehiculo = campos[5].trim().toUpperCase();
				String tipoServicio = campos[6].trim().toUpperCase();

				ServicioDTO servicio = new ServicioDTO(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo,
						categoriaVehiculo, tipoServicio);

				lista.add(servicio);
			}
		}

		return lista;
	}
}
package md.datos.dao.txt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Lectura común a los DAO de texto: recorre el archivo línea por línea
 * (ignora vacías y comentarios con #), separa los campos por ";" y avisa en
 * qué archivo y línea hay un error.
 */
final class LectorTxt {

	/** Qué hacer con los campos de cada línea. */
	interface Procesador {
		void procesar(String[] campos);
	}

	private LectorTxt() {
	}

	static void leer(String ruta, int minCampos, int maxCampos, Procesador procesador) throws IOException {
		// UTF-8 para que las tildes se lean igual en cualquier sistema
		try (BufferedReader lector = new BufferedReader(new FileReader(ruta, StandardCharsets.UTF_8))) {
			String linea;
			int numero = 0;
			while ((linea = lector.readLine()) != null) {
				numero++;
				if (numero == 1 && linea.startsWith("\uFEFF")) {
					linea = linea.substring(1); // BOM que agregan algunos editores
				}
				linea = linea.trim();
				if (linea.isEmpty() || linea.startsWith("#")) {
					continue;
				}
				String[] campos = recortar(linea.split(";"));
				try {
					if (campos.length < minCampos || campos.length > maxCampos) {
						throw new IllegalArgumentException("cantidad de campos incorrecta (" + campos.length + ")");
					}
					procesador.procesar(campos);
				} catch (IllegalArgumentException e) { // incluye NumberFormatException
					throw new IllegalArgumentException(
							new File(ruta).getName() + ", línea " + numero + ": " + e.getMessage(), e);
				}
			}
		}
	}

	/** Quita espacios de cada campo y descarta los vacíos del final (por el ";" final). */
	private static String[] recortar(String[] partes) {
		int n = partes.length;
		while (n > 0 && partes[n - 1].trim().isEmpty()) {
			n--;
		}
		String[] campos = new String[n];
		for (int i = 0; i < n; i++) {
			campos[i] = partes[i].trim();
		}
		return campos;
	}
}
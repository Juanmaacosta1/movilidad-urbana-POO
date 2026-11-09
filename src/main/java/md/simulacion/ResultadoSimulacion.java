package md.simulacion;

import java.util.List;

import md.modelo.EstadoViaje;

/**
 * Qué pasó al simular uno o más pasos del viaje.
 *
 * @param estadoFinal estado en el que quedó el viaje
 * @param pasos       descripción legible de cada cosa que ocurrió (no incluye las calificaciones,
 *                    que ninguna de las partes debe conocer)
 */
public record ResultadoSimulacion(EstadoViaje estadoFinal, List<String> pasos) {

	public ResultadoSimulacion {
		pasos = List.copyOf(pasos);
	}
}

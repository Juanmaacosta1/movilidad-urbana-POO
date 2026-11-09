package md.rutas.api;

import java.util.List;

/**
 * Resultado neutral del cálculo de un tramo vial: no expone tipos de ninguna biblioteca de rutas.
 *
 * @param distanciaMetros      distancia por calles
 * @param duracionMilisegundos duración estimada según el perfil vial (sin tránsito en vivo)
 * @param geometria            puntos ordenados del recorrido (al menos dos)
 */
public record RutaCalculada(double distanciaMetros, long duracionMilisegundos, List<PuntoRuta> geometria) {

	public RutaCalculada {
		if (!Double.isFinite(distanciaMetros) || distanciaMetros < 0) {
			throw new IllegalArgumentException("La distancia no puede ser negativa: " + distanciaMetros);
		}
		if (duracionMilisegundos < 0) {
			throw new IllegalArgumentException("La duración no puede ser negativa: " + duracionMilisegundos);
		}
		if (geometria == null || geometria.size() < 2) {
			throw new IllegalArgumentException("La ruta necesita al menos dos puntos");
		}
		geometria = List.copyOf(geometria); // evita que alguien modifique la lista recibida
	}

	public double distanciaKm() {
		return distanciaMetros / 1000.0;
	}

	public double duracionMinutos() {
		return duracionMilisegundos / 60000.0;
	}
}

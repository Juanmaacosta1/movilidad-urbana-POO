package md.rutas.api;

import java.util.Objects;

/** Los dos tramos de un viaje, con sus métricas separadas. */
public record RutasViaje(RutaCalculada acercamiento, RutaCalculada traslado) {

	public RutasViaje {
		Objects.requireNonNull(acercamiento, "El tramo de acercamiento es obligatorio");
		Objects.requireNonNull(traslado, "El tramo de traslado es obligatorio");
	}
}

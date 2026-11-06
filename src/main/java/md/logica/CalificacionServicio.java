package md.logica;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import md.modelo.CalificacionViaje;

public class CalificacionServicio {

	private final List<CalificacionViaje> calificaciones = new ArrayList<>();

	public void registrarCalificacion(CalificacionViaje calificacion) {
		if (calificacion == null) {
			throw new IllegalArgumentException("La calificación no puede ser nula");
		}
		if (calificacion == CalificacionViaje.NO_CALIFICADO) {
			throw new IllegalArgumentException("No se puede registrar una calificación sin puntaje");
		}
		calificaciones.add(calificacion);
	}

	public double obtenerPromedio() {
		if (calificaciones.isEmpty()) {
			return 0.0;
		}

		int suma = 0;
		for (CalificacionViaje calificacion : calificaciones) {
			suma += calificacion.getValor();
		}
		return (double) suma / calificaciones.size();
	}

	public int cantidadCalificaciones() {
		return calificaciones.size();
	}

	public boolean tieneCalificaciones() {
		return !calificaciones.isEmpty();
	}

	public List<CalificacionViaje> getCalificaciones() {
		return Collections.unmodifiableList(new ArrayList<>(calificaciones));
	}
}
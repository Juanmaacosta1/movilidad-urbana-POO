package modelo;

import java.util.ArrayList;
import java.util.List;

public class Cliente {

	private List<Viaje> viajes = new ArrayList<>();

	public boolean enViaje() {
		for (Viaje viaje : viajes) {
			EstadoViaje estado = viaje.estadoActual();
			if (estado != EstadoViaje.FINALIZADO && estado != EstadoViaje.CANCELADO
					&& estado != EstadoViaje.RECHAZADO) {
				return true;
			}
		}
		return false;
	}

	public boolean agregarViaje(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		} else if (viajes.contains(viaje)) {
			return false;
		} else {
			viajes.add(viaje);
			return true;
		}
	}

	public List<Viaje> getViajes() {
		return viajes;
	}

}
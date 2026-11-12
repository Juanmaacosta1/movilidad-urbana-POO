package md.logica;

import java.util.Objects;

import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.rutas.api.PlanificadorRutas;
import md.rutas.api.RutaCalculada;
import md.rutas.api.RutasViaje;

/**
 * Servicio de aplicación: compone las dos consultas viales de un viaje y devuelve {@link RutasViaje}.
 * No dibuja ni cotiza. Depende del contrato, no de GraphHopper.
 */
public class PlanificacionViajeService {

	private final PlanificadorRutas planificador;

	public PlanificacionViajeService(PlanificadorRutas planificador) {
		this.planificador = Objects.requireNonNull(planificador, "El planificador de rutas es obligatorio");
	}

	/**
	 * @param posicionConductor dónde está el conductor al tomar el pedido
	 * @return acercamiento (conductor -> origen) y traslado (origen -> destino)
	 */
	public RutasViaje planificar(Ubicacion posicionConductor, Ubicacion origen, Ubicacion destino,
			TipoVehiculo tipoVehiculo) {
		Objects.requireNonNull(posicionConductor, "La posición del conductor es obligatoria");
		Objects.requireNonNull(origen, "El origen es obligatorio");
		Objects.requireNonNull(destino, "El destino es obligatorio");
		Objects.requireNonNull(tipoVehiculo, "El tipo de vehículo es obligatorio");

		RutaCalculada acercamiento = planificador.calcular(posicionConductor, origen, tipoVehiculo);
		RutaCalculada traslado = planificador.calcular(origen, destino, tipoVehiculo);
		return new RutasViaje(acercamiento, traslado);
	}
}

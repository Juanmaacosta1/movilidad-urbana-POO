package md.logica;

import java.util.ArrayList;
import java.util.List;

import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class PlanificacionViajeService {

	private static PlanificacionViajeService instancia;
	private final Logica logica;

	private PlanificacionViajeService() {
		this.logica = Logica.getInstance();
	}

	public static PlanificacionViajeService getInstance() {
		if (instancia == null) {
			instancia = new PlanificacionViajeService();
		}
		return instancia;
	}

	public double calcularDistancia(Ubicacion origen, Ubicacion destino) {
		if (origen == null) {
			throw new IllegalArgumentException("La ubicación de origen no puede ser nula");
		}
		if (destino == null) {
			throw new IllegalArgumentException("La ubicación de destino no puede ser nula");
		}
		return origen.calcularDistancia(destino);
	}

	public double calcularTiempoEstimado(Ubicacion origen, Ubicacion destino, double velocidadPromedioKmHora) {
		if (velocidadPromedioKmHora <= 0) {
			throw new IllegalArgumentException("La velocidad promedio debe ser mayor a cero");
		}
		double distancia = calcularDistancia(origen, destino);
		return distancia / velocidadPromedioKmHora * 60.0;
	}

	public double calcularCostoEstimado(Servicio servicio, Ubicacion origen, Ubicacion destino) {
		if (servicio == null) {
			throw new IllegalArgumentException("El servicio no puede ser nulo");
		}
		double distancia = calcularDistancia(origen, destino);
		double minutos = calcularTiempoEstimado(origen, destino, 30.0);
		return servicio.calcularCosto(distancia, minutos);
	}

	public List<Usuario> buscarConductoresDisponibles(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		return convertir(logica.buscarConductoresDisponibles(viaje));
	}

	public Viaje solicitarViaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		if (cliente == null) {
			throw new IllegalArgumentException("El cliente no puede ser nulo");
		}
		if (origen == null || destino == null) {
			throw new IllegalArgumentException("Origen y destino son obligatorios");
		}
		if (servicio == null) {
			throw new IllegalArgumentException("El servicio es obligatorio");
		}
		return logica.solicitarViaje(cliente, origen, destino, servicio);
	}

	public List<Usuario> filtrarConductoresPorVehiculo(List<Usuario> conductores, Vehiculo vehiculo) {
		if (conductores == null) {
			throw new IllegalArgumentException("La lista de conductores no puede ser nula");
		}
		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}
		List<Usuario> filtrados = new ArrayList<>();
		for (Usuario conductor : conductores) {
			if (conductor != null && conductor.esConductor() && conductor.getConductor().getVehiculos().contains(vehiculo)) {
				filtrados.add(conductor);
			}
		}
		return filtrados;
	}

	private static <T> List<T> convertir(Iterable<T> elementos) {
		List<T> resultado = new ArrayList<>();
		for (T elemento : elementos) {
			resultado.add(elemento);
		}
		return resultado;
	}
}
package modelo;

import java.util.List;
import java.util.ArrayList;

public class Conductor {

	private String licenciaConducir;
	private List<Vehiculo> vehiculos = new ArrayList<>();
	private Vehiculo vehiculoActivo;
	private CategoriaVehiculo categoriaVehiculoActivo;
	private EstadoConductor estadoConductor = EstadoConductor.FUERA_DE_SERVICIO;
	private List<Viaje> viajes = new ArrayList<>();

	public Conductor(String licenciaConducir, Vehiculo vehiculo) {
	    if (licenciaConducir == null || licenciaConducir.isBlank()) {
	        throw new IllegalArgumentException("La licencia de conducir es obligatoria");
	    }
	    if (vehiculo == null) {
	        throw new IllegalArgumentException("Debe registrar al menos un vehículo");
	    }
	    this.licenciaConducir = licenciaConducir;
	    this.vehiculos.add(vehiculo);
	    this.vehiculoActivo = vehiculo;
	    this.categoriaVehiculoActivo = vehiculo.getCategoriaVehiculo();
	}
	
	public void agregarVehiculo(Vehiculo vehiculo) {
		if (vehiculo == null)
			throw new IllegalArgumentException("El vehículo no puede ser nuloo");
		else if (!vehiculos.contains(vehiculo))
			vehiculos.add(vehiculo);
	}

	public void agregarViaje(Viaje nuevoViaje) {
		if (nuevoViaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		} else if (!this.viajes.contains(nuevoViaje)) {
			this.viajes.add(nuevoViaje);
		}

	}

	public String getLicenciaConducir() {
		return licenciaConducir;
	}

	public List<Vehiculo> getVehiculos() {
		return vehiculos;
	}

	public Vehiculo getVehiculoActivo() {
		return vehiculoActivo;
	}

	public CategoriaVehiculo getCategoriaVehiculoActivo() {
		return categoriaVehiculoActivo;
	}

	public EstadoConductor getEstadoConductor() {
		return estadoConductor;
	}

	public List<Viaje> getViajes() {
		return viajes;
	}

}
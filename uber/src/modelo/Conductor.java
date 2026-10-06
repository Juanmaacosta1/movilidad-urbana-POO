package modelo;

import java.util.List;
import java.util.ArrayList;

public class Conductor {

	private String licenciaConducir;
	private List<Vehiculo> vehiculos = new ArrayList<>();
	private Vehiculo vehiculoActivo;
	private EstadoConductor estadoConductor = EstadoConductor.FUERA_DE_SERVICIO;
	private List<Viaje> viajes = new ArrayList<>();
	private CategoriaVehiculo categoriaMaximaAceptada;

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
	}

	public void agregarVehiculo(Vehiculo vehiculo) {
		if (vehiculo == null)
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		else if (!vehiculos.contains(vehiculo))
			vehiculos.add(vehiculo);
	}

	// Permite elegir con que vehiculo (de los ya registrados) sale a trabajar.
	// Tiene que ser uno de los suyos, sino no tiene sentido activarlo.
	public void cambiarVehiculoActivo(Vehiculo vehiculo) {
		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}
		if (!vehiculos.contains(vehiculo)) {
			throw new IllegalArgumentException("El vehículo no esta registrado para este conductor");
		}
		this.vehiculoActivo = vehiculo;
	}

	public void cambiarEstado(EstadoConductor nuevoEstado) {
		if (nuevoEstado == null) {
			throw new IllegalArgumentException("El estado no puede ser nulo");
		}
		this.estadoConductor = nuevoEstado;
	}

	public void agregarViaje(Viaje nuevoViaje) {
		if (nuevoViaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		} else if (!this.viajes.contains(nuevoViaje)) {
			this.viajes.add(nuevoViaje);
			
		}

	}
	
	
	public void establecerCategoriaMaxima(CategoriaVehiculo categoria) {
	    if (categoria == null) {
	        throw new IllegalArgumentException("La categoría no puede ser nula");
	    }

	    this.categoriaMaximaAceptada = categoria;
	}

	public CategoriaVehiculo getCategoriaMaximaAceptada() {
	    return categoriaMaximaAceptada;
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
		return vehiculoActivo.getCategoriaVehiculo();
	}

	public EstadoConductor getEstadoConductor() {
		return estadoConductor;
	}

	public List<Viaje> getViajes() {
		return viajes;
	}

}
package md.modelo;

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
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
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

	/** Cambia el vehículo con el que trabaja; debe ser uno de los registrados. */
	public void setVehiculoActivo(Vehiculo vehiculo) {
		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}
		if (!vehiculos.contains(vehiculo)) {
			throw new IllegalArgumentException("El vehículo no pertenece al conductor");
		}
		this.vehiculoActivo = vehiculo;
	}

	/**
	 * Categoría más baja de viaje que acepta tomar (el límite superior es la
	 * categoría de su vehículo activo).
	 */
	public void setCategoriaVehiculoActivo(CategoriaVehiculo categoria) {
		if (categoria == null) {
			throw new IllegalArgumentException("La categoría no puede ser nula");
		}
		this.categoriaVehiculoActivo = categoria;
	}

	public void setEstadoConductor(EstadoConductor estado) {
		if (estado == null) {
			throw new IllegalArgumentException("El estado no puede ser nulo");
		}
		this.estadoConductor = estado;
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

	public void setLicenciaConducir(String licenciaConducir) {
		this.licenciaConducir = licenciaConducir;
	}

	public void setVehiculos(List<Vehiculo> vehiculos) {
		this.vehiculos = vehiculos;
	}

	public void setViajes(List<Viaje> viajes) {
		this.viajes = viajes;
	}

	
	
}
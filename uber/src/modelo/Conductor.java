package modelo;

import java.util.List;

public class Conductor {

	private String licenciaConducir;
	private List<Vehiculo> vehiculos;
	private Vehiculo vehiculoActivo;
	private CategoriaVehiculo categoriaVehiculoActivo;
	private EstadoConductor estadoConductor = EstadoConductor.FUERA_DE_SERVICIO;
	private List<Viaje> viaje;
	
}

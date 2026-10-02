package modelo;

public class Vehiculo {

	private String patente;
	private String modelo;
	private int capacidadPasajero;
	private CategoriaVehiculo categoriaVehiculo;
	private TipoVehiculo tipoVehiculo;
	public Vehiculo(String patente, String modelo, int capacidadPasajero, CategoriaVehiculo categoriaVehiculo,
			TipoVehiculo tipoVehiculo) {
		super();
		this.patente = patente;
		this.modelo = modelo;
		this.capacidadPasajero = capacidadPasajero;
		this.categoriaVehiculo = categoriaVehiculo;
		this.tipoVehiculo = tipoVehiculo;
	}
	
	
	
	
}

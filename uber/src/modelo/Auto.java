package modelo;

public class Auto extends Vehiculo {
	
	private int capacidadPasajeros;

	public Auto(int capacidadPasajeros,String patente, String modelo, boolean aceptaPasajero, boolean aceptaCarga, Ubicacion ubicacion,
			Servicio servicio) {
		super(patente, modelo, aceptaPasajero, aceptaCarga, ubicacion, servicio);
		
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}

	
}

package md.datos.dto;

import java.util.List;

public class VehiculoDTO {

	private String patente;
	private String modelo;
	private int capacidadPasajeros;
	private String tipoVehiculo;
	private String categoriaVehiculo;
	private List<String> tiposServicio;

	public VehiculoDTO(String patente, String modelo, int capacidadPasajeros, String tipoVehiculo,
			String categoriaVehiculo, List<String> tiposServicio) {

		this.patente = patente;
		this.modelo = modelo;
		this.capacidadPasajeros = capacidadPasajeros;
		this.tipoVehiculo = tipoVehiculo;
		this.categoriaVehiculo = categoriaVehiculo;
		this.tiposServicio = tiposServicio;
	}

	public String getPatente() {
		return patente;
	}

	public String getModelo() {
		return modelo;
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}

	public String getTipoVehiculo() {
		return tipoVehiculo;
	}

	public String getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

	public List<String> getTiposServicio() {
		return tiposServicio;
	}
}
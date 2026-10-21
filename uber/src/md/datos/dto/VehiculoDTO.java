package md.datos.dto;

import java.util.List;

import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class VehiculoDTO {

	private String patente;
	private String modelo;
	private int capacidadPasajeros;
	private TipoVehiculo tipoVehiculo;
	private CategoriaVehiculo categoriaVehiculo;
	private List<TipoServicio> tiposServicio;

	public VehiculoDTO(String patente, String modelo, int capacidadPasajeros, TipoVehiculo tipoVehiculo,
			CategoriaVehiculo categoriaVehiculo, List<TipoServicio> tiposServicio) {
		super();
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

	public void setPatente(String patente) {
		this.patente = patente;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public int getCapacidadPasajeros() {
		return capacidadPasajeros;
	}

	public void setCapacidadPasajeros(int capacidadPasajeros) {
		this.capacidadPasajeros = capacidadPasajeros;
	}

	public TipoVehiculo getTipoVehiculo() {
		return tipoVehiculo;
	}

	public void setTipoVehiculo(TipoVehiculo tipoVehiculo) {
		this.tipoVehiculo = tipoVehiculo;
	}

	public CategoriaVehiculo getCategoriaVehiculo() {
		return categoriaVehiculo;
	}

	public void setCategoriaVehiculo(CategoriaVehiculo categoriaVehiculo) {
		this.categoriaVehiculo = categoriaVehiculo;
	}

	public List<TipoServicio> getTiposServicio() {
		return tiposServicio;
	}

	public void setTiposServicio(List<TipoServicio> tiposServicio) {
		this.tiposServicio = tiposServicio;
	}

}

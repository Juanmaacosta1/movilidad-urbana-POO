package modelo;

import java.util.Objects;

public abstract class Vehiculo {

	private String patente;
	private String modelo;
	private boolean aceptaPasajero;
	private boolean aceptaCarga;
	private Ubicacion ubicacion;
	private Servicio servicio;

	public Vehiculo(String patente, String modelo, boolean aceptaPasajero, boolean aceptaCarga, Ubicacion ubicacion,
			Servicio servicio) {
		super();
		this.patente = patente;
		this.modelo = modelo;
		this.aceptaPasajero = aceptaPasajero;
		this.aceptaCarga = aceptaCarga;
		this.ubicacion = ubicacion;
		this.servicio = servicio;
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

	public boolean isAceptaPasajero() {
		return aceptaPasajero;
	}

	public void setAceptaPasajero(boolean aceptaPasajero) {
		this.aceptaPasajero = aceptaPasajero;
	}

	public boolean isAceptaCarga() {
		return aceptaCarga;
	}

	public void setAceptaCarga(boolean aceptaCarga) {
		this.aceptaCarga = aceptaCarga;
	}

	public Ubicacion getUbicacion() {
		return ubicacion;
	}

	public void setUbicacion(Ubicacion ubicacion) {
		this.ubicacion = ubicacion;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}

	@Override
	public int hashCode() {
		return Objects.hash(patente);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Vehiculo other = (Vehiculo) obj;
		return Objects.equals(patente, other.patente);
	}

	@Override
	public String toString() {
		return "Vehiculo [patente=" + patente + ", modelo=" + modelo + ", aceptaPasajero=" + aceptaPasajero
				+ ", aceptaCarga=" + aceptaCarga + ", ubicacion=" + ubicacion + ", servicio=" + servicio + "]";
	}
	
	

}

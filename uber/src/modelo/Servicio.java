package modelo;

import java.util.Objects;

public class Servicio {

	private String nombre;
	private double tarifaBase;
	private double precioPorKm;
	private double precioPorMinuto;
	TipoServicio tipoServicio;

	public Servicio(String nombre, double tarifaBase, double precioPorKm, double precioPorMinuto,
			TipoServicio tipoServicio) {
		super();
		this.nombre = nombre;
		this.tarifaBase = tarifaBase;
		this.precioPorKm = precioPorKm;
		this.precioPorMinuto = precioPorMinuto;
		this.tipoServicio = tipoServicio;
	}

	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getTarifaBase() {
		return tarifaBase;
	}

	public void setTarifaBase(double tarifaBase) {
		this.tarifaBase = tarifaBase;
	}

	public double getPrecioPorKm() {
		return precioPorKm;
	}

	public void setPrecioPorKm(double precioPorKm) {
		this.precioPorKm = precioPorKm;
	}

	public double getPrecioPorMinuto() {
		return precioPorMinuto;
	}

	public void setPrecioPorMinuto(double precioPorMinuto) {
		this.precioPorMinuto = precioPorMinuto;
	}

	public TipoServicio getTipoServicio() {
		return tipoServicio;
	}

	public void setTipoServicio(TipoServicio tipoServicio) {
		this.tipoServicio = tipoServicio;
	}

	@Override
	public int hashCode() {
		return Objects.hash(nombre, precioPorKm, precioPorMinuto, tarifaBase, tipoServicio);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Servicio other = (Servicio) obj;
		return Objects.equals(nombre, other.nombre)
				&& Double.doubleToLongBits(precioPorKm) == Double.doubleToLongBits(other.precioPorKm)
				&& Double.doubleToLongBits(precioPorMinuto) == Double.doubleToLongBits(other.precioPorMinuto)
				&& Double.doubleToLongBits(tarifaBase) == Double.doubleToLongBits(other.tarifaBase)
				&& tipoServicio == other.tipoServicio;
	}

	@Override
	public String toString() {
		return "Servicio [nombre=" + nombre + ", tarifaBase=" + tarifaBase + ", precioPorKm=" + precioPorKm
				+ ", precioPorMinuto=" + precioPorMinuto + ", tipoServicio=" + tipoServicio + "]";
	}

	
}

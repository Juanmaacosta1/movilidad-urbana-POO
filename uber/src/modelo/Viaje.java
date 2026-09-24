package modelo;

import java.time.LocalDate;
import java.util.Objects;

public class Viaje {

	private String id_viaje; //identificador de viaje AGREGADO
	private int tiempoEstimado;
	private double factorDemora;
	private double costoViaje;
	private LocalDate fechaHora;
	private Vehiculo vehiculo;
	private Conductor conductor;
	private Cliente cliente;
	private Estado estado;
	private Calificacion calificacionConductor;
	private Calificacion calificacionCliente;
	private Ubicacion origen;
	private Ubicacion destino;

	public Viaje(String id_viaje,Cliente cliente, Ubicacion origen, Ubicacion destino, int tiempoEstimado, double factorDemora) {
		this.id_viaje=id_viaje;
		this.cliente = cliente;
		this.origen = origen;
		this.destino = destino;
		this.tiempoEstimado = tiempoEstimado;
		this.factorDemora = factorDemora;
		this.fechaHora = LocalDate.now();
		this.estado = Estado.SOLICITADO;		
	}

	public int getTiempoEstimado() {
		return tiempoEstimado;
	}

	public void setTiempoEstimado(int tiempoEstimado) {
		this.tiempoEstimado = tiempoEstimado;
	}

	public double getFactorDemora() {
		return factorDemora;
	}

	public void setFactorDemora(double factorDemora) {
		this.factorDemora = factorDemora;
	}

	public double getCostoViaje() {
		return costoViaje;
	}

	public void setCostoViaje(double costoViaje) {
		this.costoViaje = costoViaje;
	}

	public LocalDate getFechaHora() {
		return fechaHora;
	}

	public void setFechaHora(LocalDate fechaHora) {
		this.fechaHora = fechaHora;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public Conductor getConductor() {
		return conductor;
	}

	public void setConductor(Conductor conductor) {
		this.conductor = conductor;
	}

	
	public String getId_viaje() {
		return id_viaje;
	}

	public void setId_viaje(String id_viaje) {
		this.id_viaje = id_viaje;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Estado getEstado() {
		return estado;
	}

	public void setEstado(Estado estado) {
		this.estado = estado;
	}

	public Calificacion getCalificacionConductor() {
		return calificacionConductor;
	}

	public void setCalificacionConductor(Calificacion calificacionConductor) {
		this.calificacionConductor = calificacionConductor;
	}

	public Calificacion getCalificacionCliente() {
		return calificacionCliente;
	}

	public void setCalificacionCliente(Calificacion calificacionCliente) {
		this.calificacionCliente = calificacionCliente;
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public void setOrigen(Ubicacion origen) {
		this.origen = origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public void setDestino(Ubicacion destino) {
		this.destino = destino;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id_viaje);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Viaje other = (Viaje) obj;
		return Objects.equals(id_viaje, other.id_viaje);
	}

	@Override
	public String toString() {
		return "Viaje [id_viaje=" + id_viaje + ", tiempoEstimado=" + tiempoEstimado + ", factorDemora=" + factorDemora
				+ ", costoViaje=" + costoViaje + ", fechaHora=" + fechaHora + ", vehiculo=" + vehiculo + ", conductor="
				+ conductor + ", cliente=" + cliente + ", estado=" + estado + ", calificacionConductor="
				+ calificacionConductor + ", calificacionCliente=" + calificacionCliente + ", origen=" + origen
				+ ", destino=" + destino + "]";
	}

	
	
}

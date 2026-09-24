package modelo;

import java.time.LocalDate;

public class Viaje {

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

	public Viaje(Cliente cliente, Ubicacion origen, Ubicacion destino, int tiempoEstimado, double factorDemora) {
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

	
}

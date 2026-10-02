package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Viaje {
	private UUID id = UUID.randomUUID();
	private Usuario cliente;
	private Ubicacion origen;
	private Ubicacion destino;
	private Servicio servicio;
	private List<RegistroViaje> registroViaje = new ArrayList<>();
	private Usuario Conductor;
	private Vehiculo vehiculo;
	private RolUsuario rolCancela;
	private String motivoDeCancelacion;
	private double costoCancelacion;

	private CalificacionViaje calificacionConductor = CalificacionViaje.NO_CALIFICADO;
	private CalificacionViaje calificacionCliente = CalificacionViaje.NO_CALIFICADO;

	public Viaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {
		super();
		this.cliente = cliente;
		this.origen = origen;
		this.destino = destino;
		this.servicio = servicio;
	}



	public void solicitar(LocalDateTime fechaHora) {
		if(!registroViaje.isEmpty())
			throw new  IllegalArgumentException("El viaje ya fue solicitado");
		
	}

	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {

	}

	public void iniciar(LocalDateTime fechaHora) {

	}
	/*
	 * @param calificacionConductor
	 * 
	 * @param calificacionCliente
	 */

	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {

	}

	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {

	}

	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo, double kmRecorridos) {

	}

	public void rechazar(LocalDateTime fechaHora, Usuario usuario, String motivo) {

	}

	public EstadoViaje estadoActual() {
		if (registroViaje.isEmpty())
			return null;
		return registroViaje.get(registroViaje.size() - 1).getEstadoViaje();
	}

	public UUID getId() {
		return id;
	}

	public Usuario getCliente() {
		return cliente;
	}

	public Ubicacion getOrigen() {
		return origen;
	}

	public Ubicacion getDestino() {
		return destino;
	}

	public Servicio getServicio() {
		return servicio;
	}

	public List<RegistroViaje> getRegistroViaje() {
		return registroViaje;
	}

	public Usuario getConductor() {
		return Conductor;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public RolUsuario getRolCancela() {
		return rolCancela;
	}

	public String getMotivoDeCancelacion() {
		return motivoDeCancelacion;
	}

	public double getCostoCancelacion() {
		return costoCancelacion;
	}

	public CalificacionViaje getCalificacionConductor() {
		return calificacionConductor;
	}

	public CalificacionViaje getCalificacionCliente() {
		return calificacionCliente;
	}
	
}

package modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Viaje {
	private static final double VELOCIDAD_PROMEDIO = 30.0; // km/h
	private UUID id = UUID.randomUUID();
	private Usuario cliente;
	private Ubicacion origen;
	private Ubicacion destino;
	private Servicio servicio;
	private List<RegistroViaje> registroViaje = new ArrayList<>();
	private Usuario Conductor;
	private Vehiculo vehiculo;
	private RolUsuario rolCancela;
	private String motivoCancelacion;
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
		if (!registroViaje.isEmpty())
			throw new IllegalArgumentException("El viaje ya fue solicitado");
		registrar(fechaHora, EstadoViaje.SOLICITADO);
	}

	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		if (conductor == null || !conductor.esConductor())
			throw new IllegalArgumentException("El conductor es obligatorio y debe estar registrado como tal");
		exigirEstado("aceptar", EstadoViaje.SOLICITADO);

		this.Conductor = conductor;
		this.vehiculo = conductor.getConductor().getVehiculoActivo();
		conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
		conductor.getConductor().agregarViaje(this);
		registrar(fechaHora, EstadoViaje.ACEPTADO);
	}

	public void iniciar(LocalDateTime fechaHora) {
		exigirEstado("iniciar", EstadoViaje.ACEPTADO);

		Conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
		registrar(fechaHora, EstadoViaje.INICIADO);
	}

	/**
	 * @param calificacionConductor calificacion que recibe el conductor
	 * @param calificacionCliente   calificacion que recibe el cliente
	 */
	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {
		exigirEstado("finalizar", EstadoViaje.INICIADO);
		if (calificacionConductor == null || calificacionConductor == CalificacionViaje.NO_CALIFICADO
				|| calificacionCliente == null || calificacionCliente == CalificacionViaje.NO_CALIFICADO) {
			throw new IllegalArgumentException("Hay que calificar al conductor y al cliente para finalizar el viaje");
		}

		this.calificacionConductor = calificacionConductor;
		this.calificacionCliente = calificacionCliente;
		Conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
		registrar(fechaHora, EstadoViaje.FINALIZADO);
	}

	/** Cancelacion antes de que empiece el viaje (sin costo). */
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {
		cancelarViaje(fechaHora, usuario, motivo, 0, EstadoViaje.SOLICITADO, EstadoViaje.ACEPTADO);
	}

	/** Cancelacion con el viaje ya aceptado o iniciado: se cobra lo recorrido. */
	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo, double kmRecorridos) {
		if (kmRecorridos < 0)
			throw new IllegalArgumentException("Los kilometros recorridos no pueden ser negativos");
		cancelarViaje(fechaHora, usuario, motivo, kmRecorridos, EstadoViaje.ACEPTADO, EstadoViaje.INICIADO);
	}

	public void rechazar(LocalDateTime fechaHora) {
		exigirEstado("rechazar", EstadoViaje.SOLICITADO);
		registrar(fechaHora, EstadoViaje.RECHAZADO);
	}

	private void cancelarViaje(LocalDateTime fechaHora, Usuario usuario, String motivo, double km,
			EstadoViaje... estadosPermitidos) {
		if (usuario == null)
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		if (motivo == null || motivo.isBlank())
			throw new IllegalArgumentException("El motivo de cancelacion es obligatorio");
		exigirEstado("cancelar", estadosPermitidos);

		if (usuario == cliente) {
			rolCancela = RolUsuario.CLIENTE;
		} else if (usuario == Conductor) {
			rolCancela = RolUsuario.CONDUCTOR;
		} else {
			throw new IllegalArgumentException("Solo el cliente o el conductor del viaje pueden cancelarlo");
		}

		motivoCancelacion = motivo;
		costoCancelacion = km > 0 ? servicio.calcularCosto(km, km / VELOCIDAD_PROMEDIO * 60) : 0;
		if (Conductor != null) {
			Conductor.getConductor().setEstadoConductor(EstadoConductor.DISPONIBLE);
		}
		registrar(fechaHora, EstadoViaje.CANCELADO);
	}

	private void registrar(LocalDateTime fechaHora, EstadoViaje estado) {
		registroViaje.add(new RegistroViaje(fechaHora, estado));
	}

	private void exigirEstado(String accion, EstadoViaje... permitidos) {
		EstadoViaje actual = estadoActual();
		for (EstadoViaje e : permitidos) {
			if (e == actual)
				return;
		}
		throw new IllegalStateException("No se puede " + accion + " un viaje en estado " + actual);
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

	public String getmotivoCancelacion() {
		return motivoCancelacion;
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

	public String getMotivoCancelacion() {
		return motivoCancelacion;
	}

	public void setMotivoCancelacion(String motivoCancelacion) {
		this.motivoCancelacion = motivoCancelacion;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setCliente(Usuario cliente) {
		this.cliente = cliente;
	}

	public void setOrigen(Ubicacion origen) {
		this.origen = origen;
	}

	public void setDestino(Ubicacion destino) {
		this.destino = destino;
	}

	public void setServicio(Servicio servicio) {
		this.servicio = servicio;
	}

	public void setRegistroViaje(List<RegistroViaje> registroViaje) {
		this.registroViaje = registroViaje;
	}

	public void setConductor(Usuario conductor) {
		Conductor = conductor;
	}

	public void setVehiculo(Vehiculo vehiculo) {
		this.vehiculo = vehiculo;
	}

	public void setRolCancela(RolUsuario rolCancela) {
		this.rolCancela = rolCancela;
	}

	public void setCostoCancelacion(double costoCancelacion) {
		this.costoCancelacion = costoCancelacion;
	}

	public void setCalificacionConductor(CalificacionViaje calificacionConductor) {
		this.calificacionConductor = calificacionConductor;
	}

	public void setCalificacionCliente(CalificacionViaje calificacionCliente) {
		this.calificacionCliente = calificacionCliente;
	}

}
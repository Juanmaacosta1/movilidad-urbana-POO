package modelo;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Viaje {
	private UUID id = UUID.randomUUID();
	private Usuario cliente;
	private Ubicacion origen;
	private Ubicacion destino;
	private Servicio servicio;
	private List<RegistroViaje> registroViaje = new ArrayList<>();
	private Usuario conductor;
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
		if (!registroViaje.isEmpty())
			throw new IllegalArgumentException("El viaje ya fue solicitado");
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.SOLICITADO));
		cliente.getCliente().agregarViaje(this);
	}

	public void aceptar(LocalDateTime fechaHora, Usuario conductor) {
		if (estadoActual() != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("Solo se puede aceptar un viaje SOLICITADO");
		}
		if (conductor == null || !conductor.esConductor()) {
			throw new IllegalArgumentException("El usuario debe estar registrado como conductor");
		}
		this.conductor = conductor;
		this.vehiculo = conductor.getConductor().getVehiculoActivo();
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.ACEPTADO));
		conductor.getConductor().agregarViaje(this);
		conductor.getConductor().cambiarEstado(EstadoConductor.VIAJE_A_ORIGEN);
	}

	public void iniciar(LocalDateTime fechaHora) {
		if (estadoActual() != EstadoViaje.ACEPTADO) {
			throw new IllegalStateException("Solo se puede iniciar un viaje ACEPTADO");
		}
		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.INICIADO));
		conductor.getConductor().cambiarEstado(EstadoConductor.VIAJE_A_DESTINO);
	}
	/*
	 * @param calificacionConductor
	 * 
	 * @param calificacionCliente
	 */

	public void finalizar(LocalDateTime fechaHora, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {
		if (estadoActual() != EstadoViaje.INICIADO) {
			throw new IllegalStateException("No se puede finalizar un viaje que no ha iniciado");
		}
		this.calificacionCliente = calificacionCliente;
		this.calificacionConductor = calificacionConductor;

		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.FINALIZADO));
		conductor.getConductor().cambiarEstado(EstadoConductor.DISPONIBLE);

	}

	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo) {

		if (estadoActual() == EstadoViaje.FINALIZADO || estadoActual() == EstadoViaje.CANCELADO
				|| estadoActual() == EstadoViaje.RECHAZADO) {

			throw new IllegalStateException("El viaje no puede ser cancelado");
		}

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario que cancela no puede ser null");
		}

		if (!usuario.equals(cliente) && !usuario.equals(conductor)) {
			throw new IllegalArgumentException("El usuario no participa del viaje");
		}

		this.rolCancela = usuario.equals(cliente) ? RolUsuario.CLIENTE : RolUsuario.CONDUCTOR;

		this.motivoDeCancelacion = motivo;
		this.costoCancelacion = 0;

		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.CANCELADO));

		if (conductor != null) {
			conductor.getConductor().cambiarEstado(EstadoConductor.DISPONIBLE);
		}
	}

	public void cancelar(LocalDateTime fechaHora, Usuario usuario, String motivo, double kmRecorridos) {

		if (estadoActual() == EstadoViaje.FINALIZADO || estadoActual() == EstadoViaje.CANCELADO
				|| estadoActual() == EstadoViaje.RECHAZADO) {

			throw new IllegalStateException("El viaje no puede ser cancelado");
		}

		if (usuario == null || !usuario.equals(cliente)) {
			throw new IllegalArgumentException("Solo el cliente puede cancelar de esta forma");
		}

		if (kmRecorridos < 0) {
			throw new IllegalArgumentException("Los kilómetros recorridos no pueden ser negativos");
		}

		this.rolCancela = RolUsuario.CLIENTE;
		this.motivoDeCancelacion = motivo;

		if (estadoActual() == EstadoViaje.SOLICITADO) {
			this.costoCancelacion = 0;
		} else {
			LocalDateTime fechaInicio = registroViaje.stream().filter(r -> r.getEstadoViaje() == EstadoViaje.ACEPTADO)
					.findFirst().get().getFechaHora();

			long minutos = ChronoUnit.MINUTES.between(fechaInicio, fechaHora);

			this.costoCancelacion = servicio.calcularCosto(kmRecorridos, minutos);
		}

		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.CANCELADO));

		if (conductor != null) {
			conductor.getConductor().cambiarEstado(EstadoConductor.DISPONIBLE);
		}
	}

	public void rechazar(LocalDateTime fechaHora, String motivo) {

		if (estadoActual() != EstadoViaje.SOLICITADO) {
			throw new IllegalStateException("Solo se puede rechazar un viaje SOLICITADO");
		}

		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
		}

		registroViaje.add(new RegistroViaje(fechaHora, EstadoViaje.RECHAZADO));
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
		return conductor;
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

	@Override
	public int hashCode() {
		return Objects.hash(id);
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
		return Objects.equals(id, other.id);
	}

}
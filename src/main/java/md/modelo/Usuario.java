package md.modelo;

/**
 * Usuario registrado. Siempre es cliente; opcionalmente también conductor
 * (composición 1 a Cliente y 0..1 a Conductor).
 */
public class Usuario {
	private final String nombre;
	private final String telefono;
	private final String email;
	private final Cliente cliente = new Cliente();
	private Conductor conductor;
	private RolUsuario rolActivo = RolUsuario.CLIENTE;

	public Usuario(String nombre, String telefono, String email) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre es obligatorio");
		}
		if (telefono == null || telefono.isBlank()) {
			throw new IllegalArgumentException("El teléfono es obligatorio");
		}
		if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
			throw new IllegalArgumentException("Email inválido: " + email);
		}
		this.nombre = nombre;
		this.telefono = telefono;
		this.email = email;
	}

	/**
	 * Habilita al usuario como conductor registrando licencia y primer vehículo.
	 */
	public void altaConductor(String licencia, Vehiculo vehiculo) {
		if (conductor != null) {
			throw new IllegalStateException("El usuario ya es conductor");
		}
		this.conductor = new Conductor(licencia, vehiculo);
	}

	public boolean esConductor() {
		return conductor != null;
	}

	public void cambiarRolActivo(RolUsuario rolNuevo) {
		if (rolNuevo == null) {
			throw new IllegalArgumentException("El rol no puede ser nulo");
		}
		if (rolNuevo == RolUsuario.CONDUCTOR && conductor == null) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}
		// Validar que el conductor esté FUERA_DE_SERVICIO para cambiar a CONDUCTOR
		if (rolNuevo == RolUsuario.CONDUCTOR && conductor.getEstadoConductor() != EstadoConductor.FUERA_DE_SERVICIO) {
			throw new IllegalStateException("El conductor debe estar FUERA_DE_SERVICIO para cambiar de rol");
		}
		if (cliente.enViaje() || (conductor != null && (conductor.getEstadoConductor() == EstadoConductor.VIAJE_A_ORIGEN
				|| conductor.getEstadoConductor() == EstadoConductor.VIAJE_A_DESTINO))) {
			throw new IllegalStateException("No se puede cambiar de rol durante un viaje");
		}
		// Cuando se cambia a CLIENTE, poner el conductor en FUERA_DE_SERVICIO automáticamente
		if (rolNuevo == RolUsuario.CLIENTE && conductor != null) {
			conductor.setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
		}
		this.rolActivo = rolNuevo;
	}

	public String getNombre() {
		return nombre;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getEmail() {
		return email;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public Conductor getConductor() {
		return conductor;
	}

	public RolUsuario getRolActivo() {
		return rolActivo;
	}

	public void setConductor(Conductor conductor) {
		this.conductor = conductor;
	}

	public void setRolActivo(RolUsuario rolActivo) {
		this.rolActivo = rolActivo;
	}

	@Override
	public String toString() {
		return nombre + " <" + email + ">";
	}
}
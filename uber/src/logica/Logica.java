package logica;
import java.time.LocalDateTime;
import java.util.Objects;

import modelo.*;
import datos.*;
import net.datastructures.*;

public class Logica {

	private static final double VELOCIDAD_PROMEDIO = 30.0;
	private static Logica instancia;
	private Map<String, Usuario> usuarios = new ChainHashMap<>();
	private List<Servicio> servicios = new ArrayList<>();
	private List<Viaje> viajes = new ArrayList<>();
	
	 private Logica() {
	    }
	 
	    public static Logica getInstance() {
	        if (instancia == null) {
	            instancia = new Logica();
	        }
	        return instancia;
	    }
	 
	    /** Borra todos los datos en memoria (útil para los tests). */
	    public void reiniciar() {
	        usuarios = new ChainHashMap<>();
	        servicios = new ArrayList<>();
	        viajes = new ArrayList<>();
	    }
	    
	    // ==================================================================
	    // GESTIÓN DE USUARIOS
	    // ==================================================================
	 
	    /** Registra un usuario nuevo (todo usuario es también cliente). */
	    public Usuario registrarUsuario(String nombre, String telefono, String email) {
	        validarTexto(nombre, "El nombre es obligatorio");
	        validarTexto(telefono, "El teléfono es obligatorio");
	        validarTexto(email, "El email es obligatorio");
	        String clave = email.trim().toLowerCase();
	        if (usuarios.get(clave) != null) {
	            throw new IllegalArgumentException("Ya existe un usuario con el email " + email);
	        }
	        Usuario usuario = new Usuario(nombre.trim(), telefono.trim(), email.trim());
	        usuarios.put(clave, usuario);
	        return usuario;
	    }
	 
	    /** @return el usuario con ese email, o {@code null} si no existe. */
	    public Usuario buscarUsuario(String email) {
	        return email == null ? null : usuarios.get(email.trim().toLowerCase());
	    }
	
	// ==================================================================
	// GESTIÓN DE CONDUCTORES
	// ==================================================================

	/**
	 * El conductor indica con qué vehículo trabaja y hasta qué categoría menor
	 * acepta viajes (el límite superior es la categoría de ese vehículo), y pasa
	 * de Fuera de Servicio a Disponible.
	 */
	public void ponerConductorEnServicio(Usuario usuario, Vehiculo vehiculo, CategoriaVehiculo categoriaMinima) {

		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}
		if (!usuario.esConductor()) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}
		if (vehiculo == null) {
			throw new IllegalArgumentException("El vehículo no puede ser nulo");
		}
		if (categoriaMinima == null) {
			throw new IllegalArgumentException("La categoría no puede ser nula");
		}

		Conductor conductor = usuario.getConductor();

		if (conductor.getEstadoConductor() != EstadoConductor.FUERA_DE_SERVICIO) {
			throw new IllegalStateException("El conductor debe estar Fuera de Servicio para ponerse en servicio");
		}
		if (!contiene(conductor.getVehiculos(), vehiculo)) {
			throw new IllegalArgumentException("El vehículo no pertenece al conductor");
		}
		if (categoriaMinima.getValor() > vehiculo.getCategoriaVehiculo().getValor()) {
			throw new IllegalArgumentException("La categoría no puede ser mayor a la de su vehículo");
		}

		conductor.setVehiculoActivo(vehiculo);
		conductor.setCategoriaVehiculoActivo(categoriaMinima);
		conductor.setEstadoConductor(EstadoConductor.DISPONIBLE);
	}

	public void retirarConductorDeServicio(Usuario usuario) {
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}
		if (!usuario.esConductor()) {
			throw new IllegalStateException("El usuario no está registrado como conductor");
		}
		Conductor conductor = usuario.getConductor();
		if (conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {
			throw new IllegalStateException("El conductor no está disponible");
		}

		conductor.setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
	}

	// ==================================================================
	// GESTIÓN DE VIAJES
	// ==================================================================

	public List<Usuario> buscarConductoresDisponibles(Viaje viaje) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}

		List<Usuario> conductoresDisponibles = new ArrayList<>();
		Servicio servicio = viaje.getServicio();

		for (Usuario usuario : usuarios.values()) {
			if (!usuario.esConductor()) {
				continue;
			}

			Conductor conductor = usuario.getConductor();
			if (conductor.getEstadoConductor() != EstadoConductor.DISPONIBLE) {
				continue;
			}

			Vehiculo vehiculo = conductor.getVehiculoActivo();

			if (!vehiculo.presta(servicio.getTipoServicio())) {
				continue;
			}
			if (vehiculo.getTipoVehiculo() != servicio.getTipoVehiculo()) {
				continue;
			}
			// la categoría pedida debe estar entre la que eligió el conductor (la más baja
			// que acepta) y la de su vehículo
			int categoriaPedida = servicio.getCategoriaVehiculo().getValor();
			if (categoriaPedida < conductor.getCategoriaVehiculoActivo().getValor()
					|| categoriaPedida > vehiculo.getCategoriaVehiculo().getValor()) {
				continue;
			}
			conductoresDisponibles.add(conductoresDisponibles.size(), usuario);
		}

		return conductoresDisponibles;
	}

	public Viaje solicitarViaje(Usuario cliente, Ubicacion origen, Ubicacion destino, Servicio servicio) {

		if (cliente == null) {
			throw new IllegalArgumentException("El cliente no puede ser nulo");
		}
		if (cliente.getCliente().enViaje()) {
			throw new IllegalStateException("El cliente ya tiene un viaje en curso");
		}

		Viaje viaje = new Viaje(cliente, origen, destino, servicio);
		viaje.solicitar(LocalDateTime.now());
		cliente.getCliente().agregarViaje(viaje);
		viajes.add(viajes.size(), viaje);
		return viaje;
	}

	public void rechazarViaje(Viaje viaje, String motivo) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
		}
		viaje.rechazar(LocalDateTime.now());
	}

	public void aceptarViaje(Viaje viaje, Usuario conductor) {

		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (conductor == null) {
			throw new IllegalArgumentException("El conductor no puede ser nulo");
		}

		List<Usuario> conductoresDisponibles = buscarConductoresDisponibles(viaje);

		if (!contiene(conductoresDisponibles, conductor)) {
			throw new IllegalStateException("El conductor no está disponible para este viaje");
		}
		viaje.aceptar(LocalDateTime.now(), conductor);
	}

	public void iniciarViaje(Viaje viaje) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (viaje.estadoActual() != EstadoViaje.ACEPTADO) {
			throw new IllegalStateException("Solo se puede iniciar un viaje ACEPTADO");
		}
		viaje.iniciar(LocalDateTime.now());
	}

	public void finalizarViaje(Viaje viaje, CalificacionViaje calificacionConductor,
			CalificacionViaje calificacionCliente) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (viaje.estadoActual() != EstadoViaje.INICIADO) {
			throw new IllegalStateException("Solo se puede finalizar un viaje INICIADO");
		}
		viaje.finalizar(LocalDateTime.now(), calificacionConductor, calificacionCliente);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
		}
		viaje.cancelar(LocalDateTime.now(), usuario, motivo);
	}

	public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, double kmRecorridos) {
		if (viaje == null) {
			throw new IllegalArgumentException("El viaje no puede ser nulo");
		}
		if (usuario == null) {
			throw new IllegalArgumentException("El usuario no puede ser nulo");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
		}
		if (kmRecorridos < 0) {
			throw new IllegalArgumentException("Los kilómetros recorridos no pueden ser negativos");
		}
		viaje.cancelar(LocalDateTime.now(), usuario, motivo, kmRecorridos);
	}

	// ==================================================================
	// CONSULTAS
	// ==================================================================

	public List<Viaje> getViajes() {
		return viajes;
	}

	public List<Usuario> getUsuarios() {
		List<Usuario> lista = new ArrayList<>();
		for (Usuario u : usuarios.values()) {
			lista.add(lista.size(), u);
		}
		return lista;
	}

	/** Todos los vehículos de todos los conductores registrados. */
	public List<Vehiculo> getVehiculos() {
		List<Vehiculo> lista = new ArrayList<>();
		for (Usuario u : usuarios.values()) {
			if (u.esConductor()) {
				for (Vehiculo v : u.getConductor().getVehiculos()) {
					lista.add(lista.size(), v);
				}
			}
		}
		return lista;
	}

	public List<Servicio> getServicios() {
		return servicios;
	}

	// ==================================================================
	// MÉTODOS AUXILIARES PRIVADOS
	// ==================================================================

	/** Busca con equals() en cualquier colección recorrible (List de net.datastructures no tiene contains). */
	private <T> boolean contiene(Iterable<T> coleccion, T buscado) {
		for (T e : coleccion) {
			if (Objects.equals(e, buscado)) {
				return true;
			}
		}
		return false;
	}


	    private Conductor obtenerConductor(Usuario usuario) {
	        validarUsuario(usuario);
	        if (usuario.getConductor() == null) {
	            throw new IllegalStateException("El usuario no está habilitado como conductor");
	        }
	        return usuario.getConductor();
	    }
	 
	    private void validarUsuario(Usuario usuario) {
	        Objects.requireNonNull(usuario, "El usuario es obligatorio");
	        if (usuarios.get(usuario.getEmail().trim().toLowerCase()) != usuario) {
	            throw new IllegalArgumentException("El usuario no está registrado");
	        }
	    }
	 
	    private void validarTexto(String texto, String mensaje) {
	        if (texto == null || texto.trim().isEmpty()) {
	            throw new IllegalArgumentException(mensaje);
	        }
	    }
	    
	    
	    
	
}
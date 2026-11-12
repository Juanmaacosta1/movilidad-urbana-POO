package md.logica;

import java.time.LocalDateTime;
import java.util.Objects;

import md.datos.dao.DAOFactory;
import md.config.Loader;
import md.datos.dao.ServicioDAO;
import md.datos.dao.UsuarioDAO;
import md.datos.dto.ServicioDTO;
import md.datos.dto.UsuarioDTO;
import md.datos.dto.VehiculoDTO;

import md.modelo.*;
import net.datastructures.*;

public class Logica {

		private static final double VELOCIDAD_PROMEDIO = 30.0;
		private static Logica instancia;
		private Map<String, Usuario> usuarios = new ChainHashMap<>();
		private List<Servicio> servicios = new ArrayList<>();
		private List<Viaje> viajes = new ArrayList<>();
		private List<Vehiculo> vehiculosRegistrados = new ArrayList<>();
		private ServicioDAO servicioDAO;
		private UsuarioDAO usuarioDAO;

		private Logica() {
			try {
				cargarDatosIniciales();
			} catch (Exception e) {
				e.printStackTrace();
				throw new IllegalStateException("No se pudieron cargar los datos iniciales", e);
			}
		}

		private void cargarDatosIniciales() throws Exception {

			DAOFactory fabrica = Loader.getFactory();
			servicioDAO = fabrica.getServicioDAO();
			usuarioDAO = fabrica.getUsuarioDAO();

			// 1. Obtener los DTO mediante los DAO
			java.util.List<ServicioDTO> serviciosDTO = servicioDAO.obtenerTodos();

			java.util.List<VehiculoDTO> vehiculosDTO = fabrica.getVehiculoDAO().obtenerTodos();

			java.util.List<UsuarioDTO> usuariosDTO = usuarioDAO.obtenerTodos();

			// 2. Convertir los DTO de servicios al modelo
			for (ServicioDTO dto : serviciosDTO) {

				Servicio servicio = new Servicio(dto.getNombre(), dto.getTarifaBase(), dto.getPrecioKm(),
						dto.getPrecioMinuto(), TipoVehiculo.valueOf(dto.getTipoVehiculo()),
						CategoriaVehiculo.valueOf(dto.getCategoriaVehiculo()), TipoServicio.valueOf(dto.getTipoServicio()));

				servicios.add(servicios.size(), servicio);
			}

			// 3. Convertir los DTO de vehículos al modelo
			// Usamos un mapa auxiliar para buscar vehículos por patente.
			java.util.Map<String, Vehiculo> vehiculosPorPatente = new java.util.HashMap<>();

			for (VehiculoDTO dto : vehiculosDTO) {

				java.util.List<String> tipos = dto.getTiposServicio();

				if (tipos == null || tipos.isEmpty()) {
					throw new IllegalArgumentException("El vehículo " + dto.getPatente() + " no tiene tipos de servicio");
				}

				Vehiculo vehiculo = new Vehiculo(dto.getPatente(), dto.getModelo(), dto.getCapacidadPasajeros(),
						TipoVehiculo.valueOf(dto.getTipoVehiculo()), CategoriaVehiculo.valueOf(dto.getCategoriaVehiculo()),
						TipoServicio.valueOf(tipos.get(0)));

				// Agregar los tipos de servicio restantes, si existen.
				for (int i = 1; i < tipos.size(); i++) {
					vehiculo.agregarTipoServicio(TipoServicio.valueOf(tipos.get(i)));
				}

				String patente = vehiculo.getPatente().toUpperCase();

				if (vehiculosPorPatente.containsKey(patente)) {
					throw new IllegalArgumentException("Patente duplicada: " + patente);
				}

				vehiculosPorPatente.put(patente, vehiculo);
				vehiculosRegistrados.add(vehiculosRegistrados.size(), vehiculo);
			}

			// 4. Convertir los DTO de usuarios al modelo
			for (UsuarioDTO dto : usuariosDTO) {

				String email = dto.getEmail().trim().toLowerCase();

				if (usuarios.get(email) != null) {
					throw new IllegalArgumentException("Email duplicado: " + email);
				}

				Usuario usuario = new Usuario(dto.getNombre(), dto.getTelefono(), dto.getEmail());

				String licencia = dto.getLicenciaConducir();
				java.util.List<String> patentes = dto.getPatentesVehiculos();

				// Si tiene licencia, también es conductor.
				if (licencia != null && !licencia.isBlank()) {

					if (patentes == null || patentes.isEmpty()) {
						throw new IllegalArgumentException("El conductor " + email + " no tiene vehículos asociados");
					}

					// La primera patente es obligatoria para el constructor.
					String primeraPatente = patentes.get(0).trim().toUpperCase();

					Vehiculo primerVehiculo = vehiculosPorPatente.get(primeraPatente);

					if (primerVehiculo == null) {
						throw new IllegalArgumentException("No existe el vehículo con patente: " + primeraPatente);
					}

					usuario.altaConductor(licencia, primerVehiculo);

					// Asociar los vehículos adicionales.
					for (int i = 1; i < patentes.size(); i++) {

						String patente = patentes.get(i).trim().toUpperCase();

						Vehiculo vehiculo = vehiculosPorPatente.get(patente);

						if (vehiculo == null) {
							throw new IllegalArgumentException("No existe el vehículo con patente: " + patente);
						}

						usuario.getConductor().agregarVehiculo(vehiculo);
					}
				}

				usuarios.put(email, usuario);
			}
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

		public Usuario registrarUsuarioPersistido(String nombre, String telefono, String email) throws Exception {
			Usuario usuario = validarNuevoUsuario(nombre, telefono, email);
			usuarioDAO.agregar(new UsuarioDTO(usuario.getNombre(), usuario.getTelefono(), usuario.getEmail(), "", java.util.List.of()));
			usuarios.put(usuario.getEmail().toLowerCase(), usuario);
			return usuario;
		}

		public Usuario registrarConductorPersistido(String nombre, String telefono, String email,
				String licencia, Vehiculo vehiculo) throws Exception {
			Usuario usuario = validarNuevoUsuario(nombre, telefono, email);
			if (licencia == null || licencia.isBlank()) {
				throw new IllegalArgumentException("La licencia de conducir es obligatoria");
			}
			if (vehiculo == null) {
				throw new IllegalArgumentException("Elegí un vehículo para el conductor");
			}
			validarCampoTxt(licencia.trim(), "La licencia de conducir");
			usuario.altaConductor(licencia, vehiculo);
			usuarioDAO.agregar(new UsuarioDTO(usuario.getNombre(), usuario.getTelefono(), usuario.getEmail(),
					usuario.getConductor().getLicenciaConducir(), java.util.List.of(vehiculo.getPatente())));
			usuarios.put(usuario.getEmail().toLowerCase(), usuario);
			return usuario;
		}

		private Usuario validarNuevoUsuario(String nombre, String telefono, String email) {
			validarTexto(nombre, "El nombre es obligatorio");
			validarTexto(telefono, "El teléfono es obligatorio");
			validarTexto(email, "El email es obligatorio");
			String clave = email.trim().toLowerCase();
			if (usuarios.get(clave) != null) {
				throw new IllegalArgumentException("Ya existe un usuario con el email " + email);
			}
			Usuario usuario = new Usuario(nombre.trim(), telefono.trim(), email.trim());
			validarCampoTxt(usuario.getNombre(), "El nombre");
			validarCampoTxt(usuario.getTelefono(), "El teléfono");
			validarCampoTxt(usuario.getEmail(), "El email");
			return usuario;
		}

		public void eliminarUsuario(Usuario usuario) throws Exception {
			validarUsuario(usuario);
			if (usuario.getCliente().enViaje()
					|| (usuario.esConductor()
							&& (usuario.getConductor().getEstadoConductor() == EstadoConductor.VIAJE_A_ORIGEN
									|| usuario.getConductor().getEstadoConductor() == EstadoConductor.VIAJE_A_DESTINO))) {
				throw new IllegalStateException("No se puede eliminar un usuario que participa en un viaje activo");
			}
			usuarioDAO.eliminar(usuario.getEmail());
			usuarios.remove(usuario.getEmail().trim().toLowerCase());
		}

		/** @return el usuario con ese email, o {@code null} si no existe. */
		public Usuario buscarUsuario(String email) {
			return email == null ? null : usuarios.get(email.trim().toLowerCase());
		}

		public Usuario actualizarUsuario(Usuario usuario, String nombre, String telefono, String email,
				String licenciaConducir) throws Exception {
			if (usuario == null) {
				throw new IllegalArgumentException("Seleccioná un usuario para editar");
			}
			String emailAnterior = usuario.getEmail();
			String claveAnterior = emailAnterior.trim().toLowerCase();
			if (usuarios.get(claveAnterior) != usuario) {
				throw new IllegalArgumentException("El usuario seleccionado ya no está registrado");
			}
			Usuario datosValidados = new Usuario(nombre, telefono, email);
			String emailNuevo = datosValidados.getEmail().trim();
			String claveNueva = emailNuevo.toLowerCase();
			if (!claveAnterior.equals(claveNueva) && usuarios.get(claveNueva) != null) {
				throw new IllegalArgumentException("Ya existe un usuario con el email " + emailNuevo);
			}
			String licenciaActualizada = "";
			if (usuario.esConductor()) {
				licenciaActualizada = licenciaConducir == null || licenciaConducir.isBlank()
						? usuario.getConductor().getLicenciaConducir()
						: licenciaConducir.trim();
				validarTexto(licenciaActualizada, "La licencia de conducir es obligatoria");
				validarCampoTxt(licenciaActualizada, "La licencia de conducir");
			} else if (licenciaConducir != null && !licenciaConducir.isBlank()) {
				throw new IllegalArgumentException("El usuario seleccionado no está registrado como conductor");
			}
			validarCampoTxt(datosValidados.getNombre(), "El nombre");
			validarCampoTxt(datosValidados.getTelefono(), "El teléfono");
			validarCampoTxt(emailNuevo, "El email");

			java.util.List<String> patentes = new java.util.ArrayList<>();
			if (usuario.esConductor()) {
				for (Vehiculo vehiculo : usuario.getConductor().getVehiculos()) {
					patentes.add(vehiculo.getPatente());
				}
			}
			UsuarioDTO datosPersistidos = new UsuarioDTO(datosValidados.getNombre(),
					datosValidados.getTelefono(), emailNuevo,
					licenciaActualizada, patentes);
			usuarioDAO.actualizar(datosPersistidos, emailAnterior);

			usuario.actualizarDatos(datosValidados.getNombre(), datosValidados.getTelefono(), emailNuevo);
			if (usuario.esConductor()) {
				usuario.getConductor().setLicenciaConducir(licenciaActualizada);
			}
			if (!claveAnterior.equals(claveNueva)) {
				usuarios.remove(claveAnterior);
				usuarios.put(claveNueva, usuario);
			}
			return usuario;
		}

		private void validarCampoTxt(String valor, String nombreCampo) {
			if (valor.indexOf(';') >= 0 || valor.contains("\n") || valor.contains("\r")) {
				throw new IllegalArgumentException(nombreCampo + " no puede contener ';' ni saltos de línea");
			}
		}

		// ==================================================================
		// GESTIÓN DE CONDUCTORES
		// ==================================================================

		/**
		 * El conductor indica con qué vehículo trabaja y hasta qué categoría menor
		 * acepta viajes (el límite superior es la categoría de ese vehículo), y pasa de
		 * Fuera de Servicio a Disponible.
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
			List<Usuario> conductoresDisponibles = new ArrayList<>();
			for (Usuario usuario : buscarConductoresCompatibles(viaje)) {
				if (usuario.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE) {
					conductoresDisponibles.add(conductoresDisponibles.size(), usuario);
				}
			}
			return conductoresDisponibles;
		}

		public List<Usuario> buscarConductoresCompatibles(Viaje viaje) {
			if (viaje == null) {
				throw new IllegalArgumentException("El viaje no puede ser nulo");
			}

			List<Usuario> conductoresCompatibles = new ArrayList<>();
			Servicio servicio = viaje.getServicio();

			for (Usuario usuario : usuarios.values()) {
				if (!usuario.esConductor()) {
					continue;
				}

				Conductor conductor = usuario.getConductor();
				Vehiculo vehiculo = conductor.getVehiculoActivo();

				if (!vehiculo.presta(servicio.getTipoServicio())) {
					continue;
				}
				if (vehiculo.getTipoVehiculo() != servicio.getTipoVehiculo()) {
					continue;
				}
				// Mostramos el pedido solo si entra en el rango que acepta el conductor y
				// en la categoría de su vehículo.
				int categoriaPedida = servicio.getCategoriaVehiculo().getValor();
				if (categoriaPedida < conductor.getCategoriaVehiculoActivo().getValor()
						|| categoriaPedida > vehiculo.getCategoriaVehiculo().getValor()) {
					continue;
				}
				conductoresCompatibles.add(conductoresCompatibles.size(), usuario);
			}
			return conductoresCompatibles;
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
			rechazarViaje(viaje, motivo, LocalDateTime.now());
		}

		public void rechazarViaje(Viaje viaje, String motivo, LocalDateTime fechaHora) {

			if (viaje == null) {
				throw new IllegalArgumentException("El viaje no puede ser nulo");
			}
			if (motivo == null || motivo.isBlank()) {
				throw new IllegalArgumentException("El motivo de rechazo es obligatorio");
			}
			viaje.rechazar(fechaHora);
		}

		public void aceptarViaje(Viaje viaje, Usuario conductor) {
			aceptarViaje(viaje, conductor, LocalDateTime.now());
		}

		public void aceptarViaje(Viaje viaje, Usuario conductor, LocalDateTime fechaHora) {

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
			viaje.aceptar(fechaHora, conductor);
		}

		public void iniciarViaje(Viaje viaje) {
			iniciarViaje(viaje, LocalDateTime.now());
		}

		public void iniciarViaje(Viaje viaje, LocalDateTime fechaHora) {
			if (viaje == null) {
				throw new IllegalArgumentException("El viaje no puede ser nulo");
			}
			if (viaje.estadoActual() != EstadoViaje.ACEPTADO) {
				throw new IllegalStateException("Solo se puede iniciar un viaje ACEPTADO");
			}
			viaje.iniciar(fechaHora);
		}

		public void finalizarViaje(Viaje viaje, CalificacionViaje calificacionConductor,
				CalificacionViaje calificacionCliente) {
			finalizarViaje(viaje, calificacionConductor, calificacionCliente, LocalDateTime.now());
		}

		public void finalizarViaje(Viaje viaje, CalificacionViaje calificacionConductor,
				CalificacionViaje calificacionCliente, LocalDateTime fechaHora) {
			if (viaje == null) {
				throw new IllegalArgumentException("El viaje no puede ser nulo");
			}
			if (viaje.estadoActual() != EstadoViaje.INICIADO) {
				throw new IllegalStateException("Solo se puede finalizar un viaje INICIADO");
			}
			viaje.finalizar(fechaHora, calificacionCliente, calificacionConductor);
		}

		public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo) {
			cancelarViaje(viaje, usuario, motivo, LocalDateTime.now());
		}

		public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, LocalDateTime fechaHora) {
			if (viaje == null) {
				throw new IllegalArgumentException("El viaje no puede ser nulo");
			}
			if (usuario == null) {
				throw new IllegalArgumentException("El usuario no puede ser nulo");
			}
			if (motivo == null || motivo.isBlank()) {
				throw new IllegalArgumentException("El motivo de cancelación es obligatorio");
			}
			viaje.cancelar(fechaHora, usuario, motivo);
		}

		public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, double kmRecorridos) {
			cancelarViaje(viaje, usuario, motivo, kmRecorridos, LocalDateTime.now());
		}

		public void cancelarViaje(Viaje viaje, Usuario usuario, String motivo, double kmRecorridos,
				LocalDateTime fechaHora) {
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
			viaje.cancelar(fechaHora, usuario, motivo, kmRecorridos);
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

		public List<Vehiculo> getVehiculosRegistrados() {
			return vehiculosRegistrados;
		}

		public List<Servicio> getServicios() {
			return servicios;
		}

		public Servicio agregarServicio(String nombre, double tarifaBase, double precioKm, double precioMinuto,
				TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo, TipoServicio tipoServicio)
				throws Exception {
			Servicio nuevo = crearServicio(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo,
					categoriaVehiculo, tipoServicio);
			validarNombreServicio(nuevo.getNombre(), null);
			java.util.List<ServicioDTO> serviciosActualizados = convertirServicios(nuevo, null);
			servicioDAO.guardarTodos(serviciosActualizados);
			servicios.add(servicios.size(), nuevo);
			return nuevo;
		}

		public Servicio actualizarServicio(Servicio original, String nombre, double tarifaBase, double precioKm,
				double precioMinuto, TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo,
				TipoServicio tipoServicio) throws Exception {
			if (original == null) {
				throw new IllegalArgumentException("Seleccioná el servicio que querés editar");
			}
			int indiceOriginal = -1;
			for (int i = 0; i < servicios.size(); i++) {
				if (servicios.get(i) == original) {
					indiceOriginal = i;
					break;
				}
			}
			if (indiceOriginal < 0) {
				throw new IllegalArgumentException("El servicio seleccionado ya no existe");
			}
			Servicio actualizado = crearServicio(nombre, tarifaBase, precioKm, precioMinuto, tipoVehiculo,
					categoriaVehiculo, tipoServicio);
			validarNombreServicio(actualizado.getNombre(), original);
			java.util.List<ServicioDTO> serviciosActualizados = convertirServicios(actualizado, original);
			servicioDAO.guardarTodos(serviciosActualizados);
			servicios.set(indiceOriginal, actualizado);
			return actualizado;
		}

		private Servicio crearServicio(String nombre, double tarifaBase, double precioKm, double precioMinuto,
				TipoVehiculo tipoVehiculo, CategoriaVehiculo categoriaVehiculo, TipoServicio tipoServicio) {
			if (nombre == null || nombre.isBlank() || nombre.indexOf(';') >= 0
					|| nombre.contains("\n") || nombre.contains("\r")) {
				throw new IllegalArgumentException("Ingresá un nombre de servicio válido");
			}
			if (!Double.isFinite(tarifaBase) || !Double.isFinite(precioKm) || !Double.isFinite(precioMinuto)
					|| tarifaBase < 0 || precioKm < 0 || precioMinuto < 0) {
				throw new IllegalArgumentException("Los precios deben ser números válidos y no negativos");
			}
			if (tipoVehiculo == null || categoriaVehiculo == null || tipoServicio == null) {
				throw new IllegalArgumentException("Completá el tipo, la categoría y la modalidad del servicio");
			}
			return new Servicio(nombre.trim(), tarifaBase, precioKm, precioMinuto, tipoVehiculo,
					categoriaVehiculo, tipoServicio);
		}

		private void validarNombreServicio(String nombre, Servicio excluido) {
			for (Servicio servicio : servicios) {
				if (servicio != excluido && servicio.getNombre().equalsIgnoreCase(nombre.trim())) {
					throw new IllegalArgumentException("Ya existe un servicio con ese nombre");
				}
			}
		}

		private java.util.List<ServicioDTO> convertirServicios(Servicio reemplazo, Servicio original) {
			java.util.List<ServicioDTO> resultado = new java.util.ArrayList<>();
			boolean reemplazado = false;
			for (Servicio servicio : servicios) {
				if (servicio == original) {
					resultado.add(aDTO(reemplazo));
					reemplazado = true;
				} else {
					resultado.add(aDTO(servicio));
				}
			}
			if (original == null || !reemplazado) {
				resultado.add(aDTO(reemplazo));
			}
			return resultado;
		}

		private ServicioDTO aDTO(Servicio servicio) {
			return new ServicioDTO(servicio.getNombre(), servicio.getTarifaBase(), servicio.getPrecioKm(),
					servicio.getPrecioMinuto(), servicio.getTipoVehiculo().name(),
					servicio.getCategoriaVehiculo().name(), servicio.getTipoServicio().name());
		}

		// ==================================================================
		// MÉTODOS AUXILIARES PRIVADOS
		// ==================================================================

		/**
		 * Busca con equals() en cualquier colección recorrible (List de
		 * net.datastructures no tiene contains).
		 */
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

		public void altaConductor(Usuario usario, String licencia, Vehiculo vehiculo) {

			if (usario == null) {
				throw new IllegalArgumentException("El usuario no puede ser nulo");
			}
			if (licencia == null || licencia.isBlank()) {
				throw new IllegalArgumentException("La licencia es obligatoria");
			}
			if (vehiculo == null) {
				throw new IllegalArgumentException("El vehículo no puede ser nulo");
			}
			if (usario.esConductor()) {
				throw new IllegalStateException("El usuario ya está registrado como conductor");
			}

			Conductor conductor = new Conductor(licencia.trim(), vehiculo);
			usario.altaConductor(licencia, vehiculo);
		}
		// agg vehiculo al conductor

		public void agregarVehiculoAConductor(Usuario usuario, Vehiculo vehiculo) {
			obtenerConductor(usuario).agregarVehiculo(vehiculo);
		}

		// cambio de rol

		public void CambiarRol(Usuario usuario, RolUsuario rol) {
			validarUsuario(usuario);
			if (rol == null) {
				throw new IllegalArgumentException("El rol no puede ser nulo");
			}
			usuario.cambiarRolActivo(rol);
		}

		// promedio de estrellas

		public double calcularPromedioEstrellas(Usuario usuario) {
			Conductor conductor = obtenerConductor(usuario);
			int suma = 0;
			int cantidad = 0;
			for (Viaje viaje : conductor.getViajes()) {
				CalificacionViaje calificacion = viaje.getCalificacionConductor();
				if (viaje.estadoActual() != EstadoViaje.FINALIZADO) {
					continue;
				} else if (calificacion != CalificacionViaje.NO_CALIFICADO) {
					suma += calificacion.getValor();
					cantidad++;
				}
			}
			if (cantidad == 0) {
				return 0.0;
			}
			return (double) suma / cantidad;
		}

		// promedio calificaciones que recibio el cliente
		public double calcularPromedioEstrellasCliente(Usuario usuario) {
			validarUsuario(usuario);
			int suma = 0;
			int cantidad = 0;
			for (Viaje viaje : usuario.getCliente().getViajes()) {
				CalificacionViaje calificacion = viaje.getCalificacionCliente();
				if (viaje.estadoActual() != EstadoViaje.FINALIZADO) {
					continue;
				} else if (calificacion != CalificacionViaje.NO_CALIFICADO) {
					suma += calificacion.getValor();
					cantidad++;
				}
			}
			if (cantidad == 0) {
				return 0.0;
			}
			return (double) suma / cantidad;
		}

		// convertirun promedio en estrellas (RECOMENDACION POR IA)

		public String estrellas(double promedio) {
			if (promedio > 5 || promedio < 0) {
				throw new IllegalArgumentException("El promedio debe estar entre 0 y 5");
			}
			int llenas = (int) Math.round(promedio);
			String resultado = "";
			for (int i = 0; i < llenas; i++) {
				if (i < llenas) {
					resultado += "★";
				} else {
					resultado += "☆";
				}
			}
			return resultado;
		}

		// toodos los usuarios que son conductores
		public List<Usuario> getConductores() {
			List<Usuario> listaDeconductores = new ArrayList<>();
			for (Usuario usuario : usuarios.values()) {
				if (usuario.esConductor()) {
					listaDeconductores.add(listaDeconductores.size(), usuario);
				}
			}
			return listaDeconductores;
		}

		// catidad de viajes finalizados por un conductor

		public int cantidadDeViajesFinalizados(Usuario usuario) {
			Conductor conductor = obtenerConductor(usuario);
			int cantidad = 0;
			for (Viaje viaje : conductor.getViajes()) {
				if (viaje.estadoActual() == EstadoViaje.FINALIZADO) {
					cantidad++;
				}
			}
			return cantidad;

		}

		// cantidad de viajes finalizados por un cliente
		public int cantidadDeViajesFinalizadosCliente(Usuario usuario) {
			validarUsuario(usuario);
			int cantidad = 0;
			for (Viaje viaje : usuario.getCliente().getViajes()) {
				if (viaje.estadoActual() == EstadoViaje.FINALIZADO) {
					cantidad++;
				}
			}
			return cantidad;
		}

		// verificacion si el usario esa en un viaje ya sea como cliente o como
		// conductor
		public boolean UsuarioEnViaje(Usuario usuario) {
			validarUsuario(usuario);
			if (usuario.getCliente().enViaje()) {
				return true;
			}
			if (usuario.esConductor()) {
				EstadoConductor estado = usuario.getConductor().getEstadoConductor();
				if (estado == EstadoConductor.DISPONIBLE || estado == EstadoConductor.FUERA_DE_SERVICIO) {
					return false;
				} else {
					return true;
				}
			}
			return false;
		}

		// conductores que estan en servicio (disponibles o en viaje)
		public List<Usuario> getConductoresEnServicio() {
			List<Usuario> lista = new ArrayList<>();

			for (Usuario usuario : usuarios.values()) {
				if (usuario.esConductor()) {
					EstadoConductor estado = usuario.getConductor().getEstadoConductor();
					if (estado != EstadoConductor.FUERA_DE_SERVICIO) {
						lista.add(lista.size(), usuario);
					}
				}
			}
			return lista;
		}
		
	}
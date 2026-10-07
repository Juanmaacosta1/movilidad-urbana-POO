package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import logica.Logica;
import modelo.*;

/** Tests de todos los metodos publicos de la capa logica. */
public class LogicaTest {

	private static final double DELTA = 0.0001;

	private Logica logica;
	private Usuario cliente;
	private Usuario conductor;
	private Vehiculo vehiculo;
	private Servicio servicio;
	private Ubicacion origen;
	private Ubicacion destino;

	@BeforeEach
	void setUp() {
		logica = Logica.getInstance();
		logica.reiniciar(); // la logica es un Singleton: se limpia antes de cada test

		cliente = logica.registrarUsuario("Ana Perez", "+5491111111111", "ana@email.com");
		conductor = logica.registrarUsuario("Beto Gomez", "+5492222222222", "beto@email.com");

		vehiculo = new Vehiculo("AA111AA", "Toyota Corolla", 4, CategoriaVehiculo.CONFORT, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);
		conductor.altaConductor("B1", vehiculo);

		servicio = new Servicio("Confort", 2500, 250, 125, CategoriaVehiculo.CONFORT, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);
		origen = new Ubicacion(-42.65, -64.89);
		destino = new Ubicacion(-42.86, -64.88);
	}

	// ---------------- helpers ----------------

	/** Crea un viaje (sin pasar por la logica) con el historial de estados dado. */
	private Viaje viajeConEstados(Usuario cli, EstadoViaje... estados) {
		Viaje viaje = new Viaje(cli, origen, destino, servicio);
		ArrayList<RegistroViaje> registros = new ArrayList<>();
		for (EstadoViaje estado : estados) {
			registros.add(new RegistroViaje(LocalDateTime.now(), estado));
		}
		viaje.setRegistroViaje(registros);
		return viaje;
	}

	/**
	 * Crea un viaje FINALIZADO con esas calificaciones y lo asocia al cliente y al
	 * conductor de este test.
	 */
	private Viaje viajeFinalizado(CalificacionViaje calConductor, CalificacionViaje calCliente) {
		Viaje viaje = viajeConEstados(cliente, EstadoViaje.SOLICITADO, EstadoViaje.ACEPTADO, EstadoViaje.INICIADO,
				EstadoViaje.FINALIZADO);
		viaje.setCalificacionConductor(calConductor);
		viaje.setCalificacionCliente(calCliente);
		cliente.getCliente().agregarViaje(viaje);
		conductor.getConductor().agregarViaje(viaje);
		return viaje;
	}

	private long contarEstrellas(String texto) {
		return texto.chars().filter(c -> c == '\u2605').count();
	}

	private Usuario nuevoConductorEstandar() {
		Usuario carla = logica.registrarUsuario("Carla Diaz", "+5493333333333", "carla@email.com");
		carla.altaConductor("B2", new Vehiculo("BB222BB", "Fiat Cronos", 4, CategoriaVehiculo.ESTANDAR,
				TipoVehiculo.AUTO, TipoServicio.PASAJEROS));
		return carla;
	}

	// ---------------- singleton / reiniciar ----------------

	@Test
	void getInstance_siempreDevuelveLaMismaInstancia() {
		assertSame(Logica.getInstance(), Logica.getInstance());
	}

	@Test
	void reiniciar_borraUsuariosYViajes() {
		logica.solicitarViaje(cliente, origen, destino, servicio);

		logica.reiniciar();

		assertTrue(logica.getUsuarios().isEmpty());
		assertTrue(logica.getViajes().isEmpty());
		assertTrue(logica.getServicios().isEmpty());
		assertNull(logica.buscarUsuario("ana@email.com"));
	}

	// ---------------- usuarios ----------------

	@Test
	void registrarUsuario_sePuedeBuscarPorEmail() {
		assertEquals(cliente, logica.buscarUsuario("ANA@email.com"));
	}

	@Test
	void registrarUsuario_devuelveUsuarioConDatosLimpios() {
		Usuario u = logica.registrarUsuario("  Carla Diaz  ", " +5493333333333 ", " carla@email.com ");

		assertEquals("Carla Diaz", u.getNombre());
		assertEquals("+5493333333333", u.getTelefono());
		assertEquals("carla@email.com", u.getEmail());
		assertNotNull(u.getCliente()); // todo usuario es tambien cliente
		assertFalse(u.esConductor());
		assertSame(u, logica.buscarUsuario("CARLA@email.com"));
	}

	@Test
	void registrarUsuario_emailRepetido_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class,
				() -> logica.registrarUsuario("Otra Ana", "+5493333333333", "ana@email.com"));
	}

	@Test
	void registrarUsuario_emailRepetidoConOtraMayuscula_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class,
				() -> logica.registrarUsuario("Otra Ana", "+5493333333333", "ANA@EMAIL.COM"));
	}

	@Test
	void registrarUsuario_datosVaciosONulos_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario(null, "1", "x@email.com"));
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("  ", "1", "x@email.com"));
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("X", null, "x@email.com"));
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("X", "", "x@email.com"));
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("X", "1", null));
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("X", "1", " "));
	}

	@Test
	void registrarUsuario_emailInvalido_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.registrarUsuario("X", "1", "sin-arroba"));
	}

	@Test
	void buscarUsuario_inexistenteONulo_devuelveNull() {
		assertNull(logica.buscarUsuario("nadie@email.com"));
		assertNull(logica.buscarUsuario(null));
	}

	@Test
	void getUsuarios_devuelveTodosLosRegistrados() {
		assertEquals(2, logica.getUsuarios().size());

		logica.registrarUsuario("Carla Diaz", "+5493333333333", "carla@email.com");

		assertEquals(3, logica.getUsuarios().size());
	}

	// ---------------- alta de conductor y vehiculos ----------------

	@Test
	void altaConductor_convierteAlUsuarioEnConductor() {
		assertFalse(cliente.esConductor());
		Vehiculo v = new Vehiculo("CC333CC", "Honda Wave", 1, CategoriaVehiculo.ESTANDAR, TipoVehiculo.MOTO,
				TipoServicio.PASAJEROS);

		logica.altaConductor(cliente, "LIC-99", v);

		assertTrue(cliente.esConductor());
		assertEquals("LIC-99", cliente.getConductor().getLicenciaConducir());
		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, cliente.getConductor().getEstadoConductor());
	}

	@Test
	void altaConductor_datosInvalidos_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.altaConductor(null, "L1", vehiculo));
		assertThrows(IllegalArgumentException.class, () -> logica.altaConductor(cliente, null, vehiculo));
		assertThrows(IllegalArgumentException.class, () -> logica.altaConductor(cliente, "  ", vehiculo));
		assertThrows(IllegalArgumentException.class, () -> logica.altaConductor(cliente, "L1", null));
	}

	@Test
	void altaConductor_yaEsConductor_lanzaExcepcion() {
		assertThrows(IllegalStateException.class, () -> logica.altaConductor(conductor, "L2", vehiculo));
	}

	@Test
	void agregarVehiculoAConductor_losVehiculosCrecen() {
		Vehiculo otro = new Vehiculo("DD444DD", "Fiat Cronos", 4, CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);

		logica.agregarVehiculoAConductor(conductor, otro);

		assertEquals(2, conductor.getConductor().getVehiculos().size());
	}

	@Test
	void agregarVehiculoAConductor_vehiculoRepetido_noSeDuplica() {
		logica.agregarVehiculoAConductor(conductor, vehiculo);

		assertEquals(1, conductor.getConductor().getVehiculos().size());
	}

	@Test
	void agregarVehiculoAConductor_casosInvalidos_lanzaExcepcion() {
		Usuario noRegistrado = new Usuario("Fantasma", "1", "fantasma@email.com");

		assertThrows(NullPointerException.class, () -> logica.agregarVehiculoAConductor(null, vehiculo));
		assertThrows(IllegalArgumentException.class, () -> logica.agregarVehiculoAConductor(noRegistrado, vehiculo));
		assertThrows(IllegalStateException.class, () -> logica.agregarVehiculoAConductor(cliente, vehiculo));
		assertThrows(IllegalArgumentException.class, () -> logica.agregarVehiculoAConductor(conductor, null));
	}

	@Test
	void getVehiculos_juntaLosVehiculosDeTodosLosConductores() {
		assertEquals(1, logica.getVehiculos().size());

		logica.agregarVehiculoAConductor(conductor, new Vehiculo("DD444DD", "Fiat Cronos", 4,
				CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO, TipoServicio.PASAJEROS));
		nuevoConductorEstandar();

		assertEquals(3, logica.getVehiculos().size());
	}

	@Test
	void getConductores_soloDevuelveLosUsuariosConductores() {
		assertEquals(1, logica.getConductores().size());
		assertSame(conductor, logica.getConductores().get(0));

		nuevoConductorEstandar();

		assertEquals(2, logica.getConductores().size());
	}

	@Test
	void getServicios_empiezaVacia() {
		assertNotNull(logica.getServicios());
		assertTrue(logica.getServicios().isEmpty());
	}

	// ---------------- cambio de rol ----------------

	@Test
	void cambiarRol_conductorPuedeAlternarEntreRoles() {
		assertEquals(RolUsuario.CLIENTE, conductor.getRolActivo());

		logica.CambiarRol(conductor, RolUsuario.CONDUCTOR);
		assertEquals(RolUsuario.CONDUCTOR, conductor.getRolActivo());

		logica.CambiarRol(conductor, RolUsuario.CLIENTE);
		assertEquals(RolUsuario.CLIENTE, conductor.getRolActivo());
	}

	@Test
	void cambiarRol_clienteNoPuedeSerConductor_lanzaExcepcion() {
		assertThrows(IllegalStateException.class, () -> logica.CambiarRol(cliente, RolUsuario.CONDUCTOR));
	}

	@Test
	void cambiarRol_argumentosInvalidos_lanzaExcepcion() {
		Usuario noRegistrado = new Usuario("Fantasma", "1", "fantasma@email.com");

		assertThrows(IllegalArgumentException.class, () -> logica.CambiarRol(conductor, null));
		assertThrows(IllegalArgumentException.class, () -> logica.CambiarRol(noRegistrado, RolUsuario.CLIENTE));
	}

	@Test
	void cambiarRol_duranteUnViaje_lanzaExcepcion() {
		conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);

		assertThrows(IllegalStateException.class, () -> logica.CambiarRol(conductor, RolUsuario.CLIENTE));
	}

	// ---------------- conductores en servicio ----------------

	@Test
	void ponerConductorEnServicio_quedaDisponibleConVehiculoYCategoria() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);

		Conductor c = conductor.getConductor();
		assertEquals(EstadoConductor.DISPONIBLE, c.getEstadoConductor());
		assertSame(vehiculo, c.getVehiculoActivo());
		assertEquals(CategoriaVehiculo.ESTANDAR, c.getCategoriaVehiculoActivo());
	}

	@Test
	void ponerConductorEnServicio_conOtroVehiculo_cambiaElVehiculoActivo() {
		Vehiculo otro = new Vehiculo("DD444DD", "Fiat Cronos", 4, CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);
		logica.agregarVehiculoAConductor(conductor, otro);

		logica.ponerConductorEnServicio(conductor, otro, CategoriaVehiculo.ESTANDAR);

		assertSame(otro, conductor.getConductor().getVehiculoActivo());
	}

	@Test
	void ponerConductorEnServicio_argumentosNulos_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class,
				() -> logica.ponerConductorEnServicio(null, vehiculo, CategoriaVehiculo.ESTANDAR));
		assertThrows(IllegalArgumentException.class,
				() -> logica.ponerConductorEnServicio(conductor, null, CategoriaVehiculo.ESTANDAR));
		assertThrows(IllegalArgumentException.class,
				() -> logica.ponerConductorEnServicio(conductor, vehiculo, null));
	}

	@Test
	void ponerConductorEnServicio_usuarioNoConductor_lanzaExcepcion() {
		assertThrows(IllegalStateException.class,
				() -> logica.ponerConductorEnServicio(cliente, vehiculo, CategoriaVehiculo.ESTANDAR));
	}

	@Test
	void ponerConductorEnServicio_yaDisponible_lanzaExcepcion() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);

		assertThrows(IllegalStateException.class,
				() -> logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR));
	}

	@Test
	void ponerConductorEnServicio_vehiculoAjeno_lanzaExcepcion() {
		Vehiculo ajeno = new Vehiculo("ZZ999ZZ", "Ford Ka", 4, CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);

		assertThrows(IllegalArgumentException.class,
				() -> logica.ponerConductorEnServicio(conductor, ajeno, CategoriaVehiculo.ESTANDAR));
	}

	@Test
	void ponerConductorEnServicio_categoriaMayorAlVehiculo_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class,
				() -> logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.PREMIUM));
	}

	@Test
	void retirarConductorDeServicio_quedaFueraDeServicio() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);

		logica.retirarConductorDeServicio(conductor);

		assertEquals(EstadoConductor.FUERA_DE_SERVICIO, conductor.getConductor().getEstadoConductor());
	}

	@Test
	void retirarConductorDeServicio_casosInvalidos_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.retirarConductorDeServicio(null));
		assertThrows(IllegalStateException.class, () -> logica.retirarConductorDeServicio(cliente));
		// sigue Fuera de Servicio: no esta disponible
		assertThrows(IllegalStateException.class, () -> logica.retirarConductorDeServicio(conductor));
	}

	@Test
	void getConductoresEnServicio_incluyeDisponiblesYEnViaje() {
		assertTrue(logica.getConductoresEnServicio().isEmpty());

		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		assertEquals(1, logica.getConductoresEnServicio().size());

		conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
		assertEquals(1, logica.getConductoresEnServicio().size());

		logica.reiniciar();
		assertTrue(logica.getConductoresEnServicio().isEmpty());
	}

	// ---------------- busqueda de conductores ----------------

	@Test
	void buscarConductoresDisponibles_viajeNulo_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.buscarConductoresDisponibles(null));
	}

	@Test
	void conductorFueraDeServicio_noRecibeViajes() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertTrue(logica.buscarConductoresDisponibles(viaje).isEmpty());
	}

	@Test
	void conductorEnServicio_recibeElViaje() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertEquals(1, logica.buscarConductoresDisponibles(viaje).size());
	}

	@Test
	void buscarConductoresDisponibles_filtraPorCategoria() {
		Usuario carla = nuevoConductorEstandar();
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR); // acepta ESTANDAR y CONFORT
		logica.ponerConductorEnServicio(carla, carla.getConductor().getVehiculoActivo(), CategoriaVehiculo.ESTANDAR); // solo ESTANDAR

		Servicio estandar = new Servicio("Estandar", 2000, 200, 100, CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);
		Servicio premium = new Servicio("Premium", 3000, 300, 150, CategoriaVehiculo.PREMIUM, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);

		assertEquals(2, logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, estandar)).size());
		assertEquals(1, logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, servicio)).size());
		assertSame(conductor,
				logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, servicio)).get(0));
		assertTrue(logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, premium)).isEmpty());
	}

	@Test
	void buscarConductoresDisponibles_respetaLaCategoriaMinimaDelConductor() {
		// el conductor solo quiere viajes CONFORT o superiores
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.CONFORT);
		Servicio estandar = new Servicio("Estandar", 2000, 200, 100, CategoriaVehiculo.ESTANDAR, TipoVehiculo.AUTO,
				TipoServicio.PASAJEROS);

		assertTrue(logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, estandar)).isEmpty());
		assertEquals(1, logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, servicio)).size());
	}

	@Test
	void buscarConductoresDisponibles_filtraPorTipoDeServicioYDeVehiculo() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Servicio envios = new Servicio("Envios", 1500, 150, 75, CategoriaVehiculo.CONFORT, TipoVehiculo.AUTO,
				TipoServicio.ENVIOS);
		Servicio moto = new Servicio("Mototaxi", 1000, 100, 50, CategoriaVehiculo.CONFORT, TipoVehiculo.MOTO,
				TipoServicio.PASAJEROS);

		// el vehiculo solo presta PASAJEROS y es un AUTO
		assertTrue(logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, envios)).isEmpty());
		assertTrue(logica.buscarConductoresDisponibles(new Viaje(cliente, origen, destino, moto)).isEmpty());
	}

	// ---------------- viajes ----------------

	@Test
	void solicitarViaje_quedaSolicitadoYElClienteEnViaje() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertEquals(EstadoViaje.SOLICITADO, viaje.estadoActual());
		assertTrue(cliente.getCliente().enViaje());
	}

	@Test
	void solicitarViaje_seRegistraEnLaLogicaYEnElCliente() {
		assertTrue(logica.getViajes().isEmpty());

		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertEquals(1, logica.getViajes().size());
		assertSame(viaje, logica.getViajes().get(0));
		assertTrue(cliente.getCliente().getViajes().contains(viaje));
		assertSame(cliente, viaje.getCliente());
	}

	@Test
	void solicitarViaje_clienteNulo_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.solicitarViaje(null, origen, destino, servicio));
	}

	@Test
	void solicitarViaje_clienteYaEnViaje_lanzaExcepcion() {
		logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalStateException.class, () -> logica.solicitarViaje(cliente, origen, destino, servicio));
	}

	@Test
	void solicitarViaje_despuesDeCancelar_permiteUnoNuevo() {
		Viaje primero = logica.solicitarViaje(cliente, origen, destino, servicio);
		logica.cancelarViaje(primero, cliente, "Me arrepenti");

		Viaje segundo = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertEquals(EstadoViaje.SOLICITADO, segundo.estadoActual());
		assertEquals(2, logica.getViajes().size());
	}

	@Test
	void rechazarViaje_quedaRechazado() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		logica.rechazarViaje(viaje, "Sin conductores disponibles");

		assertEquals(EstadoViaje.RECHAZADO, viaje.estadoActual());
		assertFalse(cliente.getCliente().enViaje());
	}

	@Test
	void rechazarViaje_argumentosInvalidos_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class, () -> logica.rechazarViaje(null, "motivo"));
		assertThrows(IllegalArgumentException.class, () -> logica.rechazarViaje(viaje, null));
		assertThrows(IllegalArgumentException.class, () -> logica.rechazarViaje(viaje, "   "));
	}

	@Test
	void aceptarViaje_conductorNoDisponible_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalStateException.class, () -> logica.aceptarViaje(viaje, conductor));
	}

	@Test
	void aceptarViaje_usuarioQueNoEsConductor_lanzaExcepcion() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalStateException.class, () -> logica.aceptarViaje(viaje, cliente));
	}

	@Test
	void aceptarViaje_argumentosNulos_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class, () -> logica.aceptarViaje(null, conductor));
		assertThrows(IllegalArgumentException.class, () -> logica.aceptarViaje(viaje, null));
	}

	@Test
	void aceptarViaje_quedaAceptadoYElConductorVaAlOrigen() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		logica.aceptarViaje(viaje, conductor);

		assertEquals(EstadoViaje.ACEPTADO, viaje.estadoActual());
		assertEquals(EstadoConductor.VIAJE_A_ORIGEN, conductor.getConductor().getEstadoConductor());
		assertSame(conductor, viaje.getConductor());
	}

	@Test
	void iniciarViaje_quedaIniciadoYElConductorVaAlDestino() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);
		logica.aceptarViaje(viaje, conductor);

		logica.iniciarViaje(viaje);

		assertEquals(EstadoViaje.INICIADO, viaje.estadoActual());
		assertEquals(EstadoConductor.VIAJE_A_DESTINO, conductor.getConductor().getEstadoConductor());
	}

	@Test
	void iniciarViaje_nuloOSinAceptar_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class, () -> logica.iniciarViaje(null));
		assertThrows(IllegalStateException.class, () -> logica.iniciarViaje(viaje)); // sigue SOLICITADO
	}

	@Test
	void viajeCompleto_terminaFinalizadoYElConductorQuedaDisponible() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		logica.aceptarViaje(viaje, conductor);
		logica.iniciarViaje(viaje);
		logica.finalizarViaje(viaje, CalificacionViaje.BUENO, CalificacionViaje.EXCELENTE);

		assertEquals(EstadoViaje.FINALIZADO, viaje.estadoActual());
		assertEquals(EstadoConductor.DISPONIBLE, conductor.getConductor().getEstadoConductor());
		assertFalse(cliente.getCliente().enViaje());
	}

	@Test
	void finalizarViaje_guardaLasCalificaciones() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);
		logica.aceptarViaje(viaje, conductor);
		logica.iniciarViaje(viaje);

		logica.finalizarViaje(viaje, CalificacionViaje.BUENO, CalificacionViaje.EXCELENTE);

		assertEquals(CalificacionViaje.BUENO, viaje.getCalificacionConductor());
		assertEquals(CalificacionViaje.EXCELENTE, viaje.getCalificacionCliente());
	}

	@Test
	void finalizarViaje_sinCalificar_lanzaExcepcion() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);
		logica.aceptarViaje(viaje, conductor);
		logica.iniciarViaje(viaje);

		assertThrows(IllegalArgumentException.class,
				() -> logica.finalizarViaje(viaje, CalificacionViaje.NO_CALIFICADO, CalificacionViaje.BUENO));
	}

	@Test
	void finalizarViaje_nuloOSinIniciar_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class,
				() -> logica.finalizarViaje(null, CalificacionViaje.BUENO, CalificacionViaje.BUENO));
		assertThrows(IllegalStateException.class,
				() -> logica.finalizarViaje(viaje, CalificacionViaje.BUENO, CalificacionViaje.BUENO));
	}

	@Test
	void cancelarViaje_antesDeAceptar_quedaCancelado() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		logica.cancelarViaje(viaje, cliente, "Me arrepenti");

		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
		assertEquals("Me arrepenti", viaje.getmotivoCancelacion());
		assertFalse(cliente.getCliente().enViaje());
	}

	@Test
	void cancelarViaje_argumentosInvalidos_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(null, cliente, "motivo"));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, null, "motivo"));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, cliente, null));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, cliente, " "));
	}

	@Test
	void cancelarViaje_conKmRecorridos_quedaCancelado() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);
		logica.aceptarViaje(viaje, conductor);
		logica.iniciarViaje(viaje);

		logica.cancelarViaje(viaje, cliente, "Cambio de planes", 2.5);

		assertEquals(EstadoViaje.CANCELADO, viaje.estadoActual());
		assertEquals("Cambio de planes", viaje.getmotivoCancelacion());
		assertTrue(viaje.getCostoCancelacion() >= 0);
	}

	@Test
	void cancelarViaje_conKmRecorridos_argumentosInvalidos_lanzaExcepcion() {
		Viaje viaje = logica.solicitarViaje(cliente, origen, destino, servicio);

		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(null, cliente, "motivo", 1.0));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, null, "motivo", 1.0));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, cliente, "", 1.0));
		assertThrows(IllegalArgumentException.class, () -> logica.cancelarViaje(viaje, cliente, "motivo", -1.0));
	}

	// ---------------- estadisticas ----------------

	@Test
	void calcularPromedioEstrellas_sinViajes_esCero() {
		assertEquals(0.0, logica.calcularPromedioEstrellas(conductor), DELTA);
	}

	@Test
	void calcularPromedioEstrellas_promediaSoloViajesFinalizadosYCalificados() {
		viajeFinalizado(CalificacionViaje.BUENO, CalificacionViaje.MALO); // 3
		viajeFinalizado(CalificacionViaje.EXCELENTE, CalificacionViaje.MALO); // 5
		viajeFinalizado(CalificacionViaje.NO_CALIFICADO, CalificacionViaje.MALO); // se ignora

		Viaje cancelado = viajeConEstados(cliente, EstadoViaje.SOLICITADO, EstadoViaje.CANCELADO);
		cancelado.setCalificacionConductor(CalificacionViaje.MALO); // se ignora: no esta finalizado
		conductor.getConductor().agregarViaje(cancelado);

		assertEquals(4.0, logica.calcularPromedioEstrellas(conductor), DELTA);
	}

	@Test
	void calcularPromedioEstrellas_usuarioNoConductor_lanzaExcepcion() {
		assertThrows(IllegalStateException.class, () -> logica.calcularPromedioEstrellas(cliente));
	}

	@Test
	void calcularPromedioEstrellasCliente_sinViajes_esCero() {
		assertEquals(0.0, logica.calcularPromedioEstrellasCliente(cliente), DELTA);
	}

	@Test
	void calcularPromedioEstrellasCliente_promediaSoloViajesFinalizadosYCalificados() {
		viajeFinalizado(CalificacionViaje.MALO, CalificacionViaje.MUY_BUENO); // 4
		viajeFinalizado(CalificacionViaje.MALO, CalificacionViaje.REGULAR); // 2
		viajeFinalizado(CalificacionViaje.MALO, CalificacionViaje.NO_CALIFICADO); // se ignora

		Viaje enCurso = viajeConEstados(cliente, EstadoViaje.SOLICITADO);
		enCurso.setCalificacionCliente(CalificacionViaje.EXCELENTE); // se ignora: no esta finalizado
		cliente.getCliente().agregarViaje(enCurso);

		assertEquals(3.0, logica.calcularPromedioEstrellasCliente(cliente), DELTA);
	}

	@Test
	void calcularPromedioEstrellasCliente_usuarioNoRegistrado_lanzaExcepcion() {
		Usuario noRegistrado = new Usuario("Fantasma", "1", "fantasma@email.com");

		assertThrows(IllegalArgumentException.class, () -> logica.calcularPromedioEstrellasCliente(noRegistrado));
	}

	@Test
	void estrellas_cantidadDeEstrellasLlenasSegunElPromedio() {
		assertEquals(0L, contarEstrellas(logica.estrellas(0)));
		assertEquals(3L, contarEstrellas(logica.estrellas(3)));
		assertEquals(4L, contarEstrellas(logica.estrellas(3.6))); // redondea hacia arriba
		assertEquals(3L, contarEstrellas(logica.estrellas(3.4))); // redondea hacia abajo
		assertEquals(5L, contarEstrellas(logica.estrellas(5)));
	}

	@Test
	void estrellas_promedioFueraDeRango_lanzaExcepcion() {
		assertThrows(IllegalArgumentException.class, () -> logica.estrellas(-0.1));
		assertThrows(IllegalArgumentException.class, () -> logica.estrellas(5.1));
	}

	@Test
	void cantidadDeViajesFinalizados_cuentaSoloLosFinalizadosDelConductor() {
		assertEquals(0, logica.cantidadDeViajesFinalizados(conductor));

		viajeFinalizado(CalificacionViaje.BUENO, CalificacionViaje.BUENO);
		viajeFinalizado(CalificacionViaje.BUENO, CalificacionViaje.BUENO);
		conductor.getConductor().agregarViaje(viajeConEstados(cliente, EstadoViaje.SOLICITADO, EstadoViaje.CANCELADO));

		assertEquals(2, logica.cantidadDeViajesFinalizados(conductor));
	}

	@Test
	void cantidadDeViajesFinalizados_usuarioNoConductor_lanzaExcepcion() {
		assertThrows(IllegalStateException.class, () -> logica.cantidadDeViajesFinalizados(cliente));
	}

	@Test
	void cantidadDeViajesFinalizadosCliente_cuentaSoloLosFinalizadosDelCliente() {
		assertEquals(0, logica.cantidadDeViajesFinalizadosCliente(cliente));

		viajeFinalizado(CalificacionViaje.BUENO, CalificacionViaje.BUENO);
		cliente.getCliente().agregarViaje(viajeConEstados(cliente, EstadoViaje.SOLICITADO, EstadoViaje.RECHAZADO));

		assertEquals(1, logica.cantidadDeViajesFinalizadosCliente(cliente));
	}

	@Test
	void cantidadDeViajesFinalizadosCliente_usuarioNoRegistrado_lanzaExcepcion() {
		Usuario noRegistrado = new Usuario("Fantasma", "1", "fantasma@email.com");

		assertThrows(IllegalArgumentException.class, () -> logica.cantidadDeViajesFinalizadosCliente(noRegistrado));
	}

	@Test
	void usuarioEnViaje_sinViajesNiServicio_esFalse() {
		assertFalse(logica.UsuarioEnViaje(cliente));
		assertFalse(logica.UsuarioEnViaje(conductor)); // Fuera de Servicio
	}

	@Test
	void usuarioEnViaje_conductorDisponible_esFalse() {
		logica.ponerConductorEnServicio(conductor, vehiculo, CategoriaVehiculo.ESTANDAR);

		assertFalse(logica.UsuarioEnViaje(conductor));
	}

	@Test
	void usuarioEnViaje_conductorYendoAlOrigenOAlDestino_esTrue() {
		conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
		assertTrue(logica.UsuarioEnViaje(conductor));

		conductor.getConductor().setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
		assertTrue(logica.UsuarioEnViaje(conductor));
	}

	@Test
	void usuarioEnViaje_clienteConViajeActivo_esTrue() {
		cliente.getCliente().agregarViaje(viajeConEstados(cliente, EstadoViaje.SOLICITADO));

		assertTrue(logica.UsuarioEnViaje(cliente));
	}

	@Test
	void usuarioEnViaje_clienteConViajeFinalizado_esFalse() {
		viajeFinalizado(CalificacionViaje.BUENO, CalificacionViaje.BUENO);

		assertFalse(logica.UsuarioEnViaje(cliente));
	}

	@Test
	void usuarioEnViaje_usuarioNoRegistrado_lanzaExcepcion() {
		Usuario noRegistrado = new Usuario("Fantasma", "1", "fantasma@email.com");

		assertThrows(IllegalArgumentException.class, () -> logica.UsuarioEnViaje(noRegistrado));
	}
}
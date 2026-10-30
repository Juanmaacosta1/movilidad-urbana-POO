package md.aplicacion;

import java.util.Scanner;

import md.logica.Logica;
import md.modelo.*;

public class AplicacionConsulta {

    public static void main(String[] args) {

        try (Scanner entrada = new Scanner(System.in)) {

            Logica logica = Logica.getInstance();
            int opcion = -1;

            while (opcion != Constante.OPCION_SALIR) {

                mostrarMenu();

                System.out.print("Seleccione una opción: ");
                String texto = entrada.nextLine();

                try {
                    opcion = Integer.parseInt(texto.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Debe ingresar un número válido.");
                    continue;
                }

                switch (opcion) {

                    case Constante.OPCION_USUARIOS:
                        consultarUsuarios(logica);
                        break;

                    case Constante.OPCION_SERVICIOS:
                        consultarServicios(logica);
                        break;

                    case Constante.OPCION_VEHICULOS:
                        consultarVehiculos(logica);
                        break;

                    case Constante.OPCION_BUSCAR_USUARIO:
                        buscarUsuarioPorEmail(logica, entrada);
                        break;

                    case Constante.OPCION_CONDUCTORES_EN_SERVICIO:
                        consultarConductoresEnServicio(logica);
                        break;

                    case Constante.OPCION_VIAJES:
                        consultarViajes(logica);
                        break;

                    case Constante.OPCION_BUSCAR_VEHICULO:
                        buscarVehiculoPorPatente(logica, entrada);
                        break;

                    case Constante.OPCION_HISTORIAL_CLIENTE:
                        consultarHistorialCliente(logica, entrada);
                        break;

                    case Constante.OPCION_ESTADISTICAS_CONDUCTOR:
                        consultarEstadisticasConductor(logica, entrada);
                        break;

                    case Constante.OPCION_SALIR:
                        System.out.println("Hasta luego.");
                        break;

                    default:
                        System.out.println("Opción inválida.");
                }

                System.out.println();
            }

        } catch (Exception e) {
            System.out.println(
                    "No se pudo iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void mostrarMenu() {

        System.out.println("========================================");
        System.out.println("           MOVILIDAD DIGITAL");
        System.out.println("========================================");
        System.out.println(
                Constante.OPCION_USUARIOS + ". Consultar todos los usuarios");
        System.out.println(
                Constante.OPCION_SERVICIOS + ". Consultar todos los servicios");
        System.out.println(
                Constante.OPCION_VEHICULOS + ". Consultar todos los vehículos");
        System.out.println(
                Constante.OPCION_BUSCAR_USUARIO + ". Buscar usuario por email");
        System.out.println(
                Constante.OPCION_CONDUCTORES_EN_SERVICIO
                + ". Consultar conductores en servicio");
        System.out.println(
                Constante.OPCION_VIAJES + ". Consultar viajes");
        System.out.println(
                Constante.OPCION_BUSCAR_VEHICULO + ". Buscar vehículo por patente");
        System.out.println(
                Constante.OPCION_HISTORIAL_CLIENTE
                + ". Consultar historial de un cliente");
        System.out.println(
                Constante.OPCION_ESTADISTICAS_CONDUCTOR
                + ". Consultar estadísticas de un conductor");
        System.out.println(
                Constante.OPCION_SALIR + ". Salir");
        System.out.println("========================================");
    }

    // --------------------------------------------------
    // CONSULTAR USUARIOS
    // --------------------------------------------------

    private static void consultarUsuarios(Logica logica) {

        System.out.println("\n===== USUARIOS =====");

        for (Usuario usuario : logica.getUsuarios()) {

            String rol = "Cliente";

            if (usuario.esConductor()) {
                rol += " y conductor";
            }

            System.out.println(
                    usuario.getNombre() + " | "
                    + usuario.getTelefono() + " | "
                    + usuario.getEmail() + " | "
                    + rol);
        }
    }

    // --------------------------------------------------
    // CONSULTAR SERVICIOS
    // --------------------------------------------------

    private static void consultarServicios(Logica logica) {

        System.out.println("\n===== SERVICIOS =====");

        for (Servicio servicio : logica.getServicios()) {

            System.out.println(
                    servicio.getNombre()
                    + " | Tarifa base: $" + servicio.getTarifaBase()
                    + " | Precio/km: $" + servicio.getPrecioKm()
                    + " | Precio/minuto: $" + servicio.getPrecioMinuto()
                    + " | Vehículo: " + servicio.getTipoVehiculo()
                    + " | Categoría: " + servicio.getCategoriaVehiculo()
                    + " | Tipo: " + servicio.getTipoServicio());
        }
    }

    // --------------------------------------------------
    // CONSULTAR VEHÍCULOS
    // --------------------------------------------------

    private static void consultarVehiculos(Logica logica) {

        System.out.println("\n===== VEHÍCULOS =====");

        for (Vehiculo vehiculo : logica.getVehiculos()) {

            System.out.println(
                    vehiculo.getPatente()
                    + " | " + vehiculo.getModelo()
                    + " | Capacidad: " + vehiculo.getCapacidadPasajeros()
                    + " | Tipo: " + vehiculo.getTipoVehiculo()
                    + " | Categoría: " + vehiculo.getCategoriaVehiculo()
                    + " | Servicios: " + vehiculo.getTipoServicios());
        }
    }

    // --------------------------------------------------
    // BUSCAR USUARIO POR EMAIL
    // --------------------------------------------------

    private static void buscarUsuarioPorEmail(
            Logica logica, Scanner entrada) {

        System.out.print("Ingrese el email del usuario: ");
        String email = entrada.nextLine().trim();

        Usuario usuario = logica.buscarUsuario(email);

        if (usuario == null) {
            System.out.println("No existe un usuario con ese email.");
            return;
        }

        System.out.println("\n===== DATOS DEL USUARIO =====");
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Teléfono: " + usuario.getTelefono());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Rol activo: " + usuario.getRolActivo());

        if (usuario.esConductor()) {

            Conductor conductor = usuario.getConductor();

            System.out.println(
                    "Licencia: " + conductor.getLicenciaConducir());
            System.out.println(
                    "Estado: " + conductor.getEstadoConductor());
            System.out.println("Vehículos:");

            for (Vehiculo vehiculo : conductor.getVehiculos()) {
                System.out.println("  " + vehiculo);
            }
        }
    }

    // --------------------------------------------------
    // CONSULTAR CONDUCTORES EN SERVICIO
    // --------------------------------------------------

    private static void consultarConductoresEnServicio(Logica logica) {

        System.out.println("\n===== CONDUCTORES EN SERVICIO =====");

        for (Usuario usuario : logica.getConductoresEnServicio()) {

            System.out.println(
                    usuario.getNombre()
                    + " | " + usuario.getEmail()
                    + " | Estado: "
                    + usuario.getConductor().getEstadoConductor());
        }
    }

    // --------------------------------------------------
    // CONSULTAR VIAJES
    // --------------------------------------------------

    private static void consultarViajes(Logica logica) {

        System.out.println("\n===== VIAJES =====");

        if (logica.getViajes().isEmpty()) {
            System.out.println("Todavía no hay viajes registrados.");
            return;
        }

        for (Viaje viaje : logica.getViajes()) {

            System.out.println(
                    "ID: " + viaje.getId()
                    + " | Cliente: " + viaje.getCliente().getNombre()
                    + " | Servicio: " + viaje.getServicio().getNombre()
                    + " | Estado: " + viaje.estadoActual());
        }
    }

    // --------------------------------------------------
    // NUEVA CONSULTA 1: BUSCAR VEHÍCULO POR PATENTE
    // --------------------------------------------------

    private static void buscarVehiculoPorPatente(
            Logica logica, Scanner entrada) {

        System.out.print("Ingrese la patente del vehículo: ");
        String patente = entrada.nextLine().trim();

        Vehiculo encontrado = null;

        for (Vehiculo vehiculo : logica.getVehiculos()) {

            if (vehiculo.getPatente().equalsIgnoreCase(patente)) {
                encontrado = vehiculo;
                break;
            }
        }

        if (encontrado == null) {
            System.out.println("No se encontró un vehículo con esa patente.");
            return;
        }

        System.out.println("\n===== VEHÍCULO ENCONTRADO =====");
        System.out.println("Patente: " + encontrado.getPatente());
        System.out.println("Modelo: " + encontrado.getModelo());
        System.out.println(
                "Capacidad: " + encontrado.getCapacidadPasajeros());
        System.out.println("Tipo: " + encontrado.getTipoVehiculo());
        System.out.println("Categoría: " + encontrado.getCategoriaVehiculo());
        System.out.println("Servicios: " + encontrado.getTipoServicios());

        // Buscar qué conductor tiene registrado ese vehículo.
        for (Usuario usuario : logica.getConductores()) {

            for (Vehiculo vehiculo
                    : usuario.getConductor().getVehiculos()) {

                if (vehiculo.getPatente().equalsIgnoreCase(patente)) {

                    System.out.println(
                            "Conductor registrado: " + usuario.getNombre());
                    System.out.println("Email: " + usuario.getEmail());
                    return;
                }
            }
        }

        System.out.println(
                "No se encontró un conductor asociado a este vehículo.");
    }

    // --------------------------------------------------
    // NUEVA CONSULTA 2: HISTORIAL DE UN CLIENTE
    // --------------------------------------------------

    private static void consultarHistorialCliente(
            Logica logica, Scanner entrada) {

        System.out.print("Ingrese el email del cliente: ");
        String email = entrada.nextLine().trim();

        Usuario usuario = logica.buscarUsuario(email);

        if (usuario == null) {
            System.out.println("No existe un usuario con ese email.");
            return;
        }

        java.util.List<Viaje> viajes = usuario.getCliente().getViajes();

        System.out.println("\n===== HISTORIAL DE VIAJES =====");
        System.out.println("Cliente: " + usuario.getNombre());

        if (viajes.isEmpty()) {
            System.out.println("El cliente todavía no tiene viajes registrados.");
            return;
        }

        for (Viaje viaje : viajes) {

            System.out.println("\n--------------------------------");
            System.out.println("ID: " + viaje.getId());
            System.out.println("Servicio: " + viaje.getServicio().getNombre());
            System.out.println("Estado actual: " + viaje.estadoActual());

            if (viaje.getConductor() != null) {
                System.out.println(
                        "Conductor: " + viaje.getConductor().getNombre());
            } else {
                System.out.println("Conductor: Sin asignar");
            }

            System.out.println("Historial de estados:");

            for (RegistroViaje registro : viaje.getRegistroViaje()) {

                System.out.println(
                        "  " + registro.getFechaHora()
                        + " | " + registro.getEstadoViaje());
            }
        }
    }

    // --------------------------------------------------
    // NUEVA CONSULTA 3: ESTADÍSTICAS DE UN CONDUCTOR
    // --------------------------------------------------

    private static void consultarEstadisticasConductor(
            Logica logica, Scanner entrada) {

        System.out.print("Ingrese el email del conductor: ");
        String email = entrada.nextLine().trim();

        Usuario usuario = logica.buscarUsuario(email);

        if (usuario == null) {
            System.out.println("No existe un usuario con ese email.");
            return;
        }

        if (!usuario.esConductor()) {
            System.out.println("El usuario no está registrado como conductor.");
            return;
        }

        Conductor conductor = usuario.getConductor();

        int viajesFinalizados =
                logica.cantidadDeViajesFinalizados(usuario);

        double promedio =
                logica.calcularPromedioEstrellas(usuario);

        System.out.println("\n===== ESTADÍSTICAS DEL CONDUCTOR =====");
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Email: " + usuario.getEmail());
        System.out.println("Licencia: " + conductor.getLicenciaConducir());
        System.out.println("Estado: " + conductor.getEstadoConductor());
        System.out.println("Viajes finalizados: " + viajesFinalizados);
        System.out.printf("Promedio de calificaciones: %.2f / 5%n", promedio);

        System.out.println("Vehículos registrados:");

        for (Vehiculo vehiculo : conductor.getVehiculos()) {
            System.out.println("  " + vehiculo);
        }
    }
}
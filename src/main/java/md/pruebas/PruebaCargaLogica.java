package md.pruebas;

import md.logica.Logica;

public class PruebaCargaLogica {

    public static void main(String[] args) {

        Logica logica = Logica.getInstance();

        System.out.println("===== DATOS EN LOGICA =====");
        System.out.println("Servicios: " + logica.getServicios().size());
        System.out.println("Vehículos: " + logica.getVehiculos().size());
        System.out.println("Usuarios: " + logica.getUsuarios().size());
        System.out.println("Conductores: " + logica.getConductores().size());
    }
}
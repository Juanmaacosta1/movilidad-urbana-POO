package md.datos.dao;

import java.io.IOException;

import md.datos.CargarParametros;
import md.datos.dao.txt.TxtDAOFactory;

public class DAOLoader {

    private static DAOFactory instanciaFabrica;

    public static DAOFactory getFactory() throws IOException {

        if (instanciaFabrica == null) {

            // Cargar las rutas desde config.properties
            CargarParametros.parametros();

            // Crear la fábrica que trabaja con archivos TXT
            instanciaFabrica = new TxtDAOFactory(
                    CargarParametros.getArchivoServicios(),
                    CargarParametros.getArchivoVehiculos(),
                    CargarParametros.getArchivoUsuarios()
            );
        }

        return instanciaFabrica;
    }
}
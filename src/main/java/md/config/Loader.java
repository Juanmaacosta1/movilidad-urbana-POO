package md.config;

import java.io.IOException;

import md.datos.dao.DAOFactory;
import md.datos.dao.txt.TxtDAOFactory;

public class Loader {

    private static DAOFactory instanciaFabrica;

    public static DAOFactory getFactory() throws IOException {

        if (instanciaFabrica == null) {

            // Cargar las rutas desde config.properties
            CargaParametros.parametros();

            // Crear la fábrica que trabaja con archivos TXT
            instanciaFabrica = new TxtDAOFactory(
                    CargaParametros.getArchivoServicios(),
                    CargaParametros.getArchivoVehiculos(),
                    CargaParametros.getArchivoUsuarios()
            );
        }

        return instanciaFabrica;
    }
}
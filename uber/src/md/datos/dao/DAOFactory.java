package md.datos.dao;

public interface DAOFactory {

    ServicioDAO getServicioDAO();

    VehiculoDAO getVehiculoDAO();

    UsuarioDAO getUsuarioDAO();

}
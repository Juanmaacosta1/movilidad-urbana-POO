package md.datos.dao;

import java.util.List;
import md.datos.dto.UsuarioDTO;

public interface UsuarioDAO {

    List<UsuarioDTO> obtenerTodos() throws Exception;

    default void agregar(UsuarioDTO usuario) throws Exception {
        throw new UnsupportedOperationException("Este origen de datos no permite agregar usuarios");
    }

    default void actualizar(UsuarioDTO usuario, String emailAnterior) throws Exception {
        throw new UnsupportedOperationException("Este origen de datos no permite editar usuarios");
    }

    default void eliminar(String email) throws Exception {
        throw new UnsupportedOperationException("Este origen de datos no permite eliminar usuarios");
    }

}
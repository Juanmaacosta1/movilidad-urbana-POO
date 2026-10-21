package md.datos.dao.txt;

import java.util.ArrayList;
import java.util.List;

import md.datos.dao.UsuarioDAO;
import md.datos.dto.UsuarioDTO;

/** Formato: nombre;telefono;email[;licenciaConducir;patente;patente...] */
public class UsuarioDaoTxt implements UsuarioDAO {

	private String rutaArchivo;

	public UsuarioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<UsuarioDTO> obtenerTodos() throws Exception {
		List<UsuarioDTO> lista = new ArrayList<>();
		LectorTxt.leer(rutaArchivo, 3, Integer.MAX_VALUE, campos -> {
			String licencia = campos.length > 3 ? campos[3] : null;
			List<String> patentes = new ArrayList<>();
			for (int i = 4; i < campos.length; i++) {
				patentes.add(campos[i]);
			}
			lista.add(new UsuarioDTO(campos[0], campos[1], campos[2], licencia, patentes));
		});
		return lista;
	}
}
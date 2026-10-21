package md.datos.dao.txt;

import java.util.ArrayList;
import java.util.List;

import md.datos.dao.VehiculoDAO;
import md.datos.dto.VehiculoDTO;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/**
 * Formato: patente;modelo;capacidadPasajeros;TipoVehiculo;CategoriaVehiculo;TipoServicio[;TipoServicio]
 */
public class VehiculoDaoTxt implements VehiculoDAO {

	private String rutaArchivo;

	public VehiculoDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<VehiculoDTO> obtenerTodos() throws Exception {
		List<VehiculoDTO> lista = new ArrayList<>();
		LectorTxt.leer(rutaArchivo, 6, 7, campos -> {
			List<TipoServicio> tipos = new ArrayList<>();
			for (int i = 5; i < campos.length; i++) {
				TipoServicio tipo = TipoServicio.valueOf(campos[i].toUpperCase());
				if (tipos.contains(tipo)) {
					throw new IllegalArgumentException("tipo de servicio repetido " + tipo);
				}
				tipos.add(tipo);
			}
			lista.add(new VehiculoDTO(campos[0], campos[1], Integer.parseInt(campos[2]),
					TipoVehiculo.valueOf(campos[3].toUpperCase()),
					CategoriaVehiculo.valueOf(campos[4].toUpperCase()), tipos));
		});
		return lista;
	}
}
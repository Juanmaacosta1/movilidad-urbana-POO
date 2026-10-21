package md.datos.dao.txt;

import java.util.ArrayList;
import java.util.List;

import md.datos.dao.ServicioDAO;
import md.datos.dto.ServicioDTO;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

/** Formato: nombre;tarifaBase;precioKm;precioMinuto;TipoVehiculo;CategoriaVehiculo;TipoServicio */
public class ServicioDaoTxt implements ServicioDAO {

	private String rutaArchivo;

	public ServicioDaoTxt(String rutaArchivo) {
		this.rutaArchivo = rutaArchivo;
	}

	@Override
	public List<ServicioDTO> obtenerTodos() throws Exception {
		List<ServicioDTO> lista = new ArrayList<>();
		LectorTxt.leer(rutaArchivo, 7, 7, campos -> lista.add(new ServicioDTO(campos[0],
				Double.parseDouble(campos[1]), Double.parseDouble(campos[2]), Double.parseDouble(campos[3]),
				TipoVehiculo.valueOf(campos[4].toUpperCase()),
				CategoriaVehiculo.valueOf(campos[5].toUpperCase()),
				TipoServicio.valueOf(campos[6].toUpperCase()))));
		return lista;
	}
}
package md.datos.dto;

import java.util.ArrayList;
import java.util.List;

public class UsuarioDTO {
	private String nombre;
	private String telefono;
	private String email;
	private String licenciaConducir;
	private List<String> patentes;

	public UsuarioDTO(String nombre, String telefono, String email, String licenciaConducir, List<String> patentes) {
		super();
		this.nombre = nombre;
		this.telefono = telefono;
		this.email = email;
		this.licenciaConducir = licenciaConducir;
		this.patentes = new ArrayList<>();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLicenciaConducir() {
		return licenciaConducir;
	}

	public void setLicenciaConducir(String licenciaConducir) {
		this.licenciaConducir = licenciaConducir;
	}

	public List<String> getPatentes() {
		return patentes;
	}

	public void setPatentes(List<String> patentes) {
		this.patentes = patentes;
	}

}

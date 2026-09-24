package modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Usuario {

	private String nombre;
	private String documento;
	private String telefono;
	private String email;
	private int totalCalificaciones;
	private List<Calificacion> calificaciones;

	public Usuario(String nombre, String documento, String telefono, String email) {
		super();
		this.nombre = nombre;
		this.documento = documento;
		this.telefono = telefono;
		this.email = email;
		this.totalCalificaciones=calificaciones.size();
		this.calificaciones= new ArrayList<Calificacion>();
	}
	
	 protected abstract int getLimiteCalificaciones();
	 
	 public void calificarViaje(Calificacion calificacion) {
	        if (calificacion == null) {
	            throw new IllegalArgumentException("La calificacion no puede ser nula");
	        }
	        if (calificaciones.size() >= getLimiteCalificaciones()) {
	            throw new IllegalStateException("Se alcanzo el limite de calificaciones para este usuario");
	        }
	        calificaciones.add(calificacion);
	        totalCalificaciones = calificaciones.size();
	    }
	 
	  public double promedioCalificacion() {
	        if (calificaciones.isEmpty()) {
	            return 0.0;
	        }
	        double suma = 0;
	        for (Calificacion c : calificaciones) {
	            suma += c.getValor();
	        }
	        return suma / calificaciones.size();
	    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
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

	public List<Calificacion> getCalificaciones() {
		return calificaciones;
	}

	public void setCalificaciones(List<Calificacion> calificaciones) {
		this.calificaciones = calificaciones;
	}

	public int getTotalCalificaciones() {
		return calificaciones.size();
	}

	@Override
	public int hashCode() {
		return Objects.hash(documento, telefono);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Usuario other = (Usuario) obj;
		return Objects.equals(documento, other.documento) && Objects.equals(telefono, other.telefono);
	}

	@Override
	public String toString() {
		return "Usuario [nombre=" + nombre + ", documento=" + documento + ", telefono=" + telefono + ", email=" + email
				+ ", totalCalificaciones=" + totalCalificaciones + ", calificaciones=" + calificaciones + "]";
	}
	
	
	
	
	
	

}

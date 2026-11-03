package md.datos.dto;

public class ConductorVehiculoDTO {

	private final String email;
	private final String patente;

	public ConductorVehiculoDTO(String email, String patente) {
		this.email = email;
		this.patente = patente;
	}

	public String getEmail() {
		return email;
	}

    public String getPatente() {
        return patente;
    }
}

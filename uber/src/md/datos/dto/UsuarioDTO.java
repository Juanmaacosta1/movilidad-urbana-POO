package md.datos.dto;

import java.util.List;

public class UsuarioDTO {

    private String nombre;
    private String telefono;
    private String email;
    private String licenciaConducir;
    private List<String> patentesVehiculos;

    public UsuarioDTO(
            String nombre,
            String telefono,
            String email,
            String licenciaConducir,
            List<String> patentesVehiculos) {

        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.licenciaConducir = licenciaConducir;
        this.patentesVehiculos = patentesVehiculos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getLicenciaConducir() {
        return licenciaConducir;
    }

    public List<String> getPatentesVehiculos() {
        return patentesVehiculos;
    }
}
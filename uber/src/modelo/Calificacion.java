package modelo;

public enum Calificacion {
    MALO(1),
    REGULAR(2),
    BUENO(3),
    MUY_BUENO(4),
    EXCELENTE(5);

    private final int valor;

    Calificacion(int valor) {
        this.valor = valor;
    }

    public int getValor() {
        return valor;
    }
}

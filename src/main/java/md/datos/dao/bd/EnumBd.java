package md.datos.dao.bd;

/** Traduce los códigos enteros persistidos en PostgreSQL a nombres de enums Java. */
public final class EnumBd {
    private EnumBd() {}

    public static <E extends Enum<E>> String nombre(Class<E> tipo, int codigo) {
        E[] constantes = tipo.getEnumConstants();
        if (codigo < 0 || codigo >= constantes.length) {
            throw new IllegalArgumentException("Código " + codigo + " inválido para " + tipo.getSimpleName());
        }
        return constantes[codigo].name();
    }
}

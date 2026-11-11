package md.ui.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Centraliza el idioma de la interfaz. Los textos se guardan en archivos
 * messages_XX.properties dentro de md/ui/i18n.
 */
public final class Messages {
    private static final String BASE_NAME = "md.ui.i18n.messages";
    private static Locale locale = new Locale("es");

    private Messages() { }

    public static Locale getLocale() {
        return locale;
    }

    public static void setLocale(Locale nuevoLocale) {
        if (nuevoLocale == null) return;
        String language = nuevoLocale.getLanguage();
        locale = ("en".equals(language)) ? Locale.ENGLISH : new Locale("es");
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public static String get(String key) {
        return getBundle().getString(key);
    }
}

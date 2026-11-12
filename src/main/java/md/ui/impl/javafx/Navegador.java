package md.ui.impl.javafx;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.modelo.RolUsuario;
import md.modelo.Usuario;
import md.ui.i18n.Messages;
import md.ui.impl.javafx.controller.MiniController;

/** Cambia la pantalla de la ventana principal: selección de usuario <-> aplicación. */
public final class Navegador {

    private static final String VISTA_SELECCION = "/ui/javafx/view/SeleccionUsuario.fxml";
    private static final String VISTA_PRINCIPAL = "/ui/javafx/view/MainWindowMd.fxml";

    private Navegador() {
    }

    public static void mostrarSeleccionUsuario(Stage ventana) throws IOException {
        FXMLLoader cargador = new FXMLLoader(recurso(VISTA_SELECCION), Messages.getBundle());
        Parent raiz = cargador.load();
        cambiarEscena(ventana, raiz, 520, 520, 420, 440);
        ventana.setTitle(Messages.getLocale().getLanguage().equals("en") ? "Digital Mobility · Select user" : "Movilidad Digital · Elegí tu usuario");
    }

    public static void mostrarPrincipal(Stage ventana, Usuario usuario, RolUsuario rol) throws IOException {
        FXMLLoader cargador = new FXMLLoader(recurso(VISTA_PRINCIPAL), Messages.getBundle());
        Parent raiz = cargador.load();
        MiniController controlador = cargador.getController();
        controlador.iniciarSesion(usuario, rol);
        cambiarEscena(ventana, raiz, 1200, 760, 900, 620);
        ventana.setTitle(Messages.get("ui.app.title") + " · " + usuario.getNombre());
    }

    private static URL recurso(String ruta) throws IOException {
        URL recurso = Navegador.class.getResource(ruta);
        if (recurso == null) {
            throw new IOException("No se encontró el recurso " + ruta);
        }
        return recurso;
    }

    private static void cambiarEscena(Stage ventana, Parent raiz, double ancho, double alto,
            double minimoAncho, double minimoAlto) {
        ventana.setMaximized(false);
        ventana.setMinWidth(minimoAncho);
        ventana.setMinHeight(minimoAlto);
        ventana.setScene(new Scene(raiz, ancho, alto));
        ventana.setWidth(ancho);
        ventana.setHeight(alto);
        ventana.centerOnScreen();
    }
}

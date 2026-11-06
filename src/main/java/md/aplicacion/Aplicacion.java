package md.aplicacion;

import md.ui.UILoader;
import md.ui.api.UIEngine;

/**
 * Punto de entrada común (mainClass del pom). No extiende Application: la
 * interfaz se elige con app.ui en config.properties y se inicia con ui.start().
 */
public class Aplicacion {

	public static void main(String[] args) {
		UIEngine ui = UILoader.createUIEngine();
		ui.start();
	}
}

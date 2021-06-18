package ar.com.imprenta_azul.barras;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

class JLogo extends JComponent
{
	final Image logo = ImageIO.read(Ventana.class.getResourceAsStream("azul.png"));
	final static int margen = 50;

	JLogo() throws IOException
	{
		setMinimumSize(new Dimension(100, 50));
		setPreferredSize(new Dimension(100, 50));
		setMaximumSize(new Dimension(logo.getWidth(null), logo.getHeight(null)));
	}

	@Override
	public void paint(Graphics gg) {
		super.paint(gg);
		Graphics2D g = (Graphics2D) gg;
		float prop = (float) logo.getWidth(null) / logo.getHeight(null);
		int w, h, x, y;
		int anchoComponente = getWidth();
		int altoComponente = getHeight();
		w = anchoComponente - margen;
		h = (int) (w / prop);
		if (h > (altoComponente - margen)) {
			h = altoComponente - margen;
			w = (int) (h * prop);
		}
		x = (anchoComponente - w) / 2;
		y = (altoComponente - h) / 2;
		g.drawImage(logo, x, y, w, h, null);
	}
}

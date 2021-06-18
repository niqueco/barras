/*
 *  Copyright (C) 2004, 2008 - Nicolás Lichtmaier <nick@reloco.com.ar>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ar.com.imprenta_azul.barras;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;

@SuppressWarnings("serial")
public class Barras implements Serializable, Cloneable, Transferable
{
	PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private long cai;
	private CUIT cuit;
	private int puntoDeVenta;
	/** El vencimiento. */
	private Date vencimiento;
	private TipoComprobante tipoComprobante;

	final private static DataFlavor[] flavors = new DataFlavor[] { DataFlavor.imageFlavor };
	final static private boolean[][] anchos = new boolean[][]
	{
		// 0
		new boolean[] { false, false, true, true, false},
		// 1
		new boolean[] { true, false, false, false, true},
		// 2
		new boolean[] { false, true, false, false, true},
		// 3
		new boolean[] { true, true, false, false, false},
		// 4
		new boolean[] { false, false, true, false, true},
		// 5
		new boolean[] { true, false, true, false, false},
		// 6
		new boolean[] { false, true, true, false, false},
		// 7
		new boolean[] { false, false, false, true, true},
		// 8
		new boolean[] { true, false, false, true, false},
		// 9
		new boolean[] { false, true, false, true, false},
	};
	/** Formato usado para generar los datos del código de barras.
	 *  El formato de la fecha de vto. es AAAAMMDD según la RG 1492,
	 *  que aclara a la RG 1361 que es citada por la nota externa 07/04
	 *  que amplía la información de la RG 1702.
	 */
	private static final MessageFormat fmt = new MessageFormat("{0} {1,number,00} {2,number,0000} {3,number,00000000000000} {4,date,yyyyMMdd} ");
	
	/** El font usado para el texto que va debajo del código.
	 */
	private static final Font font = new Font("Dialog", Font.PLAIN, 17);
	
	/** El alto del código de barras.
	 */
	private static final int ALTO = 45;
	
	public long getCAI()
	{
		return cai;
	}
	public void setCAI(long cai)
	{
		long x = this.cai;
		this.cai = cai;
		pcs.firePropertyChange("CAI", x, cai);
	}
	public CUIT getCUIT()
	{
		return cuit;
	}
	public void setCUIT(CUIT cuit)
	{
		CUIT x = this.cuit;
		this.cuit = cuit;
		pcs.firePropertyChange("CUIT", x, cuit);
	}
	public Date getVencimiento()
	{
		return vencimiento;
	}
	public void setVencimiento(Date fv)
	{
		Date v = vencimiento;
		vencimiento = fv;
		pcs.firePropertyChange("vencimiento", v, vencimiento);
	}
	public int getPuntoDeVenta()
	{
		return puntoDeVenta;
	}
	public void setPuntoDeVenta(int pv)
	{
		int p = puntoDeVenta;
		puntoDeVenta = pv;
		pcs.firePropertyChange("puntoDeVenta",p, pv);
	}
	public TipoComprobante getTipoComprobante()
	{
		return tipoComprobante;
	}
	public void setTipoComprobante(TipoComprobante tipoComprobante)
	{
		TipoComprobante t = this.tipoComprobante;
		this.tipoComprobante = tipoComprobante;
		pcs.firePropertyChange("tipoComprobante", t, tipoComprobante);
	}
	public void addPropertyChangeListener(PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(l);
	}
	public void addPropertyChangeListener(String prop, PropertyChangeListener l)
	{
		pcs.addPropertyChangeListener(prop, l);
	}
	public void removePropertyChangeListener(PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(l);
	}
	public void removePropertyChangeListener(String prop, PropertyChangeListener l)
	{
		pcs.removePropertyChangeListener(prop, l);
	}
	@Override
	public Object clone()
	{
		try
		{
			Barras b = (Barras)super.clone();
			b.pcs = new PropertyChangeSupport(b);
			return b;
		} catch (CloneNotSupportedException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/** Dice si hay datos válidos para dibujar un código de barras.
	 * 
	 * @return si los datos son válidos.
	 */
	boolean valido()
	{
		return cuit != null && tipoComprobante != null && puntoDeVenta>0 && vencimiento!=null && cai > 9999999999999L;
	}
	
	void pintar(Graphics2D g)
	{
		if(!valido())
			return;
		final String s = dameString();
		//System.out.println("El string es " + s);
		final GeneralPath path = new GeneralPath();
		class Dibujador
		{
			float x = 0;
			
			void barra(float ancho)
			{
				path.moveTo(x,0);
				path.lineTo(x, ALTO);
				x+=ancho;
				path.lineTo(x, ALTO);
				path.lineTo(x, 0);
				path.closePath();				
			}
			
			void espacio(float ancho)
			{
				x+=ancho;
			}
		}
		Dibujador d = new Dibujador();
		final float largoFino = 2f;
		final float largoAncho = largoFino*2f;
		//g.setFont(Font.decode("sans-serif 8"));
		g.setColor(Color.BLACK);
		
		// primero la secuencia de comienzo
		for(int i=0;i<2;i++)
		{
			d.barra(largoFino);
			d.espacio(largoFino);
		}
		
		int l = s.length();
		for(int i=0 ; i < l-1 ; i++)
		{
			while(Character.isSpaceChar(s.charAt(i)))
				i++;
			int a = s.charAt(i)-'0';
			i++;
			while(i<l && Character.isSpaceChar(s.charAt(i)))
				i++;
			if(i==l)
				break;
			int b = s.charAt(i)-'0';
			
			boolean[] p = anchos[a];
			boolean[] q = anchos[b];
			
			for(int j = 0 ; j<5 ; j++)
			{
				d.barra(p[j]?largoAncho:largoFino);
				d.espacio(q[j]?largoAncho:largoFino);
			}
		}
		
		// Y ahora, el final
		d.barra(largoAncho);
		d.espacio(largoFino);
		d.barra(largoFino);
		
		g.fill(path);
		g.setFont(font);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		FontMetrics fontMetrics = g.getFontMetrics();
		g.drawString(s, (d.x-fontMetrics.stringWidth(s))/2f, ALTO+fontMetrics.getHeight());
	}

	private String dameString()
	{
		String s = fmt.format(new Object[] {
				cuit.sinGuiones()
				, tipoComprobante.getCodigo()
				, puntoDeVenta
				, getCAI()
				, vencimiento
				});
		return s + dameDigitoVerificador(s);
	}

	static int dameDigitoVerificador(CharSequence s)
	{
		int indice, i;
		int n = s.length();
		int sumaPares=0, sumaImpares=0;
		for(indice=0, i=0 ; i<n ; i++)
		{
			char ch = s.charAt(i);
			if(Character.isSpaceChar(ch))
				continue;
			int x = ch - '0';
			// La primer posición es impar, porque cuentan desde 1.
			if((indice%2) == 1)
				sumaPares+=x;
			else
				sumaImpares+=x;
			indice++;
		}
		return (10 - ((sumaImpares*3+sumaPares)%10))%10;
	}
	public DataFlavor[] getTransferDataFlavors()
	{
		return flavors;
	}

	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		for(DataFlavor element : flavors)
			if(flavor.equals(element))
				return true;
		return false;
	}

	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
	{
		if(flavor.equals(DataFlavor.imageFlavor))
			return dameImagen();
		throw new UnsupportedFlavorException(flavor);
	}
	BufferedImage dameImagen()
	{
		int w = 605;
		int h = 80;
		BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g = i.createGraphics();
		//g.scale(2,2);
		g.setBackground(Color.WHITE);
		g.clearRect(0,0,w,h);
		pintar(g);
		return i;
	}
}

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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

@SuppressWarnings("serial")
final class AccionCopiar extends AbstractAction
{
	private final PanelDeResultado resultado;

	AccionCopiar(PanelDeResultado p)
	{
		super("Copiar");
		resultado = p;
		putValue(Action.LONG_DESCRIPTION, "Copia el código de barras al portapapeles.");
		putValue(Action.SMALL_ICON, new ImageIcon(AccionCopiar.class.getResource("Copy24.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		resultado.copiar();
	}
}

@SuppressWarnings("serial")
final class AccionGrabar extends AbstractAction
{
	private final PanelDeResultado resultado;

	AccionGrabar(PanelDeResultado p)
	{
		super("Grabar como...");
		resultado = p;
		putValue(Action.LONG_DESCRIPTION, "Graba el código de barras en un archivo.");
		putValue(Action.SMALL_ICON, new ImageIcon(AccionGrabar.class.getResource("SaveAs24.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		resultado.grabar();
	}
}

@SuppressWarnings("serial")
final class PanelDeResultado extends JPanel implements ClipboardOwner, PropertyChangeListener
{
	final private Barras barras;
	final AccionCopiar accionCopiar;
	final AccionGrabar accionGrabar;
	public PanelDeResultado(Barras b)
	{
		barras = b;
		setMinimumSize(new Dimension(605, 100));
		//setBorder(BorderFactory.createEtchedBorder());
		setToolTipText("Arrastre desde aquí");
		accionCopiar = new AccionCopiar(this);
		accionGrabar = new AccionGrabar(this);
		accionCopiar.setEnabled(false);
		accionGrabar.setEnabled(false);
		DragSource ds = DragSource.getDefaultDragSource();
		ds.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY, new DragGestureListener() {

			public void dragGestureRecognized(DragGestureEvent dge)
			{
				dge.startDrag(null, (Transferable) barras.clone());
			}
			
		});
		barras.addPropertyChangeListener(this);
		setBackground(Color.WHITE);
		setSize(605, 100);
		setPreferredSize(new Dimension(605,100));
		setOpaque(false);

		JPopupMenu menu = new JPopupMenu();
		menu.add(new JMenuItem(accionCopiar));
		menu.add(new JMenuItem(accionGrabar));
		setComponentPopupMenu(menu);
	}
	
	/**
	 * 
	 */
	public void grabar()
	{
		try
		{
			JFileChooser fc = new JFileChooser();
			fc.setFileFilter(new FileFilter() {

				@Override
				public boolean accept(File f)
				{
					return f.getName().endsWith(".png");
				}

				@Override
				public String getDescription()
				{
					return "Imágenes PNG";
				}
				
			});
			int r = fc.showSaveDialog(this);
			if(r!=JFileChooser.APPROVE_OPTION)
				return;
			File archivo = fc.getSelectedFile();
			if(archivo.exists())
			{
				r = JOptionPane.showConfirmDialog(this, "¿Sobreescribir "+archivo+'?');
				if(r!=JOptionPane.OK_OPTION)
					return;
			}

			writeImageTo(new FileOutputStream(archivo));
		} catch(AccessControlException e)
		{
			grabarWebStart();
		} catch (Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error grabando imagen", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void grabarWebStart()
	{
		try
		{
			Object fss = getWebStartService("javax.jnlp.FileSaveService");
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Class<?> cl = loader.loadClass("javax.jnlp.FileSaveService");
			String[] exts = new String[] { "png" };
			Method saveFileDialog = cl.getMethod("saveFileDialog", String.class, exts.getClass(), InputStream.class, String.class);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			writeImageTo(baos);
			saveFileDialog.invoke(fss, null, exts, new ByteArrayInputStream(baos.toByteArray()), "barras");
		} catch(Exception e)
		{
			JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Error grabando imagen", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void writeImageTo(OutputStream output) throws IOException
	{
		Iterator<ImageWriter> it = ImageIO.getImageWritersByMIMEType("image/png");
		IIOImage iio = new IIOImage(barras.dameImagen(), null, null);
		ImageWriter w = it.next();
		ImageOutputStream out = ImageIO.createImageOutputStream(output);
		w.setOutput(out);
		w.write(iio);
		out.close();
	}

	/** Copia al clipboard el código de barras.
	 */
	public void copiar()
	{
		Transferable t = (Transferable) barras.clone();
		try {
			getToolkit().getSystemClipboard().setContents(t, this);
		} catch(AccessControlException e)
		{
			try
			{
				Object cs = getWebStartService("javax.jnlp.ClipboardService");
				ClassLoader loader = Thread.currentThread().getContextClassLoader();
				Class<?> csClass = loader.loadClass("javax.jnlp.ClipboardService");
				Method setContent = csClass.getMethod("setContents", Transferable.class);
				setContent.invoke(cs, t);
			} catch (Exception e2)
			{
				e2.printStackTrace();
				JOptionPane.showMessageDialog(this, e2, "Error copiando al clipboard", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private Object getWebStartService(String className)
			throws ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> sm = loader.loadClass("javax.jnlp.ServiceManager");
		Method lookup = sm.getMethod("lookup", String.class);
		return lookup.invoke(null, className);
	}

	/*
	@Override
	public void paint(Graphics gg)
	{
		super.paint(gg);
		barras.pintar((Graphics2D)gg);
	}
	*/
	
	@Override
	protected void paintComponent(Graphics gg)
	{
		Graphics2D g = (Graphics2D)gg;
		AffineTransform transform = g.getTransform();
		//g.scale(2, 2);
		barras.pintar(g);
		g.setTransform(transform);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents)
	{
	}

	public void propertyChange(PropertyChangeEvent evt)
	{
		if(evt.getSource() == barras)
		{
			boolean valido = barras.valido();
			accionCopiar.setEnabled(valido);
			accionGrabar.setEnabled(valido);
			repaint();
		}
	}
}

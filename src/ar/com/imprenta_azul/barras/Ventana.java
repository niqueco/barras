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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class Ventana extends JFrame
{
	final Barras barras = new Barras();
	// static Preferences prefs = Preferences.userNodeForPackage(Ventana.class);;	
	
	private PanelDeResultado resultado;
	
	Ventana()
	{
		super("Generador de Código de Barras");
		/*
		String c = prefs.get("cuit", null);
		if(c!=null)
		{
			try {
				barras.setCUIT(new CUIT(c));
			} catch(RuntimeException e)
			{
				e.printStackTrace();
			}
		}*/
		Container cp = getContentPane();
		cp.setLayout(new BoxLayout(cp, BoxLayout.PAGE_AXIS));
		Box b = new Box(BoxLayout.LINE_AXIS);
		JComponent panelFormulario = damePanelFormulario();
		panelFormulario.setMaximumSize(panelFormulario.getMinimumSize());
		b.add(panelFormulario);
		try
		{
			b.add(new JLogo());
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		cp.add(b);
		JComponent panelResultado = damePanelResultado();
		panelResultado.setAlignmentX(Component.LEFT_ALIGNMENT);
		cp.add(panelResultado);
		/*
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Programa");
		menu.add(new AbstractAction("Configurar...") {

			public void actionPerformed(ActionEvent e)
			{
				Object i = JOptionPane.showInputDialog(Ventana.this
						, "C.U.I.T. de la imprenta"
						, "Configurar"
						, JOptionPane.QUESTION_MESSAGE
						, null
						, null
						, barras.getCUIT());
				if(i==null)
					return;
				try {
					barras.setCUIT(new CUIT(i.toString()));
					prefs.put("cuit", barras.getCUIT().toString());
				} catch(RuntimeException ee)
				{
					JOptionPane.showMessageDialog(Ventana.this, ee.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		menu.addSeparator();
		menu.add(new AbstractAction("Salir") {
			{
				this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
			}

			public void actionPerformed(ActionEvent e)
			{
				System.exit(0);
			}
			
		});
		menubar.add(menu);
		setJMenuBar(menubar);
		*/
		JLabel label = new JLabel(" © 2004 Imprenta Azul");
		label.setEnabled(false);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		cp.add(label);
		try
		{
			Image icono = getToolkit().createImage(Ventana.class.getResource("barras.png"));
			setIconImage(icono);
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
		pack();
		setLocationByPlatform(true);
	}

	private JComponent damePanelResultado()
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Resultado"));
		resultado = new PanelDeResultado(barras);
		JScrollPane sp = new JScrollPane(resultado);
		//sp.add(resultado);
		p.add(sp, BorderLayout.CENTER);
		JToolBar botonera = new JToolBar();
		botonera.setFloatable(false);
		agregarAccion(botonera, resultado.accionCopiar);
		agregarAccion(botonera, resultado.accionGrabar);
		botonera.add(Box.createGlue());
		JButton info = new JButton(new ImageIcon(Ventana.class.getResource("About24.gif")));
		info.addActionListener(e -> JOptionPane.showMessageDialog(Ventana.this
				, "<html>Hecho por <b>Nicolás Lichtmaier</b> en diciembre de 2004.</html>"
				, "Acerca..."
				, JOptionPane.INFORMATION_MESSAGE));
		botonera.add(info);
		p.add(botonera, BorderLayout.PAGE_START);
		return p;
	}

	private void agregarAccion(JToolBar botonera, Action accion)
	{
		JButton b;
		b = new JButton(accion);
		b.setToolTipText((String)accion.getValue(Action.LONG_DESCRIPTION));
		b.setText(null);
		botonera.add(b);
	}

	private JComponent damePanelFormulario()
	{
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createTitledBorder("Datos del comprobante"));
		p.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		int y = 0;

		JLabel label;

		label = new JLabel("C.U.I.T.");
		label.setDisplayedMnemonic('C');
		final JTextField campoCuit = new JTextField(13);
		campoRestringido(campoCuit, "0123456789-");
		// Si pongo esto anda todo bien! (!!!!)
		campoCuit.setMinimumSize(campoCuit.getPreferredSize());
		final Color cb = campoCuit.getForeground();
		campoCuit.setForeground(Color.RED);
		//final Border bordeDefault = campoCuit.getBorder();
		//campoCuit.setBorder(BorderFactory.createLineBorder(Color.RED));
		agregarCampo(p, c, y++, label, campoCuit);
		campoCuit.getDocument().addDocumentListener(new DocumentListener() {
			private void cambio()
			{
				try {
					barras.setCUIT(new CUIT(campoCuit.getText()));
					//campoCuit.setBorder(bordeDefault);
					campoCuit.setForeground(cb);
				} catch(RuntimeException e)
				{
					//campoCuit.setBorder(BorderFactory.createLineBorder(Color.RED));
					campoCuit.setForeground(Color.RED);
					if(barras.getCUIT() != null)
						barras.setCUIT(null);
				}
			}

			public void insertUpdate(DocumentEvent e)
			{
				cambio();
			}

			public void removeUpdate(DocumentEvent e)
			{
				cambio();
			}

			public void changedUpdate(DocumentEvent e)
			{
				cambio();
			}
			
		});

		label = new JLabel("Tipo de comprobante");
		label.setDisplayedMnemonic('T');
		ComboBoxModel<TipoComprobante> tcm = new DefaultComboBoxModel<>(TipoComprobante.values());
		JComboBox<TipoComprobante> lista = new JComboBox<>(tcm);
		agregarCampo(p, c, y++, label, lista);
		lista.addItemListener(e -> {
			TipoComprobante t = (TipoComprobante) e.getItemSelectable().getSelectedObjects()[0];
			barras.setTipoComprobante(t);
		});
		barras.setTipoComprobante((TipoComprobante)lista.getSelectedItem());

		label = new JLabel("Punto de venta");
		label.setDisplayedMnemonic('P');
		SpinnerNumberModel pvm = new SpinnerNumberModel(1, 1, 9999, 1);
		pvm.addChangeListener(e -> barras.setPuntoDeVenta(((Number)((SpinnerModel)e.getSource()).getValue()).intValue()));
		JSpinner pv = new JSpinner(pvm);
		agregarCampo(p, c, y++, label, pv);
		barras.setPuntoDeVenta(1);

		label = new JLabel("C.A.I.");
		label.setDisplayedMnemonic('A');
		final JTextField campoCAI = new JTextField(14);
		campoRestringido(campoCAI, "0123456789");
		// Si pongo esto anda todo bien! (!!!!)
		campoCAI.setMinimumSize(campoCAI.getPreferredSize());
		campoCAI.getDocument().addDocumentListener(new DocumentListener() {
			
			private void cambio()
			{
				try {
					barras.setCAI(Long.parseLong(campoCAI.getText()));
				} catch(RuntimeException ignored)
				{ }
			}

			public void insertUpdate(DocumentEvent e)
			{
				cambio();
			}

			public void removeUpdate(DocumentEvent e)
			{
				cambio();
			}

			public void changedUpdate(DocumentEvent e)
			{
				cambio();
			}
			
		});
		agregarCampo(p, c, y++, label, campoCAI);

		label = new JLabel("Fecha de vto.");
		label.setDisplayedMnemonic('v');
		Date hoy = new Date();
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, 15);
		Calendar calFut = new GregorianCalendar();
		calFut.add(Calendar.YEAR, 5);
		SpinnerDateModel fvm = new SpinnerDateModel(cal.getTime(), hoy, calFut.getTime(), Calendar.DAY_OF_MONTH);
		barras.setVencimiento(cal.getTime());
		fvm.addChangeListener(e -> barras.setVencimiento((Date)((SpinnerModel)e.getSource()).getValue()));
		JSpinner sp = new JSpinner(fvm);
		SimpleDateFormat df = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT);
		sp.setEditor(new JSpinner.DateEditor(sp, df.toPattern()));
		agregarCampo(p, c, y++, label, sp);
		
		return p;
	}

	private void campoRestringido(final JTextField campo, final String sePuede)
	{
		campo.addKeyListener(new KeyListener() {

			public void keyTyped(KeyEvent e)
			{
				char ch = e.getKeyChar();
				if((campo.getText().length()>=campo.getColumns() && campo.getSelectedText()==null) || ((sePuede.indexOf(ch)==-1))
						&& (!Character.isISOControl(ch)))
				{
					e.consume();
					getToolkit().beep();
				}
			}

			public void keyPressed(KeyEvent e)
			{
			}

			public void keyReleased(KeyEvent e)
			{
			}
			
		});
	}

	private void agregarCampo(JPanel p, GridBagConstraints c, int y, JLabel label, JComponent campo)
	{
		c.insets=new Insets(2,2,2,2);
		label.setLabelFor(campo);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = y;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		p.add(label, c);
		
		c.gridx = 1;
		c.gridy = y;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 1;
		p.add(campo, c);
	}

	public static void main(String[] args)
	{
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		Ventana v = new Ventana();
		v.setVisible(true);
		v.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*
		FlavorMap fm = SystemFlavorMap.getDefaultFlavorMap();
		Map m = fm.getFlavorsForNatives(null);
		Iterator i = m.entrySet().iterator();
		while(i.hasNext())
		{
			Map.Entry e = (Map.Entry)i.next();
			System.out.println(e.getKey() + " -> "+ e.getValue());
		}*/
	}
}

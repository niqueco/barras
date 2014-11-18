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

/** Representa un C.U.I.T.
 *  Esta clase representa a la famosa Clave Única de Identificación Tributaria.
 *  
 *  @author Nicolás Lichtmaier
 */
public class CUIT implements java.io.Serializable, Cloneable, Comparable<CUIT>
{
	private static final long serialVersionUID = 1L;

	/** El CUIT sin guiones.
	 *  @serial string con el cuit sin guiones
	 */
	private final String cuit;
	
	private static final int[] refvector
		= new int[] { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2, 1 };

	/** Construye un C.U.I.T.
	 *  @param c un C.U.I.T. como string, con o sin guiones.
	 */
	public CUIT(CharSequence c)
	{
		cuit=sacaGuiones(c);

		if (cuit.length() != 11)
			throw new ExcepcionCUITInvalido("El C.U.I.T. " + c
					+ " tiene longitud inválida.");
		try {
			if (Long.parseLong(cuit) <= 20000000000L)
				throw new ExcepcionCUITInvalido("El C.U.I.T. "
						+ c
						+ " es un número incorrecto.");
		} catch(NumberFormatException e)
		{
			throw new ExcepcionCUITInvalido("El C.U.I.T. " + c
					+ " no es numérico.");
		}

		/* Valida módulo 11 */
		int suma=0;
		for (int i=0 ; i < 11 ; i++)
			suma += (cuit.charAt(i) - '0') * refvector[i];

		if((suma % 11) != 0)
			throw new ExcepcionCUITInvalido("El C.U.I.T. " + c
					+ " es inválido");
	}

	/** Da una representación string del C.U.I.T.
	 *  El valor devuelto es el C.U.I.T. con los guiones donde deben
	 *  estar, listo para mostrar en pantalla.
	 */
	@Override
	public String toString()
	{
		return formateaCUIT();
	}
	
	/** Obtiene el C.U.I.T. sin guiones.
	 *  @return el C.U.I.T. sin guiones.
	 */
	public String sinGuiones()
	{
		return cuit;
	}
	
	/** Dice si el objeto representa al mismo C.U.I.T.
	 *  La comparación funciona solamente contra otro objeto C.U.I.T.,
	 *  no contra un string.
	 *  @param o
	 *  	el otro objeto.
	 *  @return
	 *  	si el C.U.I.T. es el mismo.
	 */
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof CUIT))
			return false;
		return cuit.equals( ((CUIT)o).cuit );
	}

	@Override
	public int hashCode()
	{
		return cuit.hashCode();
	}

	public int compareTo(CUIT o)
	{
		return cuit.compareTo(o.cuit);
	}

	/** Elimina guiones y espacios del string pasado.
	 * 
	 * @param s el string
	 * @return un nuevo string
	 */
	private static final String sacaGuiones(CharSequence s)
	{
		int largo = s.length();
		StringBuilder r = new StringBuilder(largo);
		for (int i=0 ; i<largo ; i++)
		{
			char ch = s.charAt(i);
			if (ch != '-' && !Character.isSpaceChar(ch))
				r.append(ch);
		}
		return r.toString();
	}

	/** Le pone guiones al cuit.
	 * 
	 * @return el cuit con guiones
	 */
	private String formateaCUIT()
	{
		//Me fijo si tiene guiones;
		if (cuit.indexOf('-') != -1)
			return cuit;
		int largo = cuit.length();
		return cuit.substring(0,2)
			+ '-' + cuit.substring(2, largo-1)
			+ '-' + cuit.substring(largo-1);
	}

	/** El C.U.I.T. es inválido.
	 */
	public static class ExcepcionCUITInvalido
		extends IllegalArgumentException
	{
		private static final long serialVersionUID = 1L;

		ExcepcionCUITInvalido(String m)
		{
			super(m);
		}
	}
}
    
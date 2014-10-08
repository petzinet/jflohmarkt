/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class EmptyIntegerFormatter implements Formatter {

	@Override
	public String format(Object o) {
		if (o instanceof Integer) {
			int i = (Integer) o;
			if (i != 0) {
				return String.valueOf(i);
			}
		}
		return "";
	}

}

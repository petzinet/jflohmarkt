/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class BooleanFormatter implements Formatter {

	private final String trueText;
	private final String falseText;
	
	public BooleanFormatter(String trueText, String falseText) {
		this.trueText = trueText;
		this.falseText = falseText;
	}
	
	@Override
	public String format(Object o) {
		if (o instanceof Boolean)
			return ((Boolean) o) ? trueText : falseText;
		return "";
	}

}

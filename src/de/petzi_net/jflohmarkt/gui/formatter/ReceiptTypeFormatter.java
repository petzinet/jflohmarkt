/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class ReceiptTypeFormatter implements Formatter {

	@Override
	public String format(Object o) {
		if (o instanceof Integer) {
			int type = (Integer) o;
			switch (type) {
			case Receipt.TYPE_SALE:
				return "Verkauf";
			case Receipt.TYPE_PICKUP:
				return "Leerung";
			case Receipt.TYPE_DROPOFF:
				return "Einlage";
			case Receipt.TYPE_INVENTORY:
				return "Bestand";
			}
		}
		return "";
	}

}

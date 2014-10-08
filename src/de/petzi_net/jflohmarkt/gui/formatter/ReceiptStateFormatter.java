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
public class ReceiptStateFormatter implements Formatter {

	@Override
	public String format(Object o) {
		if (o instanceof Integer) {
			int state = (Integer) o;
			switch (state) {
			case Receipt.STATE_ACTIVE:
				return "Aktiv";
			case Receipt.STATE_FINISHED:
				return "Abgeschlossen";
			case Receipt.STATE_ABORTED:
				return "Abgebrochen";
			}
		}
		return "";
	}

}

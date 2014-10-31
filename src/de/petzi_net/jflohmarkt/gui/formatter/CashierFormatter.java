/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class CashierFormatter implements Formatter {

	private final Map<Long, Integer> cashiers = new HashMap<Long, Integer>();
	
	public CashierFormatter(Event event) throws RemoteException {
		if (event != null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator cashiers = service.createList(Cashier.class);
			cashiers.addFilter("event=?", event.getID());
			while (cashiers.hasNext()) {
				Cashier cashier = (Cashier) cashiers.next();
				this.cashiers.put(Long.valueOf(cashier.getID()), cashier.getNumber());
			}
		}
	}
	
	@Override
	public String format(Object o) {
		if (o instanceof Long) {
			Integer cashier = cashiers.get((Long) o);
			if (cashier != null) {
				return String.valueOf(cashier);
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

}

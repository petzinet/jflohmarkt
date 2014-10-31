/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class SellerFormatter implements Formatter {

	private final Map<Long, Integer> sellers = new HashMap<Long, Integer>();
	
	public SellerFormatter(Event event) throws RemoteException {
		if (event != null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator sellers = service.createList(Seller.class);
			sellers.addFilter("event=?", event.getID());
			while (sellers.hasNext()) {
				Seller seller = (Seller) sellers.next();
				this.sellers.put(Long.valueOf(seller.getID()), seller.getNumber());
			}
		}
	}
	
	@Override
	public String format(Object o) {
		if (o instanceof Long) {
			Integer seller = sellers.get((Long) o);
			if (seller != null) {
				return String.valueOf(seller);
			} else {
				return "";
			}
		} else {
			return "";
		}
	}

}

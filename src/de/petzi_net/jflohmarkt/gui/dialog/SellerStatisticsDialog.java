/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.dialog;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SellerStatisticsDialog extends AbstractDialog<Void> {

	private Event event;
	
	public SellerStatisticsDialog(int position) {
		super(position);
		setTitle("Verkäuferstatistik");
		setSize(400, 200);
	}

	@Override
	protected void paint(Composite parent) throws Exception {
		SellerStatistics sellerStatistics = getStatistics();
		Container statistics = new SimpleContainer(parent);
		statistics.addText("" + sellerStatistics.getSimilarNames() + " ähnliche Namen", false);
		statistics.addText("" + sellerStatistics.getSimilarAddresses() + " ähnliche Adressen", false);
		statistics.addText("" + sellerStatistics.getSimilarPhonenumbers() + " ähnliche Telefonnummern", false);
		statistics.addText("" + sellerStatistics.getSimilarEmailaddresses() + " ähnliche Emailadressen", false);
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Schließen", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				close();
			}
			
		}, null, true);
		buttons.paint(parent);
	}

	@Override
	protected Void getData() throws Exception {
		return null;
	}
	
	public Event getEvent() throws RemoteException {
		if (event == null) {
			String currentEventId = JFlohmarktPlugin.getSettings().getString("event.current", "");
			if (!currentEventId.isEmpty()) {
				DBService service = JFlohmarktPlugin.getDBService();
				event = (Event) service.createObject(Event.class, currentEventId);
			}
		}
		return event;
	}
	
	public static class SellerStatistics {
		
		private final long similarNames;
		private final long similarAddresses;
		private final long similarPhonenumbers;
		private final long similarEmailaddresses;
		
		public SellerStatistics(long similarNames, long similarAddresses, long similarPhonenumbers, long similarEmailaddresses) {
			this.similarNames = similarNames;
			this.similarAddresses = similarAddresses;
			this.similarPhonenumbers = similarPhonenumbers;
			this.similarEmailaddresses = similarEmailaddresses;
		}

		public long getSimilarNames() {
			return similarNames;
		}

		public long getSimilarAddresses() {
			return similarAddresses;
		}

		public long getSimilarPhonenumbers() {
			return similarPhonenumbers;
		}

		public long getSimilarEmailaddresses() {
			return similarEmailaddresses;
		}
		
	}
	
	private static void addStatisticValue(Map<String, Long> map, boolean onlyDigits, String... strings) {
		StringBuffer buffer = new StringBuffer();
		for (String string : strings) {
			if (string != null)
				buffer.append(string);
		}
		String index;
		if (onlyDigits)
			index = buffer.toString().replaceAll("\\D", "");
		else
			index = buffer.toString().replaceAll("\\W", "").toUpperCase();
		Long found = map.get(index);
		if (found == null)
			map.put(index, 1L);
		else
			map.put(index, found + 1);
	}
	
	private static long calcStatisticResult(Map<String, Long> map) {
		long similar = 0;
		for (Map.Entry<String, Long> entry : map.entrySet()) {
			if (entry.getValue() > 1)
				similar += entry.getValue() - 1;
		}
		return similar;
	}
	
	public SellerStatistics getStatistics() throws RemoteException {
		Map<String, Long> names = new HashMap<String, Long>();
		Map<String, Long> addresses = new HashMap<String, Long>();
		Map<String, Long> phonenumbers = new HashMap<String, Long>();
		Map<String, Long> emailaddresses = new HashMap<String, Long>();
		
		if (getEvent() != null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator sellers = service.createList(Seller.class);
			sellers.addFilter("event=?", getEvent().getID());
			
			while (sellers.hasNext()) {
				Seller seller = (Seller) sellers.next();
				
				addStatisticValue(names, false, seller.getName(), seller.getGivenName());
				addStatisticValue(addresses, false, seller.getStreet(), seller.getZipCode(), seller.getCity());
				addStatisticValue(phonenumbers, true, seller.getPhone());
				addStatisticValue(emailaddresses, false, seller.getEmail());
			}
		}
		
		long similarNames = calcStatisticResult(names);
		long similarAddresses = calcStatisticResult(addresses);
		long similarPhonenumbers = calcStatisticResult(phonenumbers);
		long similarEmailaddresses = calcStatisticResult(emailaddresses);
		
		return new SellerStatistics(similarNames, similarAddresses, similarPhonenumbers, similarEmailaddresses);
	}

}

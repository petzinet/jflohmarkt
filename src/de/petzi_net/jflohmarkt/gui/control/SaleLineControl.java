/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.rmi.RemoteException;
import java.text.MessageFormat;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SaleLineControl extends AbstractControl {
	
	private Event event;
	private POS pos;
	private Receipt receipt;
	private ReceiptLine receiptLine;
	
	private SelectInput sellerSelection;

	public SaleLineControl(AbstractView view) {
		super(view);
	}
	
	public Event getEvent() throws RemoteException {
		if (event == null) {
			POS pos = getPOS();
			if (pos != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				event = (Event) service.createObject(Event.class, String.valueOf(pos.getEvent()));
			}
		}
		return event;
	}
	
	public POS getPOS() throws RemoteException {
		if (pos == null) {
			Receipt receipt = getReceipt();
			if (receipt != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				pos = (POS) service.createObject(POS.class, String.valueOf(receipt.getPOS()));
			}
		}
		return pos;
	}
	
	public Receipt getReceipt() throws RemoteException {
		if (receipt == null) {
			ReceiptLine receiptLine = getReceiptLine();
			if (receiptLine != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				receipt = (Receipt) service.createObject(Receipt.class, String.valueOf(receiptLine.getReceipt()));
			}
		}
		return receipt;
	}
	
	public ReceiptLine getReceiptLine() {
		if (receiptLine == null)
			receiptLine = (ReceiptLine) getCurrentObject();
		return receiptLine;
	}
	
	public Input getSellerSelection() throws RemoteException {
		if (sellerSelection == null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator sellers = service.createList(Seller.class);
			sellers.addFilter("event=?", getEvent().getID());
			Seller seller = (Seller) service.createObject(Seller.class, String.valueOf(getReceiptLine().getSeller()));
			sellerSelection = new SelectInput(sellers, seller);
			sellerSelection.setName("Verkäufer");
			sellerSelection.setAttribute("number");
			sellerSelection.setMandatory(true);
		}
		return sellerSelection;
	}
	
	public void handleStore() {
		try {
			if (getEvent() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Veranstaltung auswählen!", StatusBarMessage.TYPE_ERROR));
			} else if (getPOS() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Kasse auswählen!", StatusBarMessage.TYPE_ERROR));
			} else if (getReceipt() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst einen Beleg auswählen!", StatusBarMessage.TYPE_ERROR));
			} else {
				ReceiptLine receiptLine = getReceiptLine();
				receiptLine.setSeller(Long.valueOf(((Seller) getSellerSelection().getValue()).getID()));
				
				try {
					receiptLine.store();
					Application.getMessagingFactory().sendMessage(new StatusBarMessage("Belegzeile gespeichert", StatusBarMessage.TYPE_SUCCESS));
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing receipt line", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern der Belegzeile: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}

}

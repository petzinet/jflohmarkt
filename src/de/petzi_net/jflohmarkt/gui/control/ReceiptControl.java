/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.rmi.RemoteException;
import java.text.MessageFormat;

import org.eclipse.swt.widgets.TableItem;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.SaleLineAction;
import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.BooleanFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.SellerFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class ReceiptControl extends AbstractControl {
	
	private TablePart receiptLineTable;
	
	private Event event;
	private POS pos;
	private Receipt receipt;
	
	private SelectInput cashierSelection;

	public ReceiptControl(AbstractView view) {
		super(view);
	}
	
	public TablePart getReceiptLineTable() throws RemoteException {
		if (receiptLineTable == null) {
			DBIterator receiptLines = null;
			DBService service = JFlohmarktPlugin.getDBService();
			receiptLines = service.createList(ReceiptLine.class);
			receiptLines.addFilter("receipt=?", getReceipt().getID());
			
			TableFormatter tableFormatter = new TableFormatter() {
				
				@Override
				public void format(TableItem item) {
					if (item != null && item.getData() instanceof ReceiptLine) {
						ReceiptLine line = (ReceiptLine) item.getData();
						try {
							if (!line.isValid()) {
								item.setForeground(Color.COMMENT.getSWTColor());
							}
						} catch (RemoteException e) {
							// so what !
						}
					}
				}
				
			};
			
			receiptLineTable = new TablePart(receiptLines, new SaleLineAction());
			receiptLineTable.addColumn("Zeile", "line");
			receiptLineTable.addColumn("Verkäufer", "seller", new SellerFormatter(getEvent()));
			receiptLineTable.addColumn("Anzahl", "quantity");
			receiptLineTable.addColumn("Wert", "value", new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS));
			receiptLineTable.addColumn("Gültig", "valid", new BooleanFormatter("ja", "nein"));
			receiptLineTable.addColumn("Zeitpunkt", "timestamp", new TimestampFormatter());
			receiptLineTable.setFormatter(tableFormatter);
		}
		return receiptLineTable;
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
	
	public Receipt getReceipt() {
		if (receipt == null)
			receipt = (Receipt) getCurrentObject();
		return receipt;
	}
	
	public Input getCashierSelection() throws RemoteException {
		if (cashierSelection == null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator cashiers = service.createList(Cashier.class);
			cashiers.addFilter("event=?", getEvent().getID());
			Cashier cashier = (Cashier) service.createObject(Cashier.class, String.valueOf(getReceipt().getCashier()));
			cashierSelection = new SelectInput(cashiers, cashier);
			cashierSelection.setName("Kassierer");
			cashierSelection.setAttribute("number");
			cashierSelection.setMandatory(true);
		}
		return cashierSelection;
	}
	
	public void handleStore() {
		try {
			if (getEvent() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Veranstaltung auswählen!", StatusBarMessage.TYPE_ERROR));
			} else if (getPOS() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Kasse auswählen!", StatusBarMessage.TYPE_ERROR));
			} else {
				Receipt receipt = getReceipt();
				receipt.setCashier(Long.valueOf(((Cashier) getCashierSelection().getValue()).getID()));
				
				try {
					receipt.store();
					Application.getMessagingFactory().sendMessage(new StatusBarMessage("Beleg gespeichert", StatusBarMessage.TYPE_SUCCESS));
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing receipt", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern des Belegs: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}

}

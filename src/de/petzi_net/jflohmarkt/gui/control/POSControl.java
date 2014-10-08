/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.rmi.RemoteException;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.POSDetailAction;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class POSControl extends AbstractControl {
	
	private TablePart posTable;
	private TablePart receiptTable;
	
	private Event event;
	private POS pos;
	
	private SelectInput eventSelection;
	private IntegerInput number;
	private TextAreaInput description;

	public POSControl(AbstractView view) {
		super(view);
	}
	
	public TablePart getPOSTable() throws RemoteException {
		if (posTable == null) {
			DBIterator poses = null;
			if (getEvent() != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				poses = service.createList(POS.class);
				poses.addFilter("event=?", getEvent().getID());
			}
			
			posTable = new TablePart(poses, new POSDetailAction());
			posTable.addColumn("Nummer", "number");
			posTable.addColumn("Beschreibung", "description");
		}
		return posTable;
	}
	
	public TablePart getReceiptTable() throws RemoteException {
		if (receiptTable == null) {
			DBIterator receipts = null;
			if (getEvent() != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				receipts = service.createList(Receipt.class);
				receipts.addFilter("pos=?", getPOS().getID());
			}
			
			receiptTable = new TablePart(receipts, null/*new POSDetailAction()*/);
			if (getPOS() == null) {
				receiptTable.addColumn("Kasse", "pos");
			}
			receiptTable.addColumn("Nummer", "number");
			receiptTable.addColumn("Typ", "type");
			receiptTable.addColumn("Kassierer", "cashier");
			receiptTable.addColumn("Status", "state");
			receiptTable.addColumn("Zeitpunkt", "timestamp", new TimestampFormatter());
		}
		return receiptTable;
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
	
	public Input getEventSelection() throws RemoteException {
		if (eventSelection == null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator events = service.createList(Event.class);
			eventSelection = new SelectInput(events, getEvent());
			eventSelection.setName("Veranstaltung");
			eventSelection.setAttribute("name");
			eventSelection.setPleaseChoose("Bitte Veranstaltung auswählen...");
			eventSelection.addListener(new Listener() {
				
				@Override
				public void handleEvent(org.eclipse.swt.widgets.Event evt) {
					if (evt.type == SWT.Selection) {
						event = (Event) eventSelection.getValue();
						try {
							JFlohmarktPlugin.getSettings().setAttribute("event.current", event == null ? "" : event.getID());
						} catch (RemoteException e) {
							Logger.error("cannot store current selected event", e);
						}
						DBIterator poses = null;
						try {
							if (getEvent() != null) {
								DBService service = JFlohmarktPlugin.getDBService();
								poses = service.createList(POS.class);
								poses.addFilter("event=?", getEvent().getID());
							}
						} catch (RemoteException e) {
							Logger.error("cannot load POSs of currently selected event", e);
						}
						posTable.removeAll();
						if (poses != null) {
							try {
								while (poses.hasNext()) {
									posTable.addItem(poses.next());
								}
							} catch (RemoteException e) {
								Logger.error("cannot refresh list of POSs", e);
							}
						}
					}
				}
			});
		}
		return eventSelection;
	}
	
	public POS getPOS() {
		if (pos == null)
			pos = (POS) getCurrentObject();
		return pos;
	}
	
	public Input getNumber() throws RemoteException {
		if (number == null) {
			number = new IntegerInput(getPOS().getNumber());
			number.setMandatory(true);
			number.setName("Nummer");
			number.setMaxLength(4);
		}
		return number;
	}
	
	public Input getDescription() throws RemoteException {
		if (description == null) {
			description = new TextAreaInput(getPOS().getDescription(), 255);
			description.setName("Name");
			description.setHeight(400);
		}
		return description;
	}
	
	public void handleStore() {
		try {
			if (getEvent() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Veranstaltung auswählen!", StatusBarMessage.TYPE_ERROR));
			} else {
				POS pos = getPOS();
				pos.setEvent(Long.valueOf(getEvent().getID()));
				pos.setNumber((Integer) getNumber().getValue());
				pos.setDescription((String) getDescription().getValue());
				
				try {
					pos.store();
					Application.getMessagingFactory().sendMessage(new StatusBarMessage("Kasse gespeichert", StatusBarMessage.TYPE_SUCCESS));
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing pos", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern der Kasse: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}
	
	public void printList() throws ApplicationException {
		try {
			PDFReport.produceReportFile("Kassen", getEvent().getName(), 10, false, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException {
					try {
						PDFTable cashierTable = new PDFTable(getPOSTable().getItems());
						cashierTable.addColumn("Nummer", "number", 5);
						cashierTable.addColumn("Beschreibung", "description", 20);
						report.add(cashierTable);
					} catch (RemoteException e) {
						throw new ApplicationException(e);
					}
				}
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}

}

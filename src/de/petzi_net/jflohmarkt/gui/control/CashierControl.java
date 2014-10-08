/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.rmi.RemoteException;
import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.CashierDetailAction;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class CashierControl extends AbstractControl {
	
	private TablePart cashierTable;
	
	private Event event;
	private Cashier cashier;
	
	private SelectInput eventSelection;
	private IntegerInput number;
	private TextInput name;
	private TextInput givenName;

	public CashierControl(AbstractView view) {
		super(view);
	}
	
	public TablePart getCashierTable() throws RemoteException {
		if (cashierTable == null) {
			DBIterator cashiers = null;
			if (getEvent() != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				cashiers = service.createList(Cashier.class);
				cashiers.addFilter("event=?", getEvent().getID());
			}
			
			cashierTable = new TablePart(cashiers, new CashierDetailAction());
			cashierTable.addColumn("Nummer", "number");
			cashierTable.addColumn("Name", "name");
			cashierTable.addColumn("Vorname", "givenname");
		}
		return cashierTable;
	}
	
	private Event getEvent() throws RemoteException {
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
						DBIterator cashiers = null;
						try {
							if (getEvent() != null) {
								DBService service = JFlohmarktPlugin.getDBService();
								cashiers = service.createList(Cashier.class);
								cashiers.addFilter("event=?", getEvent().getID());
							}
						} catch (RemoteException e) {
							Logger.error("cannot load cashiers of currently selected event", e);
						}
						cashierTable.removeAll();
						if (cashiers != null) {
							try {
								while (cashiers.hasNext()) {
									cashierTable.addItem(cashiers.next());
								}
							} catch (RemoteException e) {
								Logger.error("cannot refresh list of cashiers", e);
							}
						}
					}
				}
			});
		}
		return eventSelection;
	}
	
	private Cashier getCashier() {
		if (cashier == null)
			cashier = (Cashier) getCurrentObject();
		return cashier;
	}
	
	public Input getNumber() throws RemoteException {
		if (number == null) {
			number = new IntegerInput(getCashier().getNumber());
			number.setMandatory(true);
			number.setName("Nummer");
			number.setMaxLength(4);
		}
		return number;
	}
	
	public Input getName() throws RemoteException {
		if (name == null) {
			name = new TextInput(getCashier().getName(), 40);
			name.setMandatory(true);
			name.setName("Name");
		}
		return name;
	}
	
	public Input getGivenName() throws RemoteException {
		if (givenName == null) {
			givenName = new TextInput(getCashier().getGivenName(), 40);
			givenName.setMandatory(true);
			givenName.setName("Vorname");
		}
		return givenName;
	}
	
	public void handleStore() {
		try {
			if (getEvent() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Veranstaltung auswählen!", StatusBarMessage.TYPE_ERROR));
			} else {
				Cashier cashier = getCashier();
				cashier.setEvent(Long.valueOf(getEvent().getID()));
				cashier.setNumber((Integer) getNumber().getValue());
				cashier.setName((String) getName().getValue());
				cashier.setGivenName((String) getGivenName().getValue());
				
				try {
					cashier.store();
					Application.getMessagingFactory().sendMessage(new StatusBarMessage("Kassierer gespeichert", StatusBarMessage.TYPE_SUCCESS));
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing cashier", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern des Kassierers: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}
	
	public void printList() throws ApplicationException {
		try {
			PDFReport.produceReportFile("Kassierer", getEvent().getName(), 10, false, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException {
					try {
						PDFTable cashierTable = new PDFTable(getCashierTable().getItems());
						cashierTable.addColumn("Nummer", "number", 5);
						cashierTable.addColumn("Name", "name", 10);
						cashierTable.addColumn("Vorname", "givenname", 10);
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

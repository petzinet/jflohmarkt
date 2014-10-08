/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Date;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.EventDetailAction;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.TextAreaInput;
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
public class EventControl extends AbstractControl {
	
	private TablePart eventTable;
	
	private Event event;
	
	private TextInput name;
	private DateInput start;
	private DateInput end;
	private TextAreaInput description;

	public EventControl(AbstractView view) {
		super(view);
	}
	
	public TablePart getEventTable() throws RemoteException {
		if (eventTable == null) {
			DBService service = JFlohmarktPlugin.getDBService();
			DBIterator events = service.createList(Event.class);
			
			eventTable = new TablePart(events, new EventDetailAction());
			eventTable.addColumn("Name", "name");
			eventTable.addColumn("Start", "start", new DateFormatter());
			eventTable.addColumn("Ende", "end", new DateFormatter());
			eventTable.addColumn("Beschreibung", "description");
		}
		return eventTable;
	}
	
	private Event getEvent() {
		if (event == null)
			event = (Event) getCurrentObject();
		return event;
	}
	
	public Input getName() throws RemoteException {
		if (name == null) {
			name = new TextInput(getEvent().getName(), 40);
			name.setMandatory(true);
			name.setName("Name");
		}
		return name;
	}
	
	public Input getStart() throws RemoteException {
		if (start == null) {
			start = new DateInput(getEvent().getStart());
			start.setMandatory(true);
			start.setName("Start");
		}
		return start;
	}
	
	public Input getEnd() throws RemoteException {
		if (end == null) {
			end = new DateInput(getEvent().getEnd());
			end.setMandatory(true);
			end.setName("Ende");
		}
		return end;
	}
	
	public Input getDescription() throws RemoteException {
		if (description == null) {
			description = new TextAreaInput(getEvent().getDescription(), 255);
			description.setName("Beschreibung");
			description.setHeight(100);
		}
		return description;
	}
	
	public void handleStore() {
		try {
			Event event = getEvent();
			event.setName((String) getName().getValue());
			event.setStart((Date) getStart().getValue());
			event.setEnd((Date) getEnd().getValue());
			event.setDescription((String) getDescription().getValue());
			
			try {
				event.store();
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Veranstaltung gespeichert", StatusBarMessage.TYPE_SUCCESS));
			} catch (ApplicationException e) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
			}
		} catch (RemoteException e) {
			Logger.error("error while storing event", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern der Veranstaltung: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}
	
	public void printList() throws ApplicationException {
		PDFReport.produceReportFile("Veranstaltungen", null, 10, false, new PDFReport.Producer() {
			
			@Override
			public void produce(PDFReport report) throws ApplicationException {
				try {
					PDFTable cashierTable = new PDFTable(getEventTable().getItems());
					cashierTable.addColumn("Name", "name", 10);
					cashierTable.addColumn("Start", "start", 4, new DateFormatter());
					cashierTable.addColumn("Ende", "end", 4, new DateFormatter());
					cashierTable.addColumn("Beschreibung", "description", 20);
					report.add(cashierTable);
				} catch (RemoteException e) {
					throw new ApplicationException(e);
				}
			}
		});
	}

}

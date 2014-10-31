/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.EventDetailAction;
import de.petzi_net.jflohmarkt.gui.control.POSControl.ReportConsumer;
import de.petzi_net.jflohmarkt.gui.control.SellerControl.ItemSale;
import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.petzi_net.jflohmarkt.report.PDFAlignment;
import de.petzi_net.jflohmarkt.report.PDFExtension;
import de.petzi_net.jflohmarkt.report.PDFGrid;
import de.petzi_net.jflohmarkt.report.PDFParagraph;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.DateFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
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
	private DecimalInput commissionRate;

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
			eventTable.addColumn("Provision", "commissionrate", new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS, "%"));
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
	
	public DecimalInput getCommissionRate() throws RemoteException {
		if (commissionRate == null) {
			DecimalFormat format = BigDecimalFormatter.createDecimalFormat(BigDecimalFormatter.TWO_DECIMALS);
			format.setParseBigDecimal(true);
			commissionRate = new DecimalInput(getEvent().getCommissionRate(), format);
			commissionRate.setMandatory(true);
			commissionRate.setName("Provisionssatz");
		}
		return commissionRate;
	}
	
	public void handleStore() {
		try {
			Event event = getEvent();
			event.setName((String) getName().getValue());
			event.setStart((Date) getStart().getValue());
			event.setEnd((Date) getEnd().getValue());
			event.setCommissionRate((BigDecimal) getCommissionRate().getNumber());
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
		try {
			PDFReport.produceReport("Veranstaltungen", null, 10, false, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					PDFTable eventTable = new PDFTable(getEventTable().getItems());
					eventTable.addColumn("Name", "name", 10);
					eventTable.addColumn("Start", "start", 4, new DateFormatter());
					eventTable.addColumn("Ende", "end", 4, new DateFormatter());
					eventTable.addColumn("Provision", "commissionrate", 4, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS, "%"));
					eventTable.addColumn("Beschreibung", "description", 16);
					report.add(eventTable);
				}
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}
	
	public void printReport() throws ApplicationException {
		try {
			PDFReport.produceReport("Veranstaltung", null, 10, false, new PDFReport.Producer() {
				
				private final Formatter FORMATTER = new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS);
				private final BigDecimal ONE_HUNDRED = new BigDecimal("100");
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					DBService service = JFlohmarktPlugin.getDBService();
					
					DBIterator sellers = service.createList(Seller.class);
					sellers.addFilter("event=?", getEvent().getID());
					sellers.setOrder("order by name, givenname");
					
					int sumItemCount = 0;
					BigDecimal sumTurnover = BigDecimal.ZERO;
					BigDecimal sumCommission = BigDecimal.ZERO;
					BigDecimal sumPayout = BigDecimal.ZERO;
					while (sellers.hasNext()) {
						Seller seller = (Seller) sellers.next();
						
						List<ItemSale> sales = SellerControl.createItemSaleList(seller);
						
						int itemCount = sales.size();
						BigDecimal turnover = BigDecimal.ZERO;
						for (ItemSale o : sales) {
							turnover = turnover.add(o.getValue());
						}
						BigDecimal commissionRate = seller.getCommissionRate() == null ? getEvent().getCommissionRate() : seller.getCommissionRate();
						BigDecimal commission = turnover.multiply(commissionRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
						BigDecimal payout = turnover.subtract(commission);
						
						sumItemCount += itemCount;
						sumTurnover = sumTurnover.add(turnover);
						sumCommission = sumCommission.add(commission);
						sumPayout = sumPayout.add(payout);
					}
					
					
					DBIterator poses = service.createList(POS.class);
					poses.addFilter("event=?", getEvent().getID());
					poses.setOrder("order by number");
					
					BigDecimal sumDifference = BigDecimal.ZERO;
					while (poses.hasNext()) {
						POS pos = (POS) poses.next();
						
						ReportConsumer reportConsumer = POSControl.createJournal(pos, new ReportConsumer(pos));
						
						sumDifference = sumDifference.add(reportConsumer.getDifference());
					}
					
					report.setFooter(new PDFExtension(" "));
					
					report.add(new PDFParagraph(" "));
					report.add(new PDFParagraph(" "));
					report.add(new PDFParagraph(" "));
					report.add(new PDFParagraph("°+°**Abrechnung**\n" + getEvent().getName(), PDFAlignment.CENTER));
					report.add(new PDFParagraph(" "));
					report.add(new PDFParagraph(" "));
					
					PDFGrid grid = new PDFGrid(true, 6, 40, 20, 40);
					grid.setCell(0, 0, new PDFParagraph("Gesamtanzahl", PDFAlignment.RIGHT));
					grid.setCell(0, 1, new PDFParagraph("**" + sumItemCount, PDFAlignment.RIGHT));
					grid.setCell(2, 0, new PDFParagraph("Gesamtumsatz", PDFAlignment.RIGHT));
					grid.setCell(2, 1, new PDFParagraph("**" + FORMATTER.format(sumTurnover), PDFAlignment.RIGHT));
					grid.setCell(2, 2, new PDFParagraph("**€"));
					grid.setCell(3, 0, new PDFParagraph("abzüglich Auszahlungen an Verkäufer", PDFAlignment.RIGHT));
					grid.setCell(3, 1, new PDFParagraph("**" + FORMATTER.format(sumPayout.negate()), PDFAlignment.RIGHT));
					grid.setCell(3, 2, new PDFParagraph("**€"));
					grid.setCell(4, 0, new PDFParagraph("abzüglich Kassendifferenzen", PDFAlignment.RIGHT));
					grid.setCell(4, 1, new PDFParagraph("**" + FORMATTER.format(sumDifference), PDFAlignment.RIGHT));
					grid.setCell(4, 2, new PDFParagraph("**€"));
					grid.setCell(5, 0, new PDFParagraph("°+°Gesamtertrag", PDFAlignment.RIGHT));
					grid.setCell(5, 1, new PDFParagraph("°+°**" + FORMATTER.format(sumTurnover.subtract(sumPayout).add(sumDifference)), PDFAlignment.RIGHT));
					grid.setCell(5, 2, new PDFParagraph("°+°**€"));
					report.add(grid);
				}
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}

}

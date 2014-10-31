/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.POSDetailAction;
import de.petzi_net.jflohmarkt.gui.action.ReceiptDetailAction;
import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.BooleanFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.CashierFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptStateFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptTypeFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.SellerFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.report.BigDecimalSum;
import de.petzi_net.jflohmarkt.report.PDFAggregation;
import de.petzi_net.jflohmarkt.report.PDFAlignment;
import de.petzi_net.jflohmarkt.report.PDFParagraph;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.input.Input;
import de.willuhn.jameica.gui.input.IntegerInput;
import de.willuhn.jameica.gui.input.SelectInput;
import de.willuhn.jameica.gui.input.TextAreaInput;
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
			
			TableFormatter tableFormatter = new TableFormatter() {
				
				@Override
				public void format(TableItem item) {
					if (item != null && item.getData() instanceof Receipt) {
						Receipt receipt = (Receipt) item.getData();
						try {
							switch (receipt.getState()) {
							case Receipt.STATE_ABORTED:
								item.setForeground(Color.COMMENT.getSWTColor());
								break;
							case Receipt.STATE_ACTIVE:
								item.setForeground(Color.ERROR.getSWTColor());
								break;
							case Receipt.STATE_FINISHED:
								break;
							}
						} catch (RemoteException e) {
							// wo what !
						}
					}
				}
				
			};
			
			receiptTable = new TablePart(receipts, new ReceiptDetailAction());
			if (getPOS() == null) {
				receiptTable.addColumn("Kasse", "pos");
			}
			receiptTable.addColumn("Nummer", "number");
			receiptTable.addColumn("Typ", "type", new ReceiptTypeFormatter());
			receiptTable.addColumn("Kassierer", "cashier", new CashierFormatter(getEvent()));
			receiptTable.addColumn("Status", "state", new ReceiptStateFormatter());
			receiptTable.addColumn("Zeitpunkt", "timestamp", new TimestampFormatter());
			receiptTable.setFormatter(tableFormatter);
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
			PDFReport.produceReport("Kassen", getEvent().getName(), 10, false, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					PDFTable cashierTable = new PDFTable(getPOSTable().getItems());
					cashierTable.addColumn("Nummer", "number", 5);
					cashierTable.addColumn("Beschreibung", "description", 20);
					report.add(cashierTable);
				}
				
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}
	
	public static class JournalLine {
		
		private final Integer receipt;
		private final Integer type;
		private final Long cashier;
		private final Integer state;
		private final Date timestamp;
		
		private final Integer line;
		private final Long seller;
		private final Integer quantity;
		private final BigDecimal value;
		private final Boolean valid;
		private final Date lineTimestamp;
		
		private final BigDecimal amount;
		private final BigDecimal difference;
		private final BigDecimal balance;
		
		public JournalLine(Receipt receipt, ReceiptLine line, BigDecimal amount, BigDecimal difference, BigDecimal balance) throws RemoteException {
			if (receipt == null) {
				this.receipt = null;
				this.type = null;
				this.cashier = null;
				this.state = null;
				this.timestamp = null;
			} else {
				this.receipt = receipt.getNumber();
				this.type = receipt.getType();
				this.cashier = receipt.getCashier();
				this.state = receipt.getState();
				this.timestamp = receipt.getTimestamp();
			}
			if (line == null) {
				this.line = null;
				this.seller = null;
				this.quantity = null;
				this.value = null;
				this.valid = null;
				this.lineTimestamp = null;
			} else {
				this.line = line.getLine();
				this.seller = line.getSeller();
				this.quantity = line.getQuantity();
				this.value = line.getValue();
				this.valid = line.isValid();
				this.lineTimestamp = line.getTimestamp();
			}
			this.amount = amount;
			this.difference = difference;
			this.balance = balance;
		}

		public Integer getReceipt() {
			return receipt;
		}

		public Integer getType() {
			return type;
		}

		public Long getCashier() {
			return cashier;
		}

		public Integer getState() {
			return state;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public Integer getLine() {
			return line;
		}

		public Long getSeller() {
			return seller;
		}

		public Integer getQuantity() {
			return quantity;
		}

		public BigDecimal getValue() {
			return value;
		}

		public Boolean getValid() {
			return valid;
		}

		public Date getLineTimestamp() {
			return lineTimestamp;
		}

		public BigDecimal getAmount() {
			return amount;
		}

		public BigDecimal getDifference() {
			return difference;
		}

		public BigDecimal getBalance() {
			return balance;
		}
		
	}
	
	private static class JournalLineListConsumer implements JournalConsumer<List<JournalLine>> {

		private List<JournalLine> result = new ArrayList<JournalLine>();
		private int insertPos;
		
		@Override
		public void startReceipt(Receipt receipt) throws RemoteException {
			insertPos = result.size();
		}

		@Override
		public void processReceiptLine(ReceiptLine line) throws RemoteException {
			result.add(new JournalLine(null, line, null, null, null));
		}

		@Override
		public void finishReceipt(Receipt receipt, BigDecimal amount, BigDecimal balance) throws RemoteException {
			BigDecimal difference = null;
			if (receipt.getType() == Receipt.TYPE_INVENTORY) {
				difference = amount;
				amount = null;
			}
			result.add(insertPos, new JournalLine(receipt, null, amount, difference, balance));
		}
		
		@Override
		public List<JournalLine> getResult() throws RemoteException {
			return result;
		}
		
	}
	
	public static interface JournalConsumer<R> {
		
		public void startReceipt(Receipt receipt) throws RemoteException;
		
		public void processReceiptLine(ReceiptLine line) throws RemoteException;
		
		public void finishReceipt(Receipt receipt, BigDecimal amount, BigDecimal balance) throws RemoteException;
		
		public R getResult() throws RemoteException;
		
	}
	
	public static <R> R createJournal(POS pos, JournalConsumer<R> consumer) throws RemoteException {
		BigDecimal balance = BigDecimal.ZERO;
		
		DBService service = JFlohmarktPlugin.getDBService();
		DBIterator receipts = service.createList(Receipt.class);
		receipts.addFilter("pos=?", pos.getID());
		receipts.setOrder("order by number");
		
		while (receipts.hasNext()) {
			Receipt receipt = (Receipt) receipts.next();
			consumer.startReceipt(receipt);
			BigDecimal amount = BigDecimal.ZERO;
			
			DBIterator lines = service.createList(ReceiptLine.class);
			lines.addFilter("receipt=?", receipt.getID());
			lines.setOrder("order by line");
			
			while (lines.hasNext()) {
				ReceiptLine line = (ReceiptLine) lines.next();
				
				if (line.isValid() && receipt.getState() == Receipt.STATE_FINISHED) {
					switch (receipt.getType()) {
					case Receipt.TYPE_DROPOFF:
						amount = amount.add(line.getValue().multiply(BigDecimal.valueOf(line.getQuantity())));
						break;
					case Receipt.TYPE_PICKUP:
						amount = amount.subtract(line.getValue().multiply(BigDecimal.valueOf(line.getQuantity())));
						break;
					case Receipt.TYPE_INVENTORY:
						amount = amount.add(line.getValue().multiply(BigDecimal.valueOf(line.getQuantity())));
						break;
					case Receipt.TYPE_SALE:
						if (line.getSeller() != null) {
							amount = amount.add(line.getValue());
						}
						break;
					}
				}
				
				consumer.processReceiptLine(line);
			}
			
			if (receipt.getType() == Receipt.TYPE_INVENTORY) {
				BigDecimal difference = amount.subtract(balance);
				balance = amount;
				amount = difference;
			} else {
				balance = balance.add(amount);
			}
			
			consumer.finishReceipt(receipt, amount, balance);
		}
		
		return consumer.getResult();
	}
	
	public void printJournal() throws ApplicationException {
		try {
			PDFReport.produceReport("Kassenjournal", getEvent().getName(), 8, true, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					DBService service = JFlohmarktPlugin.getDBService();
					DBIterator poses = service.createList(POS.class);
					poses.addFilter("event=?", getEvent().getID());
					poses.setOrder("order by number");
					
					while (poses.hasNext()) {
						POS pos = (POS) poses.next();
						
						report.add(new PDFParagraph("°+°Kasse " + pos.getNumber(), PDFAlignment.CENTER));
						report.add(new PDFParagraph(" "));
						
						PDFTable table = new PDFTable(createJournal(pos, new JournalLineListConsumer()));
						table.addColumn("Beleg", "receipt", 6, PDFAlignment.RIGHT);
						table.addColumn("Typ", "type", 6, new ReceiptTypeFormatter());
						table.addColumn("Kassierer", "cashier", 8, PDFAlignment.RIGHT, new CashierFormatter(getEvent()));
						table.addColumn("Status", "state", 12, new ReceiptStateFormatter());
						table.addColumn("Zeitpunkt", "timestamp", 16, new TimestampFormatter());
						table.addColumn("Zeile", "line", 5, PDFAlignment.RIGHT);
						table.addColumn("Verkäufer", "seller", 10, PDFAlignment.RIGHT, new SellerFormatter(getEvent()));
						table.addColumn("Anzahl", "quantity", 8, PDFAlignment.RIGHT);
						table.addColumn("Wert", "value", 8, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS));
						table.addColumn("Gültig", "valid", 6, new BooleanFormatter("ja", "nein"));
						table.addColumn("Zeitpunkt", "lineTimestamp", 16, new TimestampFormatter());
						table.addColumn("Betrag", "amount", 8, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
						table.addColumn("Differenz", "difference", 8, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
						table.addColumn("Saldo", "balance", 8, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new PDFAggregation() {
							
							private final Formatter formatter = new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS);
							
							private BigDecimal lastValue = BigDecimal.ZERO;
							
							@Override
							public void addValue(Object value) {
								if (value instanceof BigDecimal) {
									lastValue = (BigDecimal) value;
								}
							}
							
							@Override
							public String toString() {
								return formatter.format(lastValue);
							}
							
						});
						report.add(table);
						
						report.newPage();
					}
				}
				
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}
	
	public static class ReportConsumer implements JournalConsumer<ReportConsumer> {

		private final int number;
		private final String description;
		
		private BigDecimal turnover = BigDecimal.ZERO;
		private BigDecimal pickup = BigDecimal.ZERO;
		private BigDecimal dropoff = BigDecimal.ZERO;
		private BigDecimal difference = BigDecimal.ZERO;
		private BigDecimal balance = BigDecimal.ZERO;
		
		public ReportConsumer(POS pos) throws RemoteException {
			this.number = pos.getNumber();
			this.description = pos.getDescription();
		}
		
		@Override
		public void startReceipt(Receipt receipt) throws RemoteException {
		}
		
		@Override
		public void processReceiptLine(ReceiptLine line) throws RemoteException {
		}
		
		@Override
		public void finishReceipt(Receipt receipt, BigDecimal amount, BigDecimal balance) throws RemoteException {
			switch (receipt.getType()) {
			case Receipt.TYPE_DROPOFF:
				dropoff = dropoff.add(amount);
				break;
			case Receipt.TYPE_INVENTORY:
				difference = difference.add(amount);
				break;
			case Receipt.TYPE_PICKUP:
				pickup = pickup.add(amount);
				break;
			case Receipt.TYPE_SALE:
				turnover = turnover.add(amount);
				break;
			}
			this.balance = balance;
		}
		
		@Override
		public ReportConsumer getResult() throws RemoteException {
			return this;
		}
		
		public int getNumber() {
			return number;
		}
		
		public String getDescription() {
			return description;
		}
		
		public BigDecimal getTurnover() {
			return turnover;
		}
		
		public BigDecimal getDropoff() {
			return dropoff;
		}
		
		public BigDecimal getPickup() {
			return pickup;
		}
		
		public BigDecimal getDifference() {
			return difference;
		}
		
		public BigDecimal getBalance() {
			return balance;
		}
		
		public BigDecimal getRevenue() {
			return balance.subtract(pickup).subtract(dropoff);
		}
		
	}
	
	public void printReport() throws ApplicationException {
		try {
			PDFReport.produceReport("Kassenabrechnung", getEvent().getName(), 8, true, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					List<ReportConsumer> lines = new ArrayList<ReportConsumer>();
					
					DBService service = JFlohmarktPlugin.getDBService();
					DBIterator poses = service.createList(POS.class);
					poses.addFilter("event=?", getEvent().getID());
					poses.setOrder("order by number");
					
					while (poses.hasNext()) {
						POS pos = (POS) poses.next();
						
						lines.add(createJournal(pos, new ReportConsumer(pos)));
					}
					
					PDFTable table = new PDFTable(lines);
					table.addColumn("Kasse", "number", 6, PDFAlignment.RIGHT);
					table.addColumn("Beschreibung", "description", 30);
					table.addColumn("Umsatz", "turnover", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Einlage", "dropoff", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Leerung", "pickup", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Differenz", "difference", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Saldo", "balance", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Einnahmen", "revenue", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					report.add(table);
				}
				
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}

}

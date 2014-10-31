/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.SellerDetailAction;
import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.report.BigDecimalSum;
import de.petzi_net.jflohmarkt.report.PDFAlignment;
import de.petzi_net.jflohmarkt.report.PDFExtension;
import de.petzi_net.jflohmarkt.report.PDFGrid;
import de.petzi_net.jflohmarkt.report.PDFList;
import de.petzi_net.jflohmarkt.report.PDFParagraph;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.datasource.rmi.ResultSetExtractor;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.gui.input.DateInput;
import de.willuhn.jameica.gui.input.DecimalInput;
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
public class SellerControl extends AbstractControl {
	
	private TablePart sellerTable;
	
	private Event event;
	private Seller seller;
	
	private SelectInput eventSelection;
	private IntegerInput number;
	private TextInput name;
	private TextInput givenName;
	private TextInput addressAppendix;
	private TextInput street;
	private TextInput zipcode;
	private TextInput city;
	private TextInput phone;
	private TextInput email;
	private TextInput accountHolder;
	private TextInput bic;
	private TextInput iban;
	private DateInput registration;
	private DecimalInput commissionRate;

	public SellerControl(AbstractView view) {
		super(view);
	}
	
	public TablePart getSellerTable() throws RemoteException {
		if (sellerTable == null) {
			DBIterator sellers = null;
			if (getEvent() != null) {
				DBService service = JFlohmarktPlugin.getDBService();
				sellers = service.createList(Seller.class);
				sellers.addFilter("event=?", getEvent().getID());
			}
			
			sellerTable = new TablePart(sellers, new SellerDetailAction());
			sellerTable.addColumn("Nummer", "number");
			sellerTable.addColumn("Name", "name");
			sellerTable.addColumn("Vorname", "givenname");
			sellerTable.addColumn("Straße", "street");
			sellerTable.addColumn("PLZ", "zipcode");
			sellerTable.addColumn("Ort", "city");
			sellerTable.addColumn("Telefon", "phone");
			sellerTable.addColumn("Email", "email");
			sellerTable.addColumn("Registrierung", "registration", new TimestampFormatter());
			sellerTable.addColumn("Provision", "commissionrate", new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS, "%"));
			sellerTable.setMulti(true);
		}
		return sellerTable;
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
						DBIterator sellers = null;
						try {
							if (getEvent() != null) {
								DBService service = JFlohmarktPlugin.getDBService();
								sellers = service.createList(Seller.class);
								sellers.addFilter("event=?", getEvent().getID());
							}
						} catch (RemoteException e) {
							Logger.error("cannot load sellers of currently selected event", e);
						}
						sellerTable.removeAll();
						if (sellers != null) {
							try {
								while (sellers.hasNext()) {
									sellerTable.addItem(sellers.next());
								}
							} catch (RemoteException e) {
								Logger.error("cannot refresh list of sellers", e);
							}
						}
					}
				}
			});
		}
		return eventSelection;
	}
	
	private Seller getSeller() {
		if (seller == null)
			seller = (Seller) getCurrentObject();
		return seller;
	}
	
	public Input getNumber() throws RemoteException {
		if (number == null) {
			number = new IntegerInput(getSeller().getNumber());
			number.setMandatory(true);
			number.setName("Nummer");
			number.setMaxLength(4);
		}
		return number;
	}
	
	public Input getName() throws RemoteException {
		if (name == null) {
			name = new TextInput(getSeller().getName(), 40);
			name.setMandatory(true);
			name.setName("Name");
		}
		return name;
	}
	
	public Input getGivenName() throws RemoteException {
		if (givenName == null) {
			givenName = new TextInput(getSeller().getGivenName(), 40);
			givenName.setMandatory(true);
			givenName.setName("Vorname");
		}
		return givenName;
	}
	
	public Input getAddressAppendix() throws RemoteException {
		if (addressAppendix == null) {
			addressAppendix = new TextInput(getSeller().getAddressAppendix(), 40);
			addressAppendix.setName("Adressierungszusatz");
		}
		return addressAppendix;
	}
	
	public Input getStreet() throws RemoteException {
		if (street == null) {
			street = new TextInput(getSeller().getStreet(), 40);
			street.setMandatory(true);
			street.setName("Straße");
		}
		return street;
	}
	
	public Input getZipCode() throws RemoteException {
		if (zipcode == null) {
			zipcode = new TextInput(getSeller().getZipCode(), 10);
			zipcode.setMandatory(true);
			zipcode.setName("PLZ");
		}
		return zipcode;
	}
	
	public Input getCity() throws RemoteException {
		if (city == null) {
			city = new TextInput(getSeller().getCity(), 40);
			city.setMandatory(true);
			city.setName("Ort");
		}
		return city;
	}
	
	public Input getPhone() throws RemoteException {
		if (phone == null) {
			phone = new TextInput(getSeller().getPhone(), 20);
			phone.setName("Telefon");
		}
		return phone;
	}
	
	public Input getEmail() throws RemoteException {
		if (email == null) {
			email = new TextInput(getSeller().getEmail(), 50);
			email.setMandatory(true);
			email.setName("Email");
		}
		return email;
	}
	
	public Input getAccountHolder() throws RemoteException {
		if (accountHolder == null) {
			accountHolder = new TextInput(getSeller().getAccountHolder(), 80);
			accountHolder.setName("Kontoinhaber");
		}
		return accountHolder;
	}
	
	public Input getBIC() throws RemoteException {
		if (bic == null) {
			bic = new TextInput(getSeller().getBIC(), 11);
			bic.setName("BIC");
		}
		return bic;
	}
	
	public Input getIBAN() throws RemoteException {
		if (iban == null) {
			iban = new TextInput(getSeller().getIBAN(), 80);
			iban.setName("IBAN");
		}
		return iban;
	}
	
	public Input getRegistration() throws RemoteException {
		if (registration == null) {
			registration = new DateInput(getSeller().getRegistration(), TimestampFormatter.TIMESTAMP_FORMAT);
			registration.setName("Registrierung");
		}
		return registration;
	}
	
	public DecimalInput getCommissionRate() throws RemoteException {
		if (commissionRate == null) {
			DecimalFormat format = BigDecimalFormatter.createDecimalFormat(BigDecimalFormatter.TWO_DECIMALS);
			format.setParseBigDecimal(true);
			commissionRate = new DecimalInput(getSeller().getCommissionRate(), format);
			commissionRate.setName("Provisionssatz");
		}
		return commissionRate;
	}
	
	public void handleStore() {
		try {
			if (getEvent() == null) {
				Application.getMessagingFactory().sendMessage(new StatusBarMessage("Bitte erst eine Veranstaltung auswählen!", StatusBarMessage.TYPE_ERROR));
			} else {
				Seller seller = getSeller();
				seller.setEvent(Long.valueOf(getEvent().getID()));
				seller.setNumber((Integer) getNumber().getValue());
				seller.setName((String) getName().getValue());
				seller.setGivenName((String) getGivenName().getValue());
				seller.setAddressAppendix((String) getAddressAppendix().getValue());
				seller.setStreet((String) getStreet().getValue());
				seller.setZipCode((String) getZipCode().getValue());
				seller.setCity((String) getCity().getValue());
				seller.setPhone((String) getPhone().getValue());
				seller.setEmail((String) getEmail().getValue());
				seller.setAccountHolder((String) getAccountHolder().getValue());
				seller.setBIC((String) getBIC().getValue());
				seller.setIBAN((String) getIBAN().getValue());
				seller.setRegistration((Date) getRegistration().getValue());
				seller.setCommissionRate((BigDecimal) getCommissionRate().getNumber());
				
				try {
					seller.store();
					Application.getMessagingFactory().sendMessage(new StatusBarMessage("Verkäufer gespeichert", StatusBarMessage.TYPE_SUCCESS));
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
		} catch (RemoteException e) {
			Logger.error("error while storing seller", e);
			Application.getMessagingFactory().sendMessage(new StatusBarMessage(MessageFormat.format("Fehler beim Speichern des Verkäufers: {0}", e.getMessage()),StatusBarMessage.TYPE_ERROR));
		}
	}
	
	public void printList() throws ApplicationException {
		try {
			PDFReport.produceReport("Verkäufer", getEvent().getName(), 8, true, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					PDFTable sellerTable = new PDFTable(getSellerTable().getItems());
					sellerTable.addColumn("Nummer", "number", 5);
					sellerTable.addColumn("Name", "name", 9);
					sellerTable.addColumn("Vorname", "givenname", 9);
					sellerTable.addColumn("Straße", "street", 9);
					sellerTable.addColumn("PLZ", "zipcode", 4);
					sellerTable.addColumn("Ort", "city", 9);
					sellerTable.addColumn("Telefon", "phone", 9);
					sellerTable.addColumn("Email", "email", 10);
					sellerTable.addColumn("Registrierung", "registration", 8, new TimestampFormatter());
					sellerTable.addColumn("Provision", "commissionrate", 5, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS, "%"));
					report.add(sellerTable);
				}
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}
	
	public static class SellerTurnover {
		
		private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
		
		private final int number;
		private final String name;
		private final String givenName;
		private final BigDecimal turnover;
		private final BigDecimal commissionRate;
		private final BigDecimal commission;
		private final BigDecimal payout;
		
		public SellerTurnover(Seller seller, BigDecimal turnover, BigDecimal commissionRate) throws RemoteException {
			this.number = seller.getNumber();
			this.name = seller.getName();
			this.givenName = seller.getGivenName();
			this.turnover = turnover;
			this.commissionRate = commissionRate;
			this.commission = turnover.multiply(commissionRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
			this.payout = turnover.subtract(commission);
		}

		public int getNumber() {
			return number;
		}

		public String getName() {
			return name;
		}

		public String getGivenName() {
			return givenName;
		}

		public BigDecimal getTurnover() {
			return turnover;
		}

		public BigDecimal getCommissionRate() {
			return commissionRate;
		}

		public BigDecimal getCommission() {
			return commission;
		}

		public BigDecimal getPayout() {
			return payout;
		}
		
	}
	
	public void printTurnover() throws ApplicationException {
		try {
			PDFReport.produceReport("Verkäuferumsätze", getEvent().getName(), 8, false, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					List<SellerTurnover> turnovers = new ArrayList<SellerControl.SellerTurnover>();
					
					DBService service = JFlohmarktPlugin.getDBService();
					DBIterator sellers = service.createList(Seller.class);
					sellers.addFilter("event=?", getEvent().getID());
					sellers.setOrder("order by name, givenname");
					
					while (sellers.hasNext()) {
						Seller seller = (Seller) sellers.next();
						
						BigDecimal turnoverValue = (BigDecimal) JFlohmarktPlugin.getDBService().execute("select sum(l.value) from receiptline as l, receipt as r, pos as p where p.event=? and l.seller=? and l.valid=? and r.type=? and r.state=? and r.pos=p.id and l.receipt=r.id", new Object[]{getEvent().getID(), seller.getID(), true, Receipt.TYPE_SALE, Receipt.STATE_FINISHED}, new ResultSetExtractor() {
							
							@Override
							public Object extract(ResultSet rs) throws RemoteException, SQLException {
								if (rs.next()) {
									Object result = rs.getBigDecimal(1);
									if (result == null) {
										return BigDecimal.ZERO;
									} else {
										return result;
									}
								} else {
									return BigDecimal.ZERO;
								}
							}
							
						});
						
						SellerTurnover turnover = new SellerTurnover(seller, turnoverValue, seller.getCommissionRate() == null ? getEvent().getCommissionRate() : seller.getCommissionRate());
						
						turnovers.add(turnover);
					}
					
					PDFTable table = new PDFTable(turnovers);
					table.addColumn("Nummer", "number", 10);
					table.addColumn("Name", "name", 20);
					table.addColumn("Vorname", "givenName", 20);
					table.addColumn("Umsatz", "turnover", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Provision (%)", "commissionRate", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS));
					table.addColumn("Provision", "commission", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					table.addColumn("Auszahlung", "payout", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
					report.add(table);
				}
				
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
		
//		FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
//	    fd.setText("Ausgabedatei wählen...");
//	    fd.setFileName("/home/axel/ZZZZtest.pdf");
//	    fd.setFilterExtensions(new String[] { "*.pdf" });
//	    String filePath = fd.open();
//	    if (filePath == null || filePath.length() == 0) {
//	    	return;
//	    }
//	    if (!filePath.toLowerCase().endsWith(".pdf")) {
//	    	filePath = filePath + ".pdf";
//	    }
//	    File file = new File(filePath);
//		
//	    try {
//			OutputStream os = new FileOutputStream(file);
//			try {
//				PDFExtension header = new PDFExtension("**Das ist ein °8°Text°° auf Seite ##.** \n°16°Alles klar°°?");
//				PDFExtension footer = new PDFExtension("Das ist ein Text auf Seite ##. \nNa klar!");
//				PDFReport report = new PDFReport(os, "Verkäufer", "Test", 10, false);
//				report.setHeader(header);
//				report.setFooter(footer);
//				PDFTable sellerTable = new PDFTable(getSellerTable().getItems());
//				sellerTable.addColumn("Nummer", "number", 10);
//				sellerTable.addColumn("Name", "name", 10);
//				sellerTable.addColumn("Vorname", "givenname", 10);
//				sellerTable.addColumn("Straße", "street", 10);
//				sellerTable.addColumn("PLZ", "zipcode", 10);
//				sellerTable.addColumn("Ort", "city", 10);
//				sellerTable.addColumn("Telefon", "phone", 10);
//				sellerTable.addColumn("Email", "email", 10);
//				sellerTable.addColumn("Registrierung", "registration", 10);
//				report.add(sellerTable);
//				report.setHeader(new PDFExtension("blub"));
//				report.resetPageCount();
//				report.add(new PDFParagraph("Lorem ipsum dolor **sit amet, //consetetur// sadipscing elitr**, sed diam __nonumy eirmod tempor__ invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.\n At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
//				report.close();
//				
////				try {
////					Runtime.getRuntime().exec("/usr/bin/okular", new String[]{file.getPath()});
////				} catch (IOException e) {
////					e.printStackTrace();
////				}
//				
////		    	try {
////					Document doc = new Document();
////					doc.setMargins(40, 20, 20, 40);
////					PdfWriter.getInstance(doc, os);
////					doc.addAuthor("JFlohmarkt");
////					doc.addTitle("Verkäuferumsätze");
////					doc.open();
////					doc.newPage();
////					PdfPTable table = new PdfPTable(3);
////					PdfPCell cell11 = new PdfPCell(new Phrase("hallo1"));
////					cell11.setBackgroundColor(new Color(192, 192, 192));
////					table.addCell(cell11);
////					table.addCell(new PdfPCell(new Phrase("hallo2")));
////					table.addCell(new PdfPCell(new Phrase("hallo3")));
////					table.setHeaderRows(1);
////					for (int n = 0; n < 40; n++) {
////						PdfPCell cell21 = new PdfPCell(new Phrase("tach1"));
////						cell21.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
////						table.addCell(cell21);
////						table.addCell(new PdfPCell(new Phrase("tach2")));
////						table.addCell(new PdfPCell(new Phrase("tach3")));
//////					table.completeRow();
////						PdfPCell cell31 = new PdfPCell(new Phrase("moin1"));
////						cell31.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
////						cell31.setBackgroundColor(new Color(224, 224, 224));
////						table.addCell(cell31);
////						table.addCell(new PdfPCell(new Phrase("moin2")));
////						table.addCell(new PdfPCell(new Phrase("moin3")));
//////					table.completeRow();
////					}
////					doc.add(table);
////					doc.close();
////				} catch (DocumentException e) {
////					throw new ApplicationException(e);
////				}
//			} finally {
//				try {
//					os.close();
//				} catch (IOException e) {
//				}
//			}
//	    } catch (RemoteException e) {
//	    	throw new ApplicationException(e);
//		} catch (FileNotFoundException e) {
//	    	throw new ApplicationException(e);
//		}
	}
	
	public static class ItemSale {
		
		private final int pos;
		private final int receipt;
		private final int cashier;
		private final Date timestamp;
		private final BigDecimal value;
		
		public ItemSale(int pos, int receipt, int cashier, Date timestamp, BigDecimal value) {
			this.pos = pos;
			this.receipt = receipt;
			this.cashier = cashier;
			this.timestamp = timestamp;
			this.value = value;
		}

		public int getPos() {
			return pos;
		}

		public int getReceipt() {
			return receipt;
		}

		public int getCashier() {
			return cashier;
		}

		public Date getTimestamp() {
			return timestamp;
		}

		public BigDecimal getValue() {
			return value;
		}
		
	}
	
	public static List<ItemSale> createItemSaleList(Seller seller) throws RemoteException {
		final List<ItemSale> sales = new ArrayList<ItemSale>();
		DBService service = JFlohmarktPlugin.getDBService();
		Event event = (Event) service.createObject(Event.class, String.valueOf(seller.getEvent()));
		service.execute("select p.number, r.number, c.number, r.timestamp, l.value from receiptline as l, receipt as r, pos as p, cashier as c where p.event=? and l.seller=? and l.valid=? and r.type=? and r.state=? and r.pos=p.id and l.receipt=r.id and r.cashier=c.id order by r.timestamp", new Object[]{event.getID(), seller.getID(), true, Receipt.TYPE_SALE, Receipt.STATE_FINISHED}, new ResultSetExtractor() {
			
			@Override
			public Object extract(ResultSet rs) throws RemoteException, SQLException {
				while (rs.next()) {
					int pos = rs.getInt(1);
					int receipt = rs.getInt(2);
					int cashier = rs.getInt(3);
					Date timestamp = rs.getTimestamp(4);
					BigDecimal value = rs.getBigDecimal(5);
					sales.add(new ItemSale(pos, receipt, cashier, timestamp, value));
				}
				return sales;
			}
			
		});
		return sales;
	}
	
	public void printClearings() throws ApplicationException {
		try {
			PDFReport.produceReport("Abrechnung", getEvent().getName(), 12, false, new PDFReport.Producer() {
				
				private final Formatter FORMATTER = new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS);
				private final BigDecimal ONE_HUNDRED = new BigDecimal("100");
				
				@Override
				public void produce(PDFReport report) throws ApplicationException, RemoteException {
					DBService service = JFlohmarktPlugin.getDBService();
					DBIterator sellers = service.createList(Seller.class);
					sellers.addFilter("event=?", getEvent().getID());
					sellers.setOrder("order by name, givenname");
					
					while (sellers.hasNext()) {
						Seller seller = (Seller) sellers.next();
						
						List<ItemSale> sales = createItemSaleList(seller);
						
						int itemCount = sales.size();
						BigDecimal turnover = BigDecimal.ZERO;
						for (ItemSale o : sales) {
							turnover = turnover.add(o.getValue());
						}
						BigDecimal commissionRate = seller.getCommissionRate() == null ? getEvent().getCommissionRate() : seller.getCommissionRate();
						BigDecimal commission = turnover.multiply(commissionRate).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
						BigDecimal payout = turnover.subtract(commission);
						
						StringBuffer address = new StringBuffer();
						if (seller.getGivenName() != null && !seller.getGivenName().isEmpty()) {
							address.append(seller.getGivenName());
							address.append(' ');
						}
						if (seller.getName() != null && !seller.getName().isEmpty()) {
							address.append(seller.getName());
						}
						address.append('\n');
						if (seller.getAddressAppendix() != null && !seller.getAddressAppendix().isEmpty()) {
							address.append(seller.getAddressAppendix());
							address.append('\n');
						}
						if (seller.getStreet() != null && !seller.getStreet().isEmpty()) {
							address.append(seller.getStreet());
							address.append('\n');
						}
						address.append('\n');
						if (seller.getZipCode() != null && !seller.getZipCode().isEmpty()) {
							address.append(seller.getZipCode());
							address.append(' ');
						}
						if (seller.getCity() != null && !seller.getCity().isEmpty()) {
							address.append(seller.getCity());
						}
						if (seller.getAddressAppendix() == null || seller.getAddressAppendix().isEmpty()) {
							address.append('\n');
						}
						if (seller.getStreet() == null || seller.getStreet().isEmpty()) {
							address.append('\n');
						}
						
						String accountHolder = "°+°__\240                                              \240__";
						String iban = accountHolder;
						String bic = accountHolder;
						if (seller.getIBAN() != null && seller.getIBAN().length() > 3) {
							accountHolder = "**";
							if (seller.getAccountHolder() == null || seller.getAccountHolder().isEmpty()) {
								if (seller.getGivenName() != null && !seller.getGivenName().isEmpty()) {
									accountHolder += seller.getGivenName() + " ";
								}
								if (seller.getName() != null && !seller.getName().isEmpty()) {
									accountHolder += seller.getName();
								}
							} else {
								accountHolder += seller.getAccountHolder();
							}
							accountHolder += "**";
							iban = "**" + seller.getIBAN() + "**";
							bic = "**";
							if (seller.getBIC() != null && seller.getBIC().length() > 3) {
								bic += seller.getBIC();
							}
							bic += "**";
						}
						
						report.resetPageCount();
						report.setHeader(new PDFExtension("°+°**Verkäufer " + seller.getNumber() + "**°°\n" + getEvent().getName()));
						report.setFooter(new PDFExtension("- ## -"));
						
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(address.toString(), PDFAlignment.LEFT));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph("°+°**Abrechnung", PDFAlignment.CENTER));
						report.add(new PDFParagraph(" "));
						
						PDFTable table = new PDFTable(sales);
						table.addColumn("Kasse", "pos", 10);
						table.addColumn("Beleg", "receipt", 10);
						table.addColumn("Kassierer", "cashier", 10);
						table.addColumn("Zeitpunkt", "timestamp", 20, new TimestampFormatter());
						table.addColumn("Umsatz", "value", 10, PDFAlignment.RIGHT, new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS), new BigDecimalSum(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS)));
						report.add(table);
						
						report.add(new PDFParagraph(" "));
						
						PDFGrid grid = new PDFGrid(true, 5, 40, 20, 40);
						grid.setCell(0, 0, new PDFParagraph("Gesamtanzahl", PDFAlignment.RIGHT));
						grid.setCell(0, 1, new PDFParagraph("**" + itemCount, PDFAlignment.RIGHT));
						grid.setCell(2, 0, new PDFParagraph("Gesamtumsatz", PDFAlignment.RIGHT));
						grid.setCell(2, 1, new PDFParagraph("**" + FORMATTER.format(turnover), PDFAlignment.RIGHT));
						grid.setCell(2, 2, new PDFParagraph("**€"));
						grid.setCell(3, 0, new PDFParagraph("abzüglich " + FORMATTER.format(commissionRate) + "% Provision", PDFAlignment.RIGHT));
						grid.setCell(3, 1, new PDFParagraph("**" + FORMATTER.format(commission.negate()), PDFAlignment.RIGHT));
						grid.setCell(3, 2, new PDFParagraph("**€"));
						grid.setCell(4, 0, new PDFParagraph("°+°Auszahlungsbetrag", PDFAlignment.RIGHT));
						grid.setCell(4, 1, new PDFParagraph("°+°**" + FORMATTER.format(payout), PDFAlignment.RIGHT));
						grid.setCell(4, 2, new PDFParagraph("°+°**€"));
						report.add(grid);
						
						report.newPage();
						report.setFooter(new PDFExtension(" "));
						
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(address.toString(), PDFAlignment.LEFT));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph("°+°**Warenempfangsquittung", PDFAlignment.CENTER));
						report.add(new PDFParagraph(" "));
						
						report.add(new PDFParagraph("Hiermit bestätige ich, " + seller.getGivenName() + " " + seller.getName() + ", den Erhalt der Abrechnung über meine verkaufte Ware (" + itemCount + "\240Artikel in einem Gesamtwert von " + FORMATTER.format(turnover) + "\240€) sowie meiner nicht verkauften Ware."));
						report.add(new PDFParagraph(" "));
						PDFGrid signuture = new PDFGrid(false, 3, 60, 40);
						signuture.setCell(0, 1, new PDFParagraph("°+°__\240                                    \240__", PDFAlignment.CENTER));
						signuture.setCell(2, 1, new PDFParagraph("°-°(Ort, Datum, Unterschrift)", PDFAlignment.CENTER));
						report.add(signuture);
						
						report.newPage();
						
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph(address.toString(), PDFAlignment.LEFT));
						report.add(new PDFParagraph(" "));
						report.add(new PDFParagraph("°+°**Auszahlungsquittung", PDFAlignment.CENTER));
						report.add(new PDFParagraph(" "));
						
						report.add(new PDFParagraph("Die Abrechnung meiner verkauften Ware lautet wie folgt:"));
						report.add(new PDFParagraph(" "));
						report.add(grid);
						report.add(new PDFParagraph(" "));
						PDFList payoutOptions = new PDFList();
						payoutOptions.addItem(new PDFParagraph("Ich bitte um Überweisung des Auszahlungsbetrages auf folgendes Konto:\n°°Kontoinhaber: " + accountHolder + "\n°°IBAN: " + iban + "\n°°BIC: " + bic + "\n "));
						payoutOptions.addItem(new PDFParagraph("Ich bestätige den Erhalt des Auszahlungsbetrages in Bar.\n "));
						report.add(payoutOptions);
						report.add(new PDFParagraph("°-°(unzutreffendes bitte streichen)"));
						report.add(new PDFParagraph(" "));
						report.add(signuture);
					}
				}
				
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}

}

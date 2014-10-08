/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.dialog;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableItem;
import org.xml.sax.SAXException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.formatter.EmptyIntegerFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptStateFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptTypeFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.gui.object.ReceiptSummary;
import de.petzi_net.jflohmarkt.gui.object.ReceiptSummary.ImportState;
import de.petzi_net.jflohmarkt.pos.xml.DenominationLine;
import de.petzi_net.jflohmarkt.pos.xml.DenominationReceipt;
import de.petzi_net.jflohmarkt.pos.xml.Dropoff;
import de.petzi_net.jflohmarkt.pos.xml.Inventory;
import de.petzi_net.jflohmarkt.pos.xml.ItemLine;
import de.petzi_net.jflohmarkt.pos.xml.PaymentLine;
import de.petzi_net.jflohmarkt.pos.xml.Pickup;
import de.petzi_net.jflohmarkt.pos.xml.Sale;
import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.formatter.TableFormatter;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Color;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class ReceiptImportDialog extends AbstractDialog<Boolean> {

	private File[] importFiles;
	private Boolean success;
	private Event event;
	Map<Integer, String> posMap;
	Map<Integer, String> cashierMap;
	Map<Integer, String> sellerMap;
	
	public ReceiptImportDialog(int position) {
		super(position);
		setTitle("Belege-Import");
		setSize(1000, 500);
	}

	@Override
	protected void paint(Composite parent) throws Exception {
		final List<ReceiptSummary> summaries = createSummaries();
		
		TableFormatter tableFormatter = new TableFormatter() {
			
			@Override
			public void format(TableItem item) {
				if (item != null && item.getData() instanceof ReceiptSummary) {
					ImportState importState = ((ReceiptSummary) item.getData()).getImportState();
					switch (importState == null ? ImportState.KNOWN_DIFFERENT : importState) {
					case KNOWN:
						item.setForeground(Color.COMMENT.getSWTColor());
						break;
					case KNOWN_DIFFERENT:
						item.setForeground(Color.ERROR.getSWTColor());
						break;
					case KNOWN_UPDATABLE:
						break;
					case UNKNOWN:
						break;
					}
				}
			}
			
		};
		
		TablePart summaryTable = new TablePart(summaries, null);
		summaryTable.addColumn("Kasse", "pos");
		summaryTable.addColumn("Beleg", "receipt");
		summaryTable.addColumn("Typ", "type", new ReceiptTypeFormatter());
		summaryTable.addColumn("Kassierer", "cashier");
		summaryTable.addColumn("Status", "state", new ReceiptStateFormatter());
		summaryTable.addColumn("Zeitpunkt", "timestamp", new TimestampFormatter());
		summaryTable.addColumn("Anzahl", "quantity", new EmptyIntegerFormatter());
		summaryTable.addColumn("Wert", "value");
		summaryTable.setFormatter(tableFormatter);
		summaryTable.paint(parent);
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Abbrechen", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				success = false;
				close();
			}
			
		});
		buttons.addButton("Übernehmen", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				success = false;
				try {
					importReceipts(summaries);
					success = true;
					GUI.getStatusBar().setSuccessText("Import erfolgreich durchgeführt");
				} finally {
					close();
				}
			}
			
		}, null, true);
		buttons.paint(parent);
	}

	@Override
	protected Boolean getData() throws Exception {
		return success;
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
	
	public Map<Integer, String> getPOSMap() throws ApplicationException {
		if (posMap == null) {
			try {
				posMap = new HashMap<Integer, String>();
				DBService service = JFlohmarktPlugin.getDBService();
				DBIterator poses = service.createList(POS.class);
				poses.addFilter("event=?", getEvent().getID());
				while (poses.hasNext()) {
					POS pos = (POS) poses.next();
					posMap.put(pos.getNumber(), pos.getID());
				}
			} catch (RemoteException e) {
				throw new ApplicationException(e);
			}
		}
		return posMap;
	}
	
	public Map<Integer, String> getCashierMap() throws ApplicationException {
		if (cashierMap == null) {
			try {
				cashierMap = new HashMap<Integer, String>();
				DBService service = JFlohmarktPlugin.getDBService();
				DBIterator cashiers = service.createList(Cashier.class);
				cashiers.addFilter("event=?", getEvent().getID());
				while (cashiers.hasNext()) {
					Cashier cashier = (Cashier) cashiers.next();
					cashierMap.put(cashier.getNumber(), cashier.getID());
				}
			} catch (RemoteException e) {
				throw new ApplicationException(e);
			}
		}
		return cashierMap;
	}
	
	public Map<Integer, String> getSellerMap() throws ApplicationException {
		if (sellerMap == null) {
			try {
				sellerMap = new HashMap<Integer, String>();
				DBService service = JFlohmarktPlugin.getDBService();
				DBIterator sellers = service.createList(Seller.class);
				sellers.addFilter("event=?", getEvent().getID());
				while (sellers.hasNext()) {
					Seller seller = (Seller) sellers.next();
					sellerMap.put(seller.getNumber(), seller.getID());
				}
			} catch (RemoteException e) {
				throw new ApplicationException(e);
			}
		}
		return sellerMap;
	}
	
	public File[] getImportFiles() {
		return importFiles.clone();
	}
	
	public void setImportFiles(File[] importFiles) {
		this.importFiles = importFiles.clone();
	}
	
	private List<de.petzi_net.jflohmarkt.pos.xml.Receipt> readXMLReceipts() throws ApplicationException {
		List<de.petzi_net.jflohmarkt.pos.xml.Receipt> xmlReceipts = new ArrayList<de.petzi_net.jflohmarkt.pos.xml.Receipt>();
		for (File importFile : getImportFiles()) {
			try {
				ZipFile zip = new ZipFile(importFile);
				try {
					SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
					Schema schema = schemaFactory.newSchema(getClass().getClassLoader().getResource("de/petzi_net/jflohmarkt/pos/xml/jflohmarktpos.xsd"));
					JAXBContext jaxbContext = JAXBContext.newInstance(de.petzi_net.jflohmarkt.pos.xml.Receipt.class.getPackage().getName());
					Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
					unmarshaller.setSchema(schema);
					for (Enumeration<? extends ZipEntry> enumeration = zip.entries(); enumeration.hasMoreElements();) {
						ZipEntry entry = enumeration.nextElement();
						if (!entry.isDirectory()) {
							if (entry.getName().matches(".*receipt_[0-9]+_[0-9]+.xml")) {
								Logger.info("importing receipt: " + entry.getName());
								xmlReceipts.add((de.petzi_net.jflohmarkt.pos.xml.Receipt) unmarshaller.unmarshal(zip.getInputStream(entry)));
							}
						}
					}
				} finally {
					zip.close();
				}
			} catch (JAXBException e) {
				Logger.error("cannot read import zip file: " + importFile, e);
				throw new ApplicationException("Import-Datei kann nicht gelesen werden: " + importFile, e);
			} catch (SAXException e) {
				Logger.error("cannot read import zip file: " + importFile, e);
				throw new ApplicationException("Import-Datei kann nicht gelesen werden: " + importFile, e);
			} catch (ZipException e) {
				Logger.error("cannot read import zip file: " + importFile, e);
				throw new ApplicationException("Import-Datei kann nicht gelesen werden: " + importFile, e);
			} catch (IOException e) {
				Logger.error("cannot read import zip file: " + importFile, e);
				throw new ApplicationException("Import-Datei kann nicht gelesen werden: " + importFile, e);
			}
		}
		return xmlReceipts;
	}
	
	private List<ReceiptSummary> createSummaries() throws ApplicationException {
		List<de.petzi_net.jflohmarkt.pos.xml.Receipt> xmlReceipts = readXMLReceipts();
		List<ReceiptSummary> summaries = new ArrayList<ReceiptSummary>(xmlReceipts.size());
		for (de.petzi_net.jflohmarkt.pos.xml.Receipt xmlReceipt : xmlReceipts) {
			ReceiptSummary summary = new ReceiptSummary();
			summary.setXmlReceipt(xmlReceipt);
			summary.setImportState(ImportState.UNKNOWN);
			summary.setId(null);
			summary.setPos(xmlReceipt.getPos());
			summary.setReceipt(xmlReceipt.getReceipt());
			summary.setCashier(xmlReceipt.getCashier());
			switch (xmlReceipt.getState()) {
			case ACTIVE:
				summary.setState(Receipt.STATE_ACTIVE);
				break;
			case FINISHED:
				summary.setState(Receipt.STATE_FINISHED);
				break;
			case ABORTED:
				summary.setState(Receipt.STATE_ABORTED);
				break;
			}
			summary.setTimestamp(xmlReceipt.getTimestamp().toGregorianCalendar().getTime());
			
			if (xmlReceipt instanceof Sale) {
				Sale sale = (Sale) xmlReceipt;
				summary.setType(Receipt.TYPE_SALE);
				summary.setQuantity(0);
				summary.setValue(BigDecimal.ZERO);
				for (ItemLine line : sale.getItem()) {
					if (line.isValid()) {
						summary.setQuantity(summary.getQuantity() + 1);
						summary.setValue(summary.getValue().add(line.getPrice()));
					}
				}
				summaries.add(summary);
			} else if (xmlReceipt instanceof DenominationReceipt) {
				DenominationReceipt denominationReceipt = (DenominationReceipt) xmlReceipt;
				summary.setQuantity(0);
				summary.setValue(BigDecimal.ZERO);
				for (DenominationLine line : denominationReceipt.getDenomination()) {
					if (line.isValid()) {
						summary.setValue(summary.getValue().add(line.getValue().multiply(BigDecimal.valueOf(line.getQuantity()))));
					}
				}
				if (xmlReceipt instanceof Pickup) {
					summary.setType(Receipt.TYPE_PICKUP);
					summaries.add(summary);
				} else if (xmlReceipt instanceof Dropoff) {
					summary.setType(Receipt.TYPE_DROPOFF);
					summaries.add(summary);
				} else if (xmlReceipt instanceof Inventory) {
					summary.setType(Receipt.TYPE_INVENTORY);
					summaries.add(summary);
				} else {
					Logger.error("invalid receipt type: " + xmlReceipt.getClass());
				}
			} else {
				Logger.error("invalid receipt type: " + xmlReceipt.getClass());
			}
			
			if (getPOSMap().containsKey(xmlReceipt.getPos())) {
				try {
					DBService service = JFlohmarktPlugin.getDBService();
					DBIterator result = service.createList(Receipt.class);
					result.addFilter("pos=? and number=?", getPOSMap().get(xmlReceipt.getPos()), xmlReceipt.getReceipt());
					if (result.hasNext()) {
						Receipt receipt = (Receipt) result.next();
						summary.setId(receipt.getID());
						if (receipt.getType() != summary.getType()) {
							summary.setImportState(ImportState.KNOWN_DIFFERENT);
						} else {
							if (receipt.getState() == Receipt.STATE_ACTIVE) {
								summary.setImportState(ImportState.KNOWN_UPDATABLE);
							} else {
								if (receipt.getTimestamp().getTime() == summary.getTimestamp().getTime()) {
									summary.setImportState(ImportState.KNOWN);
								} else {
									summary.setImportState(ImportState.KNOWN_DIFFERENT);
								}
							}
						}
					}
				} catch (RemoteException e) {
					Logger.error("cannot seek already imported receipts", e);
					throw new ApplicationException(e);
				}
			}
		}
		return summaries;
	}
	
	private void importReceipts(List<ReceiptSummary> summaries) throws ApplicationException {
		try {
			DBService service = JFlohmarktPlugin.getDBService();
			for (ReceiptSummary summary : summaries) {
				ImportState importState = summary.getImportState();
				switch (importState == null ? ImportState.KNOWN_DIFFERENT : importState) {
				case KNOWN_UPDATABLE:
					updateReceipt(service, summary);
					break;
				case UNKNOWN:
					insertReceipt(service, summary);
					break;
				default:
					break;
				}
			}
		} catch (RemoteException e) {
			Logger.error("cannot import receipts", e);
			throw new ApplicationException(e);
		}
	}
	
	private void ensurePOS(DBService service, int posNumber) throws ApplicationException, RemoteException {
		if (!getPOSMap().containsKey(posNumber)) {
			POS pos = (POS) service.createObject(POS.class, null);
			pos.setEvent(Long.valueOf(getEvent().getID()));
			pos.setNumber(posNumber);
			pos.setDescription(String.valueOf(posNumber));
			pos.store();
			posMap = null;
		}
	}
	
	private void ensureCashier(DBService service, int cashierNumber) throws ApplicationException, RemoteException {
		if (!getCashierMap().containsKey(cashierNumber)) {
			Cashier cashier = (Cashier) service.createObject(Cashier.class, null);
			cashier.setEvent(Long.valueOf(getEvent().getID()));
			cashier.setNumber(cashierNumber);
			cashier.setName(String.valueOf(cashierNumber));
			cashier.setGivenName("");
			cashier.store();
			cashierMap = null;
		}
	}
	
	private void ensureSeller(DBService service, int sellerNumber) throws ApplicationException, RemoteException {
		if (!getSellerMap().containsKey(sellerNumber)) {
			Seller seller = (Seller) service.createObject(Seller.class, null);
			seller.setEvent(Long.valueOf(getEvent().getID()));
			seller.setNumber(sellerNumber);
			seller.setName(String.valueOf(sellerNumber));
			seller.setGivenName("");
			seller.setStreet("");
			seller.setZipCode("");
			seller.setCity("");
			seller.setEmail("");
			seller.store();
			sellerMap = null;
		}
	}
	
	private void insertReceipt(DBService service, ReceiptSummary summary) throws RemoteException, ApplicationException {
		ensurePOS(service, summary.getPos());
		ensureCashier(service, summary.getCashier());
		
		Receipt receipt = (Receipt) service.createObject(Receipt.class, null);
		receipt.setPOS(Long.valueOf(getPOSMap().get(summary.getPos())));
		receipt.setNumber(summary.getReceipt());
		receipt.setType(summary.getType());
		receipt.setState(Receipt.STATE_ACTIVE);
		receipt.setCashier(Long.valueOf(getCashierMap().get(summary.getCashier())));
		receipt.setTimestamp(summary.getTimestamp());
		receipt.store();
		
		if (summary.getXmlReceipt() instanceof Sale) {
			Sale sale = (Sale) summary.getXmlReceipt();
			int counter = 0;
			for (ItemLine line : sale.getItem()) {
				ensureSeller(service, line.getSeller());
				
				ReceiptLine receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
				receiptLine.setReceipt(Long.valueOf(receipt.getID()));
				receiptLine.setLine(++counter);
				receiptLine.setSeller(Long.valueOf(getSellerMap().get(line.getSeller())));
				receiptLine.setValue(line.getPrice());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
			for (PaymentLine line : sale.getPayment()) {
				ReceiptLine receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
				receiptLine.setReceipt(Long.valueOf(receipt.getID()));
				receiptLine.setLine(++counter);
				receiptLine.setValue(line.getAmount());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
		} else if (summary.getXmlReceipt() instanceof DenominationReceipt) {
			DenominationReceipt denominationReceipt = (DenominationReceipt) summary.getXmlReceipt();
			int counter = 0;
			for (DenominationLine line : denominationReceipt.getDenomination()) {
				ReceiptLine receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
				receiptLine.setReceipt(Long.valueOf(receipt.getID()));
				receiptLine.setLine(++counter);
				receiptLine.setQuantity(line.getQuantity());
				receiptLine.setValue(line.getValue());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
		} else {
			Logger.error("unknown xml receipt type: " + summary.getXmlReceipt().getClass());
			throw new ApplicationException("Unbekannter Belegtyp: " + summary.getXmlReceipt().getClass());
		}
		
		receipt.setState(summary.getState());
		receipt.store();
	}
	
	private void updateReceipt(DBService service, ReceiptSummary summary) throws RemoteException, ApplicationException {
		ensurePOS(service, summary.getPos());
		ensureCashier(service, summary.getCashier());
		
		Receipt receipt = (Receipt) service.createObject(Receipt.class, summary.getId());
		receipt.setType(summary.getType());
		receipt.setState(Receipt.STATE_ACTIVE);
		receipt.setCashier(Long.valueOf(getCashierMap().get(summary.getCashier())));
		receipt.setTimestamp(summary.getTimestamp());
		receipt.store();
		
		Map<Integer, ReceiptLine> lineMap = new HashMap<Integer, ReceiptLine>();
		DBIterator lineIterator = service.createList(ReceiptLine.class);
		lineIterator.addFilter("receipt=?", receipt.getID());
		while (lineIterator.hasNext()) {
			ReceiptLine line = (ReceiptLine) lineIterator.next();
			lineMap.put(line.getLine(), line);
		}
		
		if (summary.getXmlReceipt() instanceof Sale) {
			Sale sale = (Sale) summary.getXmlReceipt();
			int counter = 0;
			for (ItemLine line : sale.getItem()) {
				ensureSeller(service, line.getSeller());
				
				ReceiptLine receiptLine = lineMap.remove(++counter);
				if (receiptLine == null) {
					receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
					receiptLine.setReceipt(Long.valueOf(receipt.getID()));
					receiptLine.setLine(counter);
				}
				receiptLine.setSeller(Long.valueOf(getSellerMap().get(line.getSeller())));
				receiptLine.setQuantity(null);
				receiptLine.setValue(line.getPrice());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
			for (PaymentLine line : sale.getPayment()) {
				ReceiptLine receiptLine = lineMap.remove(++counter);
				if (receiptLine == null) {
					receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
					receiptLine.setReceipt(Long.valueOf(receipt.getID()));
					receiptLine.setLine(counter);
				}
				receiptLine.setSeller(null);
				receiptLine.setQuantity(null);
				receiptLine.setValue(line.getAmount());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
		} else if (summary.getXmlReceipt() instanceof DenominationReceipt) {
			DenominationReceipt denominationReceipt = (DenominationReceipt) summary.getXmlReceipt();
			int counter = 0;
			for (DenominationLine line : denominationReceipt.getDenomination()) {
				ReceiptLine receiptLine = lineMap.remove(++counter);
				if (receiptLine == null) {
					receiptLine = (ReceiptLine) service.createObject(ReceiptLine.class, null);
					receiptLine.setReceipt(Long.valueOf(receipt.getID()));
					receiptLine.setLine(counter);
				}
				receiptLine.setSeller(null);
				receiptLine.setQuantity(line.getQuantity());
				receiptLine.setValue(line.getValue());
				receiptLine.setValid(line.isValid());
				receiptLine.setTimestamp(line.getTimestamp().toGregorianCalendar().getTime());
				receiptLine.store();
			}
		} else {
			Logger.error("unknown xml receipt type: " + summary.getXmlReceipt().getClass());
			throw new ApplicationException("Unbekannter Belegtyp: " + summary.getXmlReceipt().getClass());
		}
		
		for (Map.Entry<Integer, ReceiptLine> entry : lineMap.entrySet()) {
			entry.getValue().delete();
		}
		
		receipt.setState(summary.getState());
		receipt.store();
	}

}

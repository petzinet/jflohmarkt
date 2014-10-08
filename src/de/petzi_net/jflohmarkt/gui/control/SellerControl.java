/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.control;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.action.SellerDetailAction;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.petzi_net.jflohmarkt.report.PDFExtension;
import de.petzi_net.jflohmarkt.report.PDFParagraph;
import de.petzi_net.jflohmarkt.report.PDFReport;
import de.petzi_net.jflohmarkt.report.PDFTable;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.datasource.rmi.DBIterator;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.gui.AbstractControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.DateInput;
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
			sellerTable.setMulti(true);
		}
		return sellerTable;
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
			PDFReport.produceReportFile("Verkäufer", getEvent().getName(), 8, true, new PDFReport.Producer() {
				
				@Override
				public void produce(PDFReport report) throws ApplicationException {
					try {
						PDFTable sellerTable = new PDFTable(getSellerTable().getItems());
						sellerTable.addColumn("Nummer", "number", 5);
						sellerTable.addColumn("Name", "name", 10);
						sellerTable.addColumn("Vorname", "givenname", 10);
						sellerTable.addColumn("Straße", "street", 10);
						sellerTable.addColumn("PLZ", "zipcode", 4);
						sellerTable.addColumn("Ort", "city", 10);
						sellerTable.addColumn("Telefon", "phone", 10);
						sellerTable.addColumn("Email", "email", 10);
						sellerTable.addColumn("Registrierung", "registration", 8, new TimestampFormatter());
						report.add(sellerTable);
					} catch (RemoteException e) {
						throw new ApplicationException(e);
					}
				}
			});
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}
	
	public void printTurnover() throws ApplicationException {
		FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
	    fd.setText("Ausgabedatei wählen...");
	    fd.setFileName("/home/axel/ZZZZtest.pdf");
	    fd.setFilterExtensions(new String[] { "*.pdf" });
	    String filePath = fd.open();
	    if (filePath == null || filePath.length() == 0) {
	    	return;
	    }
	    if (!filePath.toLowerCase().endsWith(".pdf")) {
	    	filePath = filePath + ".pdf";
	    }
	    File file = new File(filePath);
		
	    try {
			OutputStream os = new FileOutputStream(file);
			try {
				PDFExtension header = new PDFExtension("**Das ist ein °8°Text°° auf Seite ##.** \n°16°Alles klar°°?");
				PDFExtension footer = new PDFExtension("Das ist ein Text auf Seite ##. \nNa klar!");
				PDFReport report = new PDFReport(os, "Verkäufer", "Test", 10, false);
				report.setHeader(header);
				report.setFooter(footer);
				PDFTable sellerTable = new PDFTable(getSellerTable().getItems());
				sellerTable.addColumn("Nummer", "number", 10);
				sellerTable.addColumn("Name", "name", 10);
				sellerTable.addColumn("Vorname", "givenname", 10);
				sellerTable.addColumn("Straße", "street", 10);
				sellerTable.addColumn("PLZ", "zipcode", 10);
				sellerTable.addColumn("Ort", "city", 10);
				sellerTable.addColumn("Telefon", "phone", 10);
				sellerTable.addColumn("Email", "email", 10);
				sellerTable.addColumn("Registrierung", "registration", 10);
				report.add(sellerTable);
				report.setHeader(new PDFExtension("blub"));
				report.resetPageCount();
				report.add(new PDFParagraph("Lorem ipsum dolor **sit amet, //consetetur// sadipscing elitr**, sed diam __nonumy eirmod tempor__ invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua.\n At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet."));
				report.close();
				
//				try {
//					Runtime.getRuntime().exec("/usr/bin/okular", new String[]{file.getPath()});
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
//		    	try {
//					Document doc = new Document();
//					doc.setMargins(40, 20, 20, 40);
//					PdfWriter.getInstance(doc, os);
//					doc.addAuthor("JFlohmarkt");
//					doc.addTitle("Verkäuferumsätze");
//					doc.open();
//					doc.newPage();
//					PdfPTable table = new PdfPTable(3);
//					PdfPCell cell11 = new PdfPCell(new Phrase("hallo1"));
//					cell11.setBackgroundColor(new Color(192, 192, 192));
//					table.addCell(cell11);
//					table.addCell(new PdfPCell(new Phrase("hallo2")));
//					table.addCell(new PdfPCell(new Phrase("hallo3")));
//					table.setHeaderRows(1);
//					for (int n = 0; n < 40; n++) {
//						PdfPCell cell21 = new PdfPCell(new Phrase("tach1"));
//						cell21.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
//						table.addCell(cell21);
//						table.addCell(new PdfPCell(new Phrase("tach2")));
//						table.addCell(new PdfPCell(new Phrase("tach3")));
////					table.completeRow();
//						PdfPCell cell31 = new PdfPCell(new Phrase("moin1"));
//						cell31.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
//						cell31.setBackgroundColor(new Color(224, 224, 224));
//						table.addCell(cell31);
//						table.addCell(new PdfPCell(new Phrase("moin2")));
//						table.addCell(new PdfPCell(new Phrase("moin3")));
////					table.completeRow();
//					}
//					doc.add(table);
//					doc.close();
//				} catch (DocumentException e) {
//					throw new ApplicationException(e);
//				}
			} finally {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
	    } catch (RemoteException e) {
	    	throw new ApplicationException(e);
		} catch (FileNotFoundException e) {
	    	throw new ApplicationException(e);
		}
	}

}

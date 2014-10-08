/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.dialog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.view.SellerDetailView;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.TextAreaInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SellerImportDialog extends AbstractDialog<Seller> {

	private final DateFormat XML_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	public SellerImportDialog(int position) {
		super(position);
		setTitle("Verkäufer-Import");
		setSize(700, 500);
	}

	@Override
	protected void paint(Composite parent) throws Exception {
		final TextAreaInput base64Input = new TextAreaInput("");
		base64Input.setHeight(400);
		base64Input.paint(parent);
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Übernehmen", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				String text = (String) base64Input.getValue();
				if (text != null) {
					text = text.replaceAll("\\s", "");
					if (!text.isEmpty()) {
						Seller seller = convert(text);
						if (seller != null) {
							close();
							GUI.startView(SellerDetailView.class, seller);
						}
//						try {
//							text = new String(Base64.decodeBase64(text), "UTF-8");
//						} catch (UnsupportedEncodingException e) {
//						}
					}
				}
//				base64Input.setValue(text);
			}
			
		});
		buttons.addButton("Abbrechen", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				close();
			}
			
		});
		buttons.paint(parent);
	}

	@Override
	protected Seller getData() throws Exception {
		return null;
	}
	
	private Seller convert(String text) {
		if (text != null) {
			text = text.replaceAll("\\s", "");
			if (!text.isEmpty()) {
				byte[] data = Base64.decodeBase64(text);
				try {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(data));
					Element root = doc.getDocumentElement();
					if ("seller".equals(root.getTagName())) {
						Seller result = (Seller) JFlohmarktPlugin.getDBService().createObject(Seller.class, null);
						result.setNumber(Integer.valueOf(getContent(root, "number")));
						result.setName(getContent(root, "name"));
						result.setGivenName(getContent(root, "givenName"));
						result.setAddressAppendix(getContent(root, "addressAppendix"));
						result.setStreet(getContent(root, "street"));
						result.setZipCode(getContent(root, "zipCode"));
						result.setCity(getContent(root, "city"));
						result.setPhone(getContent(root, "phone"));
						result.setEmail(getContent(root, "email"));
						result.setAccountHolder(getContent(root, "accountHolder"));
						result.setBIC(getContent(root, "bic"));
						result.setIBAN(getContent(root, "iban"));
						result.setRegistration(XML_TIMESTAMP.parse(getContent(root, "registration")));
						return result;
					}
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	private static String getContent(Element element, String tag) {
		NodeList subs = element.getElementsByTagName(tag);
		if (subs != null && subs.getLength() > 0) {
			return subs.item(0).getTextContent();
		}
		return null;
	}

}

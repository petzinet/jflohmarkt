/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.control.SaleLineControl;
import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.BooleanFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.TimestampFormatter;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SaleLineView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Verkaufszeile");
		
		final SaleLineControl control = new SaleLineControl(this);
		
		Container container = new SimpleContainer(getParent());
		container.addHeadline("Details");
		if (control.getEvent() != null) {
			LabelInput input = new LabelInput(control.getEvent().getName());
			input.setName("Veranstaltung");
			container.addInput(input);
		}
		if (control.getPOS() != null) {
			LabelInput input = new LabelInput(String.valueOf(control.getPOS().getNumber()));
			input.setName("Kasse");
			container.addInput(input);
		}
		if (control.getReceipt() != null) {
			LabelInput input = new LabelInput(String.valueOf(control.getReceipt().getNumber()));
			input.setName("Beleg");
			container.addInput(input);
		}
		if (control.getReceipt() != null) {
			LabelInput line = new LabelInput(String.valueOf(control.getReceiptLine().getLine()));
			line.setName("Zeile");
			container.addInput(line);
			LabelInput value = new LabelInput(new BigDecimalFormatter(BigDecimalFormatter.TWO_DECIMALS).format(control.getReceiptLine().getValue()));
			value.setName("Preis");
			container.addInput(value);
			LabelInput valid = new LabelInput(new BooleanFormatter("ja", "nein").format(control.getReceiptLine().isValid()));
			valid.setName("Gültig");
			container.addInput(valid);
			LabelInput timestamp = new LabelInput(new TimestampFormatter().format(control.getReceiptLine().getTimestamp()));
			timestamp.setName("Zeitpunkt");
			container.addInput(timestamp);
		}
		container.addInput(control.getSellerSelection());
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Speichern", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.handleStore();
			}
			
		}, null, true);
		buttons.paint(getParent());
	}

}

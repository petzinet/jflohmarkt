/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.control.ReceiptControl;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptStateFormatter;
import de.petzi_net.jflohmarkt.gui.formatter.ReceiptTypeFormatter;
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
public class ReceiptDetailView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Beleg");
		
		final ReceiptControl control = new ReceiptControl(this);
		
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
			LabelInput receipt = new LabelInput(String.valueOf(control.getReceipt().getNumber()));
			receipt.setName("Beleg");
			container.addInput(receipt);
			LabelInput type = new LabelInput(new ReceiptTypeFormatter().format(control.getReceipt().getType()));
			type.setName("Typ");
			container.addInput(type);
			LabelInput state = new LabelInput(new ReceiptStateFormatter().format(control.getReceipt().getState()));
			state.setName("Status");
			container.addInput(state);
			LabelInput timestamp = new LabelInput(new TimestampFormatter().format(control.getReceipt().getTimestamp()));
			timestamp.setName("Zeitpunkt");
			container.addInput(timestamp);
		}
		container.addInput(control.getCashierSelection());
		
		control.getReceiptLineTable().paint(getParent());
		
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

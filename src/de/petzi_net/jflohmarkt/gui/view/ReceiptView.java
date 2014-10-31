/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.control.POSControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;

/**
 * @author axel
 *
 */
public class ReceiptView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Belege");
		
		final POSControl control = new POSControl(this);
		
		Container container = new SimpleContainer(getParent());
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
		
		control.getReceiptTable().paint(getParent());
		
//		ButtonArea buttons = new ButtonArea();
//		buttons.addButton("Löschen", null);
//		buttons.addButton("Speichern", new Action() {
//			
//			@Override
//			public void handleAction(Object context) throws ApplicationException {
//				control.handleStore();
//			}
//			
//		}, null, true);
//		buttons.paint(getParent());
	}

}

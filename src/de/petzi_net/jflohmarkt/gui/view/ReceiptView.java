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
		
		if (control.getPOS() != null) {
			Container container = new SimpleContainer(getParent());
			container.addInput(new LabelInput("Kasse " + control.getPOS().getNumber()));
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

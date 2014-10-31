/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.control.SellerControl;
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
public class SellerDetailView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Verkäufer");
		
		final SellerControl control = new SellerControl(this);
		
		Container container = new SimpleContainer(getParent());
		container.addHeadline("Details");
		if (control.getEvent() != null) {
			LabelInput input = new LabelInput(control.getEvent().getName());
			input.setName("Veranstaltung");
			container.addInput(input);
		}
		container.addInput(control.getNumber());
		container.addInput(control.getName());
		container.addInput(control.getGivenName());
		container.addInput(control.getAddressAppendix());
		container.addInput(control.getStreet());
		container.addInput(control.getZipCode());
		container.addInput(control.getCity());
		container.addInput(control.getPhone());
		container.addInput(control.getEmail());
		container.addInput(control.getAccountHolder());
		container.addInput(control.getBIC());
		container.addInput(control.getIBAN());
		container.addInput(control.getRegistration());
		container.addInput(control.getCommissionRate());
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Löschen", null);
		buttons.addButton("Speichern", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.handleStore();
			}
			
		}, null, true);
		buttons.paint(getParent());
	}

}

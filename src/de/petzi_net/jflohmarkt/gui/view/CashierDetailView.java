/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.control.CashierControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class CashierDetailView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Kassierer");
		
		final CashierControl control = new CashierControl(this);
		
		Container container = new SimpleContainer(getParent());
		container.addHeadline("Details");
		container.addInput(control.getNumber());
		container.addInput(control.getName());
		container.addInput(control.getGivenName());
		
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

/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.action.EventDetailAction;
import de.petzi_net.jflohmarkt.gui.control.EventControl;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class EventView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Veranstaltung");
		
		final EventControl control = new EventControl(this);
		
		control.getEventTable().paint(getParent());
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Liste", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printList();
			}
			
		});
		buttons.addButton("Neue Veranstaltung...", new EventDetailAction(), null, true);
		buttons.paint(getParent());
	}

}

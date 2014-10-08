/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import de.petzi_net.jflohmarkt.gui.view.EventView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class EventAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		GUI.startView(EventView.class, null);
	}

}

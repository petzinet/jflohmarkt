/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.view.EventDetailView;
import de.petzi_net.jflohmarkt.rmi.Event;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class EventDetailAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		Event event;
		if (context instanceof Event) {
			event = (Event) context;
		} else {
			try {
				event = (Event) JFlohmarktPlugin.getDBService().createObject(Event.class, null);
			} catch (RemoteException e) {
				throw new ApplicationException("error creating new event", e);
			}
		}
		GUI.startView(EventDetailView.class, event);
	}

}

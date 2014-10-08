/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.view.POSDetailView;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class POSDetailAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		POS pos;
		if (context instanceof POS) {
			pos = (POS) context;
		} else {
			try {
				pos = (POS) JFlohmarktPlugin.getDBService().createObject(POS.class, null);
			} catch (RemoteException e) {
				throw new ApplicationException("error creating new pos", e);
			}
		}
		GUI.startView(POSDetailView.class, pos);
	}

}

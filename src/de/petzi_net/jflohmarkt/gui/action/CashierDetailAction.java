/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.view.CashierDetailView;
import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class CashierDetailAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		Cashier cashier;
		if (context instanceof Cashier) {
			cashier = (Cashier) context;
		} else {
			try {
				cashier = (Cashier) JFlohmarktPlugin.getDBService().createObject(Cashier.class, null);
			} catch (RemoteException e) {
				throw new ApplicationException("error creating new cashier", e);
			}
		}
		GUI.startView(CashierDetailView.class, cashier);
	}

}

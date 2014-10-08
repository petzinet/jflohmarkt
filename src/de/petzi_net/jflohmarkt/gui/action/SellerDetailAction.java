/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.petzi_net.jflohmarkt.gui.view.SellerDetailView;
import de.petzi_net.jflohmarkt.rmi.Seller;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SellerDetailAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		Seller seller;
		if (context instanceof Seller) {
			seller = (Seller) context;
		} else {
			try {
				seller = (Seller) JFlohmarktPlugin.getDBService().createObject(Seller.class, null);
			} catch (RemoteException e) {
				throw new ApplicationException("error creating new seller", e);
			}
		}
		GUI.startView(SellerDetailView.class, seller);
	}

}

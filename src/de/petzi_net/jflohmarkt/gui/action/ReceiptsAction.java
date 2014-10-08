/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import de.petzi_net.jflohmarkt.gui.view.ReceiptView;
import de.petzi_net.jflohmarkt.rmi.POS;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class ReceiptsAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		POS pos;
		if (context instanceof POS) {
			pos = (POS) context;
		} else {
			pos = null;
//			Logger.error("receipt action needs a pos object");
//			throw new ApplicationException("Angabe der Kasse fehlt");
		}
		GUI.startView(ReceiptView.class, pos);
	}

}

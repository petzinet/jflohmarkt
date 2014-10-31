/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import de.petzi_net.jflohmarkt.gui.view.ReceiptDetailView;
import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class ReceiptDetailAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		Receipt receipt;
		if (context instanceof Receipt) {
			receipt = (Receipt) context;
		} else {
			throw new ApplicationException("cannot run action without a receipt");
		}
		GUI.startView(ReceiptDetailView.class, receipt);
	}

}

/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.gui.view.SaleLineView;
import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SaleLineAction implements Action {

	@Override
	public void handleAction(Object context) throws ApplicationException {
		ReceiptLine receiptLine;
		if (context instanceof ReceiptLine) {
			receiptLine = (ReceiptLine) context;
		} else {
			throw new ApplicationException("cannot run action without a receipt line");
		}
		try {
			if (receiptLine != null && receiptLine.getSeller() != null) {
				GUI.startView(SaleLineView.class, receiptLine);
			}
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		}
	}

}

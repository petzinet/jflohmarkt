/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.action;

import de.petzi_net.jflohmarkt.gui.dialog.AboutDialog;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class AboutAction implements Action {
	
	@Override
	public void handleAction(Object context) throws ApplicationException {
		try {
			new AboutDialog(AbstractDialog.POSITION_CENTER).open();
		} catch (Exception e) {
			String fehler = "Fehler beim Öffnen des AboutView-Dialoges";
			Logger.error(fehler, e);
			throw new ApplicationException(fehler);
		}
	}
	
}

/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.petzi_net.jflohmarkt.gui.action.SellerDetailAction;
import de.petzi_net.jflohmarkt.gui.control.SellerControl;
import de.petzi_net.jflohmarkt.gui.dialog.SellerImportDialog;
import de.petzi_net.jflohmarkt.gui.dialog.SellerStatisticsDialog;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class SellerView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Verkäufer");
		
		final SellerControl control = new SellerControl(this);
		
		Container container = new SimpleContainer(getParent());
		container.addInput(control.getEventSelection());
		
		control.getSellerTable().paint(getParent());
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Liste", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printList();
			}
			
		});
		buttons.addButton("Statistik...", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				try {
					new SellerStatisticsDialog(AbstractDialog.POSITION_CENTER).open();
				} catch (Exception e) {
					String fehler = "Fehler beim Öffnen des Verkäufer-Statistik-Dialoges";
					Logger.error(fehler, e);
					throw new ApplicationException(fehler);
				}
			}
			
		});
		buttons.addButton("Umsätze", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printTurnover();
			}
			
		});
		buttons.addButton("Verkäufer importieren...", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				try {
					new SellerImportDialog(AbstractDialog.POSITION_CENTER).open();
				} catch (Exception e) {
					String fehler = "Fehler beim Öffnen des Verkäufer-Import-Dialoges";
					Logger.error(fehler, e);
					throw new ApplicationException(fehler);
				}
			}
			
		});
		buttons.addButton("Neuer Verkäufer...", new SellerDetailAction(), null, true);
		buttons.paint(getParent());
	}

}

/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.petzi_net.jflohmarkt.gui.action.POSDetailAction;
import de.petzi_net.jflohmarkt.gui.action.ReceiptsAction;
import de.petzi_net.jflohmarkt.gui.control.POSControl;
import de.petzi_net.jflohmarkt.gui.dialog.ReceiptImportDialog;
import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.ContextMenu;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class POSView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("Kassen");
		
		final POSControl control = new POSControl(this);
		
		Container container = new SimpleContainer(getParent());
		container.addInput(control.getEventSelection());
		
		ContextMenu ctxMenu = new ContextMenu();
		ctxMenu.addItem(new ContextMenuItem("Bearbeiten", new POSDetailAction()));
		ctxMenu.addItem(new ContextMenuItem("Belege", new ReceiptsAction()));
		
		TablePart posTable = control.getPOSTable();
		posTable.setContextMenu(ctxMenu);
		posTable.paint(getParent());
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Liste", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printList();
			}
			
		});
		buttons.addButton("Journal", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printJournal();
			}
			
		});
		buttons.addButton("Abrechnung", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				control.printReport();
			}
			
		});
		buttons.addButton("Belege importieren...", new Action() {
			
			@Override
			public void handleAction(Object context) throws ApplicationException {
				FileDialog fd = new FileDialog(getParent().getShell(), SWT.MULTI);
				fd.setText("Import-ZIP auswählen...");
				fd.setFilterPath(System.getProperty("user.home"));
				if (fd.open() != null) {
					String[] pathnames = fd.getFileNames();
					if (pathnames != null && pathnames.length > 0) {
						File[] files = new File[pathnames.length];
						for (int n = 0; n < pathnames.length; n++) {
							if (pathnames[n] == null) {
								Logger.warn("illegal null value for import zip file");
								throw new ApplicationException("Import-Datei ist null");
							}
							files[n] = new File(fd.getFilterPath(), pathnames[n]);
							if (!files[n].canRead() || !files[n].isFile()) {
								Logger.warn("import zip file '" + pathnames[n] + "' does not exist or cannot be read");
								throw new ApplicationException("Import-Datei '" + pathnames[n] + "' existiert nicht oder kann nicht gelesen werden.");
							}
						}
						ReceiptImportDialog importDialog = new ReceiptImportDialog(AbstractDialog.POSITION_CENTER);
						importDialog.setImportFiles(files);
						try {
							importDialog.open();
						} catch (Exception e) {
							throw new ApplicationException(e);
						}
					} else {
						Logger.warn("no import zip file selected");
						throw new ApplicationException("Es wurde keine Import-Datei ausgewählt");
					}
				}
			}
			
		});
		buttons.addButton("Neue Kasse...", new POSDetailAction(), null, true);
		buttons.paint(getParent());
	}

}

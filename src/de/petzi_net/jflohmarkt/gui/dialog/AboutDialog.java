/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.dialog;

import org.eclipse.swt.widgets.Composite;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.input.LabelInput;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.FormTextPart;
import de.willuhn.jameica.gui.util.LabelGroup;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class AboutDialog extends AbstractDialog<Object> {

	public AboutDialog(int position) {
		super(position);
		this.setTitle("Über...");
	}

	@Override
	protected void paint(Composite parent) throws Exception {
		FormTextPart text = new FormTextPart();
		text.setText("<form><p><b>"
				+ "Plugin für Nummernflohmärkte unter Jameica"
				+ "</b></p>"
				+ "<br/>Lizenz: GPL [ http://www.gnu.org/copyleft/gpl.html ]"
				+ "<br/><p>Copyright by Axel Petzold [ info@petzi-net.de ]</p></form>");

		text.paint(parent);

		LabelGroup group = new LabelGroup(parent, "Information");

		AbstractPlugin p = Application.getPluginLoader().getPlugin(JFlohmarktPlugin.class);

//		group.addLabelPair("Version", new LabelInput("" + p.getManifest().getVersion()));
//		group.addLabelPair("Build-Date", new LabelInput("" + p.getManifest().getBuildDate()));
//		group.addLabelPair("Build-Nr", new LabelInput("" + p.getManifest().getBuildnumber()));
		group.addLabelPair("Arbeitsverzeichnis", new LabelInput("" + p.getResources().getWorkPath()));
		
		ButtonArea buttons = new ButtonArea();
		buttons.addButton("Schließen" ,new Action() {
			
			public void handleAction(Object context) throws ApplicationException {
				close();
			}
			
		}, null, true);
		buttons.paint(parent);
		
		getShell().pack();
	}

	@Override
	protected Object getData() throws Exception {
		return null;
	}

}

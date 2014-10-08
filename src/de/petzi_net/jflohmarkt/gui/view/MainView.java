/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.view;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.util.LabelGroup;

/**
 * @author axel
 *
 */
public class MainView extends AbstractView {

	@Override
	public void bind() throws Exception {
		GUI.getView().setTitle("JFlohmarkt");
		
		LabelGroup group = new LabelGroup(getParent(), "JFlohmarkt");
		group.addText("Willkommen zu JFlohmarkt", true);
	}

}

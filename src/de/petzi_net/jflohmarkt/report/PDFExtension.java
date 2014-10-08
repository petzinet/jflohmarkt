/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import com.lowagie.text.DocumentException;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Phrase;

import de.petzi_net.jflohmarkt.report.PDFParagraph.FontStyle;

/**
 * @author axel
 *
 */
public class PDFExtension {
	
	private final String text;
	
	public PDFExtension(String text) {
		this.text = text;
	}
	
	HeaderFooter build(int fontSize) throws DocumentException {
		HeaderFooter extension;
		int pos = text.indexOf("##");
		if (pos < 0) {
			extension = new HeaderFooter(PDFParagraph.makePhrase(new Phrase(), text, null, fontSize), false);
			extension.getBefore().setLeading(extension.getBefore().getFont().getCalculatedLeading(1));
		} else {
			String before = text.substring(0, pos);
			String after = text.substring(pos + 2);
			FontStyle fontStyle = new FontStyle();
			fontStyle.size = fontSize;
			extension = new HeaderFooter(PDFParagraph.makePhrase(new Phrase(), before, fontStyle, fontSize), PDFParagraph.makePhrase(new Phrase(), after, fontStyle, fontSize));
			extension.getBefore().setLeading(extension.getBefore().getFont().getCalculatedLeading(1));
			extension.getAfter().setLeading(extension.getAfter().getFont().getCalculatedLeading(1));
		}
		extension.setAlignment(HeaderFooter.ALIGN_CENTER);
		return extension;
	}

}

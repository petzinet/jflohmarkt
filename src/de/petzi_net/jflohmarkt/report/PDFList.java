/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ListItem;

/**
 * @author axel
 *
 */
public class PDFList extends PDFSection {

	private final List<PDFParagraph> items = new ArrayList<PDFParagraph>();
	
	public PDFList() {
		
	}
	
	public PDFList addItem(PDFParagraph item) {
		items.add(item);
		return this;
	}
	
	@Override
	void build(Document document, int fontSize) throws DocumentException {
		com.lowagie.text.List list = new com.lowagie.text.List();
		for (PDFParagraph item : items) {
			ListItem listItem = new ListItem();
			item.build(listItem, fontSize);
			list.add(listItem);
		}
		document.add(list);
	}

}

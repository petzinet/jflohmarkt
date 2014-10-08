/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;

/**
 * @author axel
 *
 */
public abstract class PDFSection {
	
	abstract void build(Document document, int fontSize) throws DocumentException;
	
}

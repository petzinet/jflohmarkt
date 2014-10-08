/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.io.IOException;
import java.net.URL;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;

/**
 * @author axel
 *
 */
public class PDFImage extends PDFSection {

	private final URL url;
	
	public PDFImage(URL url) {
		this.url = url;
	}
	
	@Override
	void build(Document document, int fontSize) throws DocumentException {
		try {
			document.add(Image.getInstance(url));
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

}

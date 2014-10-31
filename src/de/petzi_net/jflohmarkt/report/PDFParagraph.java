/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.awt.Color;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;

/**
 * @author axel
 *
 */
public class PDFParagraph extends PDFSection {
	
	final static int DEFAULT_FONT_SIZE = 10;
	
	private final boolean keepTogether;
	private final String text;
	private final PDFAlignment alignment;
	
	public PDFParagraph(String text) {
		this(text, PDFAlignment.BLOCK);
	}
	
	public PDFParagraph(String text, PDFAlignment alignment) {
		this(false, text, alignment);
	}
	
	public PDFParagraph(boolean keepTogether, String text, PDFAlignment alignment) {
		this.keepTogether = keepTogether;
		this.text = text;
		this.alignment = alignment;
	}
	
	void build(Paragraph paragraph, int fontSize) throws DocumentException {
		switch (alignment == null ? PDFAlignment.BLOCK : alignment) {
		case LEFT:
			paragraph.setAlignment(Paragraph.ALIGN_LEFT);
			break;
		case RIGHT:
			paragraph.setAlignment(Paragraph.ALIGN_RIGHT);
			break;
		case CENTER:
			paragraph.setAlignment(Paragraph.ALIGN_CENTER);
			break;
		case BLOCK:
			paragraph.setAlignment(Paragraph.ALIGN_JUSTIFIED);
			break;
		}
		paragraph.setLeading(0, 1.25f);
		makePhrase(paragraph, text, null, fontSize);
		paragraph.setSpacingBefore(paragraph.getFont().getCalculatedLeading(0.25f));
		paragraph.setSpacingAfter(paragraph.getFont().getCalculatedLeading(0.25f));
		paragraph.setKeepTogether(keepTogether);
	}
	
	@Override
	void build(Document document, int fontSize) throws DocumentException {
		Paragraph paragraph = new Paragraph();
		build(paragraph, fontSize);
		document.add(paragraph);
	}
	
	static Phrase makePhrase(Phrase phrase, String text, FontStyle fontStyle, int defaultFontSize) {
		if (fontStyle == null) {
			fontStyle = new FontStyle();
			fontStyle.size = defaultFontSize;
		}
		Pattern p = Pattern.compile("(\\*\\*|__|//|°\\d{0,2}°|°\\+°|°-°)");
		Matcher m = p.matcher(text);
		int pos = 0;
		while (m.find(pos)) {
			int start = m.start();
			if (pos < start) {
				phrase.add(makeChunk(text.substring(pos, start), fontStyle));
			}
			switch (text.charAt(start)) {
			case '*':
				pos = start + 2;
				fontStyle.bold = !fontStyle.bold;
				break;
			case '_':
				pos = start + 2;
				fontStyle.underlined = !fontStyle.underlined;
				break;
			case '/':
				pos = start + 2;
				fontStyle.italic = !fontStyle.italic;
				break;
			case '°':
				if (text.charAt(start + 1) == '°') {
					pos = start + 2;
					fontStyle.size = defaultFontSize;
				} else if (text.charAt(start + 1) == '+') {
					pos = start + 3;
					fontStyle.size = larger(defaultFontSize);
				} else if (text.charAt(start + 1) == '-') {
					pos = start + 3;
					fontStyle.size = smaller(defaultFontSize);
				} else if (text.charAt(start + 2) == '°') {
					pos = start + 3;
					fontStyle.size = Integer.valueOf(text.substring(start + 1, start + 2));
				} else {
					pos = start + 4;
					fontStyle.size = Integer.valueOf(text.substring(start + 1, start + 3));
				}
				break;
			}
		}
		if (pos < text.length()) {
			phrase.add(makeChunk(text.substring(pos), fontStyle));
		}
		phrase.setFont(makeFont(fontStyle));
		return phrase;
	}
	
	private static Chunk makeChunk(String text, FontStyle fontStyle) {
		Chunk chunk = new Chunk(text);
		chunk.setFont(makeFont(fontStyle));
		return chunk;
	}
	
	private static Font makeFont(FontStyle fontStyle) {
		int style = 0;
		if (fontStyle.bold)
			style |= Font.BOLD;
		if (fontStyle.italic)
			style |= Font.ITALIC;
		if (fontStyle.underlined)
			style |= Font.UNDERLINE;
		return new Font(Font.HELVETICA, fontStyle.size, style, Color.BLACK);
	}
	
	static int larger(int fontSize) {
		return (fontSize * 16) / 10;
	}
	
	static int smaller(int fontSize) {
		return (fontSize * 8) / 10;
	}
	
	static class FontStyle {
		
		public boolean bold = false;
		public boolean italic = false;
		public boolean underlined = false;
		public int size = DEFAULT_FONT_SIZE;
		
	}

}

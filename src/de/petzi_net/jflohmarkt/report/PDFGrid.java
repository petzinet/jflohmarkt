/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @author axel
 *
 */
public class PDFGrid extends PDFSection {

	private final boolean keepTogether;
	private final int rows;
	private final int[] columnRatios;
	private final PDFParagraph[][] cells;
	
	public PDFGrid(int rows, int... columnRatios) {
		this(false, rows, columnRatios);
	}
	
	public PDFGrid(boolean keepTogether, int rows, int... columnRatios) {
		this.keepTogether = keepTogether;
		this.rows = rows;
		this.columnRatios = columnRatios.clone();
		this.cells = new PDFParagraph[rows][columnRatios.length];
	}
	
	public PDFGrid setCell(int row, int column, PDFParagraph content) {
		cells[row][column] = content;
		return this;
	}
	
	@Override
	void build(Document document, int fontSize) throws DocumentException {
		PdfPTable table = new PdfPTable(columnRatios.length);
		table.setWidthPercentage(100);
		table.setWidths(columnRatios);
		table.setHeaderRows(0);
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columnRatios.length; column++) {
				Paragraph paragraph = new Paragraph();
				if (cells[row][column] != null)
					cells[row][column].build(paragraph, fontSize);
				PdfPCell cell = new PdfPCell(paragraph);
				cell.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT | PdfPCell.TOP | PdfPCell.BOTTOM);
				cell.setHorizontalAlignment(paragraph.getAlignment());
				table.addCell(cell);
			}
		}
		table.setKeepTogether(keepTogether);
		document.add(table);
	}

}

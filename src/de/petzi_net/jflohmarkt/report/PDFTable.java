/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import de.willuhn.datasource.BeanUtil;
import de.willuhn.datasource.GenericIterator;
import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.logging.Logger;

/**
 * @author axel
 *
 */
public class PDFTable extends PDFSection {
	
	private final boolean keepTogether;
	private final Iterable<?> iterable;
	private final List<Column> columns = new ArrayList<Column>();
	
	public PDFTable(GenericIterator list) {
		this(false, list);
	}
	
	public PDFTable(boolean keepTogether, final GenericIterator list) {
		this.keepTogether = keepTogether;
		this.iterable = new Iterable<GenericObject>() {

			@Override
			public Iterator<GenericObject> iterator() {
				try {
					list.begin();
				} catch (RemoteException e) {
					Logger.error("unable to iterate GenericIterator", e);
					return Collections.<GenericObject>emptySet().iterator();
				}
				return new Iterator<GenericObject>() {

					@Override
					public boolean hasNext() {
						try {
							return list.hasNext();
						} catch (RemoteException e) {
							Logger.error("unable to iterate GenericIterator", e);
							return false;
						}
					}

					@Override
					public GenericObject next() {
						try {
							return list.next();
						} catch (RemoteException e) {
							Logger.error("unable to iterate GenericIterator", e);
							return null;
						}
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
					
				};
			}
			
		};
	}
	
	public PDFTable(Iterable<?> iterable) {
		this(false, iterable);
	}
	
	public PDFTable(boolean keepTogether, Iterable<?> iterable) {
		this.keepTogether = keepTogether;
		this.iterable = iterable;
	}
	
	public PDFTable addColumn(String title, String field, int ratio) {
		columns.add(new Column(title, field, ratio, null, PDFAlignment.LEFT, null));
		return this;
	}
	
	public PDFTable addColumn(String title, String field, int ratio, Formatter formatter) {
		columns.add(new Column(title, field, ratio, formatter, PDFAlignment.LEFT, null));
		return this;
	}
	
	public PDFTable addColumn(String title, String field, int ratio, PDFAlignment alignment) {
		columns.add(new Column(title, field, ratio, null, alignment, null));
		return this;
	}
	
	public PDFTable addColumn(String title, String field, int ratio, PDFAlignment alignment, Formatter formatter) {
		columns.add(new Column(title, field, ratio, formatter, alignment, null));
		return this;
	}
	
	public PDFTable addColumn(String title, String field, int ratio, PDFAlignment alignment, Formatter formatter, Object footerValue) {
		columns.add(new Column(title, field, ratio, formatter, alignment, footerValue));
		return this;
	}
	
	@Override
	void build(Document document, int fontSize) throws DocumentException {
		PdfPTable table = new PdfPTable(columns.size());
		table.setWidthPercentage(100);
		int[] widths = new int[columns.size()];
		for (int n = 0; n < widths.length; n++)
			widths[n] = columns.get(n).ratio;
		table.setWidths(widths);
		boolean hasFooterRow = false;
		for (int n = 0; n < widths.length; n++) {
			Column column = columns.get(n);
			table.addCell(createCell(column.title, true, n == 0 ? -1 : n + 1 < columns.size() ? 0 : 1, true, fontSize, column.alignment));
			hasFooterRow |= column.footerValue != null;
		}
		table.setHeaderRows(1);
		long counter = 0;
		for (Object obj : iterable) {
			counter++;
			for (int n = 0; n < widths.length; n++) {
				Column column = columns.get(n);
				Object value;
				try {
					value = BeanUtil.get(obj, column.field);
				} catch (RemoteException e) {
					Logger.error("cannot analyse data", e);
					throw new DocumentException(e);
				}
				if (column.footerValue instanceof PDFAggregation)
					((PDFAggregation) column.footerValue).addValue(value);
				String text;
				if (column.formatter == null)
					text = value == null ? "" : value.toString();
				else
					text = column.formatter.format(value);
				table.addCell(createCell(text, false, n == 0 ? -1 : n + 1 < columns.size() ? 0 : 1, counter % 3 == 0, fontSize, column.alignment));
			}
		}
		if (hasFooterRow) {
			for (int n = 0; n < widths.length; n++) {
				Column column = columns.get(n);
				String text;
				if (column.footerValue == null)
					text = "";
				else
					text = column.footerValue.toString();
				table.addCell(createCell(text, true, n == 0 ? -1 : n + 1 < columns.size() ? 0 : 1, false, fontSize, column.alignment));
			}
		} else {
			for (PdfPCell cell : table.getRow(table.getRows().size() - 1).getCells()) {
				cell.enableBorderSide(PdfPCell.BOTTOM);
			}
		}
		table.setKeepTogether(keepTogether);
		document.add(table);
	}
	
	private PdfPCell createCell(String text, boolean header, int position, boolean highlighted, int fontSize, PDFAlignment alignment) {
		Font font;
		if (header) {
			font = new Font(Font.HELVETICA, fontSize, Font.BOLD, Color.BLACK);
		} else {
			font = new Font(Font.HELVETICA, fontSize, Font.NORMAL, Color.BLACK);
		}
		PdfPCell cell = new PdfPCell(new Phrase(text, font));
		if (highlighted) {
			cell.setBackgroundColor(HIGHLIGHTED);
		} else {
			cell.setBackgroundColor(Color.WHITE);
		}
		switch (position) {
		case -1:
			cell.disableBorderSide(PdfPCell.RIGHT);
			break;
		case 0:
			cell.disableBorderSide(PdfPCell.LEFT | PdfPCell.RIGHT);
			break;
		case 1:
			cell.disableBorderSide(PdfPCell.LEFT);
			break;
		}
		if (!header) {
			cell.disableBorderSide(PdfPCell.TOP | PdfPCell.BOTTOM);
		}
		
		switch (alignment == null ? PDFAlignment.LEFT : alignment) {
		case LEFT:
			cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
			break;
		case RIGHT:
			cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
			break;
		case CENTER:
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			break;
		case BLOCK:
			cell.setHorizontalAlignment(PdfPCell.ALIGN_JUSTIFIED);
			break;
		}
		return cell;
	}
	
	private final static Color HIGHLIGHTED = new Color(224, 224, 224);
	
	private static class Column {
		
		public final String title;
		public final String field;
		public final int ratio;
		public final Formatter formatter;
		public final PDFAlignment alignment;
		public final Object footerValue;
		
		public Column(String title, String field, int ratio, Formatter formatter, PDFAlignment alignment, Object footerValue) {
			this.title = title;
			this.field = field;
			this.ratio = ratio;
			this.formatter = formatter;
			this.alignment = alignment;
			this.footerValue = footerValue;
		}
		
	}

}

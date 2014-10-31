/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.internal.action.Program;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class PDFReport {
	
	public static boolean produceReport(String title, String subTitle, int fontSize, boolean landscape, Producer producer) throws ApplicationException, RemoteException {
		File file = askReportFile();
		if (file == null)
			return false;
		try {
			OutputStream os = new FileOutputStream(file);
			try {
				PDFReport report = new PDFReport(os, title, subTitle, fontSize, landscape);
				producer.produce(report);
				report.close();
			} finally {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		} catch (FileNotFoundException e) {
			throw new ApplicationException(e);
		}
		showFile(file);
		return true;
	}
	
	public static interface Producer {
		
		public void produce(PDFReport report) throws ApplicationException, RemoteException;
		
	}
	
	public static File askReportFile() throws ApplicationException {
		FileDialog fd = new FileDialog(GUI.getShell(), SWT.SAVE);
		fd.setText("Ausgabedatei wählen...");
		fd.setFilterExtensions(new String[] { "*.pdf" });
		String filePath = fd.open();
		if (filePath == null || filePath.length() == 0) {
			try {
				File tempFile = File.createTempFile("jflohmarkt-", ".pdf");
				tempFile.deleteOnExit();
				return tempFile;
			} catch (IOException e) {
				throw new ApplicationException(e);
			}
		}
		if (!filePath.toLowerCase().endsWith(".pdf")) {
			filePath = filePath + ".pdf";
		}
		return new File(filePath);
	}
	
	public static void showFile(final File file) {
		GUI.getDisplay().asyncExec(new Runnable(){
			
			@Override
			public void run() {
				try {
					new Program().handleAction(file);
				} catch (ApplicationException e) {
					Application.getMessagingFactory().sendMessage(new StatusBarMessage(e.getLocalizedMessage(), StatusBarMessage.TYPE_ERROR));
				}
			}
			
		});
	}
	
	private final OutputStream outputStream;
	private final int fontSize;
	private final Document document;
	
	private boolean pageFeed = false;
	
	public PDFReport(OutputStream outputStream, String title, String subTitle, int fontSize, boolean landscape) throws ApplicationException {
		this.outputStream = outputStream;
		this.fontSize = fontSize > 0 ? fontSize : PDFParagraph.DEFAULT_FONT_SIZE;
		if (landscape)
			this.document = new Document(PageSize.A4.rotate());
		else
			this.document = new Document(PageSize.A4);
		try {
			PdfWriter.getInstance(document, outputStream);
		} catch (DocumentException e) {
			throw new ApplicationException(e);
		}
		document.addAuthor("JFlohmarkt");
		if (title != null)
			document.addTitle(title);
		if (subTitle != null)
			document.addSubject(subTitle);
		document.setMargins(40, 40, 40, 40);
		
		String header = "°+°**" + (title == null ? "" : title) + "**°°";
		if (subTitle != null)
			header += "\n" + subTitle;
		setHeader(new PDFExtension(header));
		setFooter(new PDFExtension("- ## -"));
	}
	
	public void setHeader(PDFExtension header) throws ApplicationException {
		try {
			if (header != null) {
				HeaderFooter hf = header.build(fontSize);
				hf.setBorder(HeaderFooter.BOTTOM);
				hf.setBorderWidth(1);
				document.setHeader(hf);
			} else {
				document.resetHeader();
			}
			pageFeed = true;
		} catch (DocumentException e) {
			throw new ApplicationException(e);
		}
	}
	
	public void setFooter(PDFExtension footer) throws ApplicationException {
		try {
			if (footer != null) {
				HeaderFooter hf = footer.build(fontSize);
				hf.setBorder(HeaderFooter.TOP);
				hf.setBorderWidth(1);
				document.setFooter(hf);
			} else {
				document.resetFooter();
			}
			pageFeed = true;
		} catch (DocumentException e) {
			throw new ApplicationException(e);
		}
	}
	
	public void add(PDFSection section) throws ApplicationException {
		if (!document.isOpen()) {
			document.open();
		} else if (pageFeed) {
			document.newPage();
		}
		pageFeed = false;
		try {
			section.build(document, fontSize);
		} catch (DocumentException e) {
			throw new ApplicationException(e);
		}
	}
	
	public void newPage() {
		pageFeed = true;
	}
	
	public void resetPageCount() {
		document.setPageCount(0);
		pageFeed = true;
	}
	
	public void close() throws ApplicationException {
		if (document.isOpen())
			document.close();
		try {
			outputStream.close();
		} catch (IOException e) {
			throw new ApplicationException(e);
		}
	}

}

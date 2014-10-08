/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.willuhn.jameica.gui.formatter.Formatter;
import de.willuhn.jameica.system.Application;

/**
 * @author axel
 *
 */
public class TimestampFormatter implements Formatter {

	public final static DateFormat TIMESTAMP_FORMAT = SimpleDateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Application.getConfig().getLocale());
	
	@Override
	public String format(Object o) {
		if (o instanceof Date) {
			Date date = (Date) o;
			return TIMESTAMP_FORMAT.format(date);
		}
		return "";
	}

}

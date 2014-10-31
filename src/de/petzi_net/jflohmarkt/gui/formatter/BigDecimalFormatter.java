/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.formatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class BigDecimalFormatter implements Formatter {

	public final static String TWO_DECIMALS = "#,##0.00";
	
	public static DecimalFormat createDecimalFormat(String pattern) {
		DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
		if (pattern != null)
			decimalFormat.applyPattern(pattern);
		return decimalFormat;
	}
	
	private final DecimalFormat decimalFormat;
	private final String unit;
	
	public BigDecimalFormatter() {
		this.unit = null;
		this.decimalFormat = createDecimalFormat(null);
	}
	
	public BigDecimalFormatter(String pattern) {
		this.unit = null;
		this.decimalFormat = createDecimalFormat(pattern);
	}
	
	public BigDecimalFormatter(String pattern, String unit) {
		this.unit = unit;
		this.decimalFormat = createDecimalFormat(pattern);
	}
	
	@Override
	public String format(Object o) {
		if (o instanceof BigDecimal) {
			String text = decimalFormat.format(o);
			return unit == null ? text : text + " " + unit;
		} else {
			return "";
		}
	}

}

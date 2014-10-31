/**
 * 
 */
package de.petzi_net.jflohmarkt.report;

import java.math.BigDecimal;

import de.petzi_net.jflohmarkt.gui.formatter.BigDecimalFormatter;
import de.willuhn.jameica.gui.formatter.Formatter;

/**
 * @author axel
 *
 */
public class BigDecimalSum implements PDFAggregation {

	private final Formatter formatter;
	
	private BigDecimal currentValue = BigDecimal.ZERO;
	
	public BigDecimalSum() {
		this.formatter = new BigDecimalFormatter();
	}
	
	public BigDecimalSum(Formatter formatter) {
		this.formatter = formatter;
	}
	
	@Override
	public void addValue(Object value) {
		if (value instanceof BigDecimal) {
			currentValue = currentValue.add((BigDecimal) value);
		}
	}

	@Override
	public String toString() {
		return formatter.format(currentValue);
	}

}

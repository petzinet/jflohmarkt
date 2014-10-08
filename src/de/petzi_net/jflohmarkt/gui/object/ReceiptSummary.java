/**
 * 
 */
package de.petzi_net.jflohmarkt.gui.object;

import java.math.BigDecimal;
import java.util.Date;

import de.petzi_net.jflohmarkt.pos.xml.Receipt;

/**
 * @author axel
 *
 */
public class ReceiptSummary {
	
	public enum ImportState {
		KNOWN,
		KNOWN_DIFFERENT,
		KNOWN_UPDATABLE,
		UNKNOWN
	}
	
	private Receipt xmlReceipt;
	private ImportState importState;
	private String id;
	private int pos;
	private int receipt;
	private int type;
	private int cashier;
	private int state;
	private Date timestamp;
	private int quantity;
	private BigDecimal value;
	
	public Receipt getXmlReceipt() {
		return xmlReceipt;
	}
	
	public void setXmlReceipt(Receipt xmlReceipt) {
		this.xmlReceipt = xmlReceipt;
	}
	
	public ImportState getImportState() {
		return importState;
	}
	
	public void setImportState(ImportState importState) {
		this.importState = importState;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public int getPos() {
		return pos;
	}
	
	public void setPos(int pos) {
		this.pos = pos;
	}
	
	public int getReceipt() {
		return receipt;
	}
	
	public void setReceipt(int receipt) {
		this.receipt = receipt;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getCashier() {
		return cashier;
	}
	
	public void setCashier(int cashier) {
		this.cashier = cashier;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getQuantity() {
		return quantity;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public BigDecimal getValue() {
		return value;
	}
	
	public void setValue(BigDecimal value) {
		this.value = value;
	}

}

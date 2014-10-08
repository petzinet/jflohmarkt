/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

import de.petzi_net.jflohmarkt.rmi.ReceiptLine;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class ReceiptLineImpl extends AbstractDBObject implements ReceiptLine {

	private static final long serialVersionUID = 1L;

	public ReceiptLineImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "receiptline";
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "id";
	}
	
	@Override
	public long getReceipt() throws RemoteException {
		Long value = (Long) getAttribute("receipt");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setReceipt(long receipt) throws RemoteException {
		setAttribute("receipt", receipt);
	}
	
	@Override
	public int getLine() throws RemoteException {
		Integer value = (Integer) getAttribute("line");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setLine(int line) throws RemoteException {
		setAttribute("line", line);
	}
	
	@Override
	public Long getSeller() throws RemoteException {
		return (Long) getAttribute("seller");
	}
	
	@Override
	public void setSeller(Long seller) throws RemoteException {
		setAttribute("seller", seller);
	}
	
	@Override
	public Integer getQuantity() throws RemoteException {
		return (Integer) getAttribute("quantity");
	}
	
	@Override
	public void setQuantity(Integer quantity) throws RemoteException {
		setAttribute("quantity", quantity);
	}
	
	@Override
	public BigDecimal getValue() throws RemoteException {
		BigDecimal value = (BigDecimal) getAttribute("value");
		return value == null ? BigDecimal.ZERO : value;
	}
	
	@Override
	public void setValue(BigDecimal value) throws RemoteException {
		setAttribute("value", value);
	}
	
	@Override
	public boolean isValid() throws RemoteException {
		Boolean value = (Boolean) getAttribute("valid");
		return value == null ? false : value;
	}
	
	@Override
	public void setValid(boolean valid) throws RemoteException {
		setAttribute("valid", valid);
	}

	@Override
	public Date getTimestamp() throws RemoteException {
		return (Date) getAttribute("timestamp");
	}

	@Override
	public void setTimestamp(Date timestamp) throws RemoteException {
		setAttribute("timestamp", timestamp);
	}

}

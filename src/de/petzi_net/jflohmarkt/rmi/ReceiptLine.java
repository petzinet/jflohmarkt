/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

/**
 * @author axel
 *
 */
public interface ReceiptLine extends DBObject {
	
	public long getReceipt() throws RemoteException;
	
	public void setReceipt(long receipt) throws RemoteException;
	
	public int getLine() throws RemoteException;
	
	public void setLine(int line) throws RemoteException;
	
	public Long getSeller() throws RemoteException;
	
	public void setSeller(Long seller) throws RemoteException;
	
	public Integer getQuantity() throws RemoteException;
	
	public void setQuantity(Integer quantity) throws RemoteException;
	
	public BigDecimal getValue() throws RemoteException;
	
	public void setValue(BigDecimal value) throws RemoteException;
	
	public boolean isValid() throws RemoteException;
	
	public void setValid(boolean valid) throws RemoteException;
	
	public Date getTimestamp() throws RemoteException;
	
	public void setTimestamp(Date timestamp) throws RemoteException;

}

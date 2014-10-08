/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.petzi_net.jflohmarkt.rmi.Receipt;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class ReceiptImpl extends AbstractDBObject implements Receipt {

	private static final long serialVersionUID = 1L;

	public ReceiptImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "receipt";
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "id";
	}
	
	@Override
	public long getPOS() throws RemoteException {
		Long value = (Long) getAttribute("pos");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setPOS(long pos) throws RemoteException {
		setAttribute("pos", pos);
	}
	
	@Override
	public int getNumber() throws RemoteException {
		Integer value = (Integer) getAttribute("number");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setNumber(int number) throws RemoteException {
		setAttribute("number", number);
	}
	
	@Override
	public int getType() throws RemoteException {
		Integer value = (Integer) getAttribute("type");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setType(int type) throws RemoteException {
		setAttribute("type", type);
	}
	
	@Override
	public long getCashier() throws RemoteException {
		Long value = (Long) getAttribute("cashier");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setCashier(long cashier) throws RemoteException {
		setAttribute("cashier", cashier);
	}
	
	@Override
	public int getState() throws RemoteException {
		Integer value = (Integer) getAttribute("state");
		return value == null ? -1 : value;
	}
	
	@Override
	public void setState(int state) throws RemoteException {
		setAttribute("state", state);
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

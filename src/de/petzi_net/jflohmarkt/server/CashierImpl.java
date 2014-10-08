/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.rmi.Cashier;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class CashierImpl extends AbstractDBObject implements Cashier {

	private static final long serialVersionUID = 1L;

	public CashierImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "cashier";
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "id";
	}

	@Override
	public long getEvent() throws RemoteException {
		Long value = (Long) getAttribute("event");
		return value == null ? -1 : value;
	}

	@Override
	public void setEvent(long event) throws RemoteException {
		setAttribute("event", event);
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
	public String getName() throws RemoteException {
		return (String) getAttribute("name");
	}

	@Override
	public void setName(String name) throws RemoteException {
		setAttribute("name", name);
	}

	@Override
	public String getGivenName() throws RemoteException {
		return (String) getAttribute("givenname");
	}

	@Override
	public void setGivenName(String givenName) throws RemoteException {
		setAttribute("givenname", givenName);
	}

}

/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.rmi.POS;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class POSImpl extends AbstractDBObject implements POS {

	private static final long serialVersionUID = 1L;

	public POSImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "pos";
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
	public String getDescription() throws RemoteException {
		return (String) getAttribute("description");
	}

	@Override
	public void setDescription(String description) throws RemoteException {
		setAttribute("description", description);
	}

}

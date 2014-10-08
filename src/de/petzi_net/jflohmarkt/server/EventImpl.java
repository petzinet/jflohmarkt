/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;
import java.util.Date;

import de.petzi_net.jflohmarkt.rmi.Event;
import de.willuhn.datasource.db.AbstractDBObject;

/**
 * @author axel
 *
 */
public class EventImpl extends AbstractDBObject implements Event {

	private static final long serialVersionUID = 1L;

	public EventImpl() throws RemoteException {
		super();
	}

	@Override
	protected String getTableName() {
		return "event";
	}

	@Override
	public String getPrimaryAttribute() throws RemoteException {
		return "id";
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
	public Date getStart() throws RemoteException {
		return (Date) getAttribute("start");
	}

	@Override
	public void setStart(Date start) throws RemoteException {
		setAttribute("start", start);
	}
	
	@Override
	public Date getEnd() throws RemoteException {
		return (Date) getAttribute("end");
	}
	
	@Override
	public void setEnd(Date end) throws RemoteException {
		setAttribute("end", end);
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

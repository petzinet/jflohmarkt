/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.rmi.DBObject;

/**
 * @author axel
 *
 */
public interface Event extends DBObject {

	public String getName() throws RemoteException;

	public void setName(String name) throws RemoteException;

	public Date getStart() throws RemoteException;

	public void setStart(Date start) throws RemoteException;

	public Date getEnd() throws RemoteException;

	public void setEnd(Date end) throws RemoteException;

	public String getDescription() throws RemoteException;

	public void setDescription(String description) throws RemoteException;

}
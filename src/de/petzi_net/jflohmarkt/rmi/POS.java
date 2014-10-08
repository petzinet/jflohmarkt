/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBObject;

/**
 * @author axel
 *
 */
public interface POS extends DBObject {
	
	public long getEvent() throws RemoteException;
	
	public void setEvent(long event) throws RemoteException;
	
	public int getNumber() throws RemoteException;
	
	public void setNumber(int number) throws RemoteException;
	
	public String getDescription() throws RemoteException;
	
	public void setDescription(String description) throws RemoteException;

}

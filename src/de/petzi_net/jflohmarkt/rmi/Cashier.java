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
public interface Cashier extends DBObject {
	
	public long getEvent() throws RemoteException;
	
	public void setEvent(long event) throws RemoteException;
	
	public int getNumber() throws RemoteException;
	
	public void setNumber(int number) throws RemoteException;
	
	public String getName() throws RemoteException;
	
	public void setName(String name) throws RemoteException;
	
	public String getGivenName() throws RemoteException;
	
	public void setGivenName(String givenName) throws RemoteException;

}

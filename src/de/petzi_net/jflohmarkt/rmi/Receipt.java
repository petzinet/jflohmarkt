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
public interface Receipt extends DBObject {
	
	public static final int TYPE_SALE = 1;
	public static final int TYPE_PICKUP = 2;
	public static final int TYPE_DROPOFF = 3;
	public static final int TYPE_INVENTORY = 4;
	
	public static final int STATE_ACTIVE = 1;
	public static final int STATE_FINISHED = 2;
	public static final int STATE_ABORTED = 3;
	
	public long getPOS() throws RemoteException;
	
	public void setPOS(long pos) throws RemoteException;
	
	public int getNumber() throws RemoteException;
	
	public void setNumber(int number) throws RemoteException;
	
	public int getType() throws RemoteException;
	
	public void setType(int type) throws RemoteException;
	
	public long getCashier() throws RemoteException;
	
	public void setCashier(long cashier) throws RemoteException;
	
	public int getState() throws RemoteException;
	
	public void setState(int state) throws RemoteException;
	
	public Date getTimestamp() throws RemoteException;
	
	public void setTimestamp(Date timestamp) throws RemoteException;

}

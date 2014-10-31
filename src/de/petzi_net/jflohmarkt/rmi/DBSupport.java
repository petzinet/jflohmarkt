/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;

/**
 * @author axel
 *
 */
public interface DBSupport extends Serializable {
	
	public String getJdbcUrl();
	
	public String getJdbcDriver();
	
	public String getJdbcUsername();
	
	public String getJdbcPassword();
	
	public void checkConnection(Connection conn) throws RemoteException;
	
	public String getSQLTimestamp(String content) throws RemoteException;
	
	public boolean getInsertWithID() throws RemoteException;

}
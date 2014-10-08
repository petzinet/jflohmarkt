/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.io.File;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public interface DBSupport extends Serializable {
	
	public String getJdbcUrl();
	
	public String getJdbcDriver();
	
	public String getJdbcUsername();
	
	public String getJdbcPassword();
	
	public void checkConsistency(Connection conn) throws RemoteException, ApplicationException;
	
	public void checkConnection(Connection conn) throws RemoteException;
	
	public void install() throws RemoteException;
	
	public void execute(Connection conn, File sqlScript) throws RemoteException;
	
	public String getSQLTimestamp(String content) throws RemoteException;
	
	public boolean getInsertWithID() throws RemoteException;
	
	public int getTransactionIsolationLevel() throws RemoteException;

}
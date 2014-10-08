/**
 * 
 */
package de.petzi_net.jflohmarkt.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.system.Settings;
import de.willuhn.util.ApplicationException;
import de.willuhn.jameica.plugin.Version;

/**
 * @author axel
 *
 */
public interface JFlohmarktDBService extends DBService {
	
	public final static Settings SETTINGS = new Settings(JFlohmarktDBService.class);
	
	public void update(Version oldVersion, Version newVersion) throws RemoteException, ApplicationException;
	
	public void install() throws RemoteException, ApplicationException;
	
	public void checkConsistency() throws RemoteException, ApplicationException;
	
	public String getSQLTimestamp(String content) throws RemoteException;

}

/**
 * 
 */
package de.petzi_net.jflohmarkt;

import java.rmi.RemoteException;

import de.petzi_net.jflohmarkt.rmi.JFlohmarktDBService;
import de.petzi_net.jflohmarkt.server.JFlohmarktDBServiceImpl;
import de.willuhn.datasource.rmi.DBService;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class JFlohmarktPlugin extends AbstractPlugin {

	private Settings settings;

	public JFlohmarktPlugin() {
		settings = new Settings(getClass());
		settings.setStoreWhenRead(true);
	}

	@Override
	public void init() throws ApplicationException {
		Logger.info("starting init process for jflohmarkt");

		call(new ServiceCall() {

			@Override
			public void call(JFlohmarktDBService service) throws ApplicationException, RemoteException {
				service.checkConsistency();
			}
			
		});

		Application.getCallback().getStartupMonitor().addPercentComplete(5);
	}

	@Override
	public void install() throws ApplicationException {
		Logger.info("starting install process for jflohmarkt");

		call(new ServiceCall() {

			@Override
			public void call(JFlohmarktDBService service) throws RemoteException, ApplicationException {
				service.install();
			}
			
		});
	}

	@Override
	public void update(final Version oldVersion) throws ApplicationException {
		Logger.info("starting update process for jflohmarkt");

		call(new ServiceCall() {

			@Override
			public void call(JFlohmarktDBService service) throws RemoteException, ApplicationException {
				service.update(oldVersion, getManifest().getVersion());
			}
			
		});
	}

	private interface ServiceCall {

		public void call(JFlohmarktDBService service) throws ApplicationException, RemoteException;
		
	}

	private void call(ServiceCall call) throws ApplicationException {
		if (Application.inClientMode())
			return;

		JFlohmarktDBService service = null;
		try {
			service = new JFlohmarktDBServiceImpl();
			service.start();
			call.call(service);
		} catch (ApplicationException ae) {
			throw ae;
		} catch (Exception e) {
			throw new ApplicationException("Fehler beim Initialisieren der Datenbank", e);
		} finally {
			if (service != null) {
				try {
					service.stop(true);
				} catch (Exception e) {
					Logger.error("error while closing db service", e);
				}
			}
		}
	}
	
	public static DBService getDBService() throws RemoteException {
		try {
			return (DBService) Application.getServiceFactory().lookup(JFlohmarktPlugin.class, "database");
		} catch (Exception e) {
			throw new RemoteException("error while getting database service", e);		}
	}
	
	public static Settings getSettings() {
		JFlohmarktPlugin instance = Application.getPluginLoader().getPlugin(JFlohmarktPlugin.class);
		return instance.settings;
	}

}

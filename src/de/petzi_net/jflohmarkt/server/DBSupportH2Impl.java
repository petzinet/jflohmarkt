/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.io.File;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.petzi_net.jflohmarkt.JFlohmarktPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class DBSupportH2Impl extends AbstractDBSupportImpl {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unchecked")
	public DBSupportH2Impl() {
		Logger.info("switching dbservice to uppercase");
		System.setProperty(JFlohmarktDBServiceImpl.class.getName() + ".uppercase", "true");
		try {
			Method m = Application.getClassLoader().load("org.h2.engine.Constants").getMethod("getVersion", (Class<?>[]) null);
			Logger.info("h2 version: " + m.invoke(null, (Object[]) null));
		} catch (Throwable t) {
			Logger.warn("unable to determine h2 version");
		}
	}

	@Override
	public String getJdbcDriver() {
		return "org.h2.Driver";
	}

	@Override
	public String getJdbcPassword() {
		return "jflohmarkt";
	}

	@Override
	public String getJdbcUrl() {
		return "jdbc:h2:" + Application.getPluginLoader().getPlugin(JFlohmarktPlugin.class).getResources().getWorkPath() + "/h2db/jflohmarkt";
	}

	@Override
	public String getJdbcUsername() {
		return "jflohmarkt";
	}

	@Override
	public void checkConsistency(Connection conn) throws ApplicationException {
	}

	@Override
	public void execute(Connection conn, File sqlScript) throws RemoteException {
		if (sqlScript == null)
			return;
		sqlScript = new File(sqlScript.getParent(), sqlScript.getName());
		if (!sqlScript.exists()) {
			Logger.debug("file " + sqlScript + " does not exist, skipping");
			return;
		}
		super.execute(conn, sqlScript);
	}

	@Override
	public String getSQLTimestamp(String content) {
		return content;
	}

	@Override
	public boolean getInsertWithID() {
		return false;
	}

	@Override
	public void checkConnection(Connection conn) {
	}
	
}

/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;

import de.petzi_net.jflohmarkt.db.DBTool;
import de.petzi_net.jflohmarkt.rmi.DBSupport;
import de.petzi_net.jflohmarkt.rmi.JFlohmarktDBService;
import de.willuhn.datasource.db.DBServiceImpl;
import de.willuhn.jameica.plugin.Version;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class JFlohmarktDBServiceImpl extends DBServiceImpl implements JFlohmarktDBService {

	private static final long serialVersionUID = 1L;

	private DBSupport driver = null;

	public JFlohmarktDBServiceImpl() throws RemoteException {
		this(SETTINGS.getString("database.driver", DBSupportH2Impl.class.getName()));
	}

	protected JFlohmarktDBServiceImpl(String driverClass) throws RemoteException {
		super();
		this.setClassloader(Application.getClassLoader());
		this.setClassFinder(Application.getClassLoader().getClassFinder());
		if (driverClass == null)
			throw new RemoteException("no driver given");
		Logger.info("loading database driver: " + driverClass);
		try {
			Class<?> c = Application.getClassLoader().load(driverClass);
			this.driver = (DBSupport) c.newInstance();
		} catch (Throwable t) {
			throw new RemoteException("unable to load database driver " + driverClass, t);
		}
	}

	@Override
	public String getName() {
		return "Datenbank-Service für JFlohmarkt";
	}

	@Override
	protected boolean getAutoCommit() throws RemoteException {
		return SETTINGS.getBoolean("autocommit", super.getAutoCommit());
	}

	@Override
	protected String getJdbcDriver() {
		return this.driver.getJdbcDriver();
	}

	@Override
	protected String getJdbcPassword() {
		return this.driver.getJdbcPassword();
	}

	@Override
	protected String getJdbcUrl() {
		return this.driver.getJdbcUrl();
	}

	@Override
	protected String getJdbcUsername() {
		return this.driver.getJdbcUsername();
	}

	@Override
	public void checkConsistency() throws RemoteException, ApplicationException {
		update();
	}

	@Override
	public void install() throws RemoteException, ApplicationException {
		update();
	}

	@Override
	public void update(Version oldVersion, Version newVersion) throws RemoteException, ApplicationException {
		update();
	}

	private void update() throws ApplicationException {
		try {
			Connection connection = getConnection();
			DBTool.updateLiquibase(connection);
		} catch (Exception e) {
			Logger.error("error installing/updating jflohmarkt database", e);
			throw new ApplicationException(e);
		}
	}

	@Override
	public String getSQLTimestamp(String content) throws RemoteException {
		return this.driver.getSQLTimestamp(content);
	}

	@Override
	protected boolean getInsertWithID() throws RemoteException {
		return this.driver.getInsertWithID();
	}

	@Override
	protected void checkConnection(Connection conn) throws SQLException {
		try {
			this.driver.checkConnection(conn);
		} catch (RemoteException re) {
			throw new SQLException(re.getMessage());
		}
		super.checkConnection(conn);
	}

}

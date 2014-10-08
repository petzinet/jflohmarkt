/**
 * 
 */
package de.petzi_net.jflohmarkt.server;

import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.sql.Connection;

import de.petzi_net.jflohmarkt.rmi.DBSupport;
import de.willuhn.logging.Logger;
import de.willuhn.sql.ScriptExecutor;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public abstract class AbstractDBSupportImpl implements DBSupport {

	private static final long serialVersionUID = 1L;

	@Override
	public void checkConsistency(Connection conn) throws ApplicationException {
	}

	@Override
	public void execute(Connection conn, File sqlScript) throws RemoteException {
		if (sqlScript == null)
			return;

		if (!sqlScript.canRead() || !sqlScript.exists())
			return;

		Logger.info("executing sql script: " + sqlScript.getAbsolutePath());

		FileReader reader = null;

		try {
			reader = new FileReader(sqlScript);
			ScriptExecutor.execute(reader, conn);
		} catch (RemoteException re) {
			throw re;
		} catch (Exception e) {
			throw new RemoteException("error while executing sql script " + sqlScript, e);
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (Exception e3) {
				Logger.error("error while closing file " + sqlScript, e3);
			}
		}
	}

	@Override
	public void install() {
	}

	@Override
	public int getTransactionIsolationLevel() {
		return -1;
	}

}

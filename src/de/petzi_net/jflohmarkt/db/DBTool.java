/**
 * 
 */
package de.petzi_net.jflohmarkt.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * @author axel
 *
 */
public class DBTool {
	
	private final static String DB_CHANGE_LOG = "de/petzi_net/jflohmarkt/db/jflohmarkt.xml";
	
	public static boolean isLiquibaseInstalled(Connection connection) {
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select id from databasechangelog");
			return rs.next();
		} catch (SQLException e) {
			return false;
		}
	}
	
	public static void updateLiquibase(Connection connection) throws ApplicationException {
		try {
			Liquibase liquibase = new Liquibase(DB_CHANGE_LOG, new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
			liquibase.update("");
		} catch (LiquibaseException e) {
			Logger.error("error updating/installing jflohmarkt database", e);
			throw new ApplicationException(e);
		}
	}

}

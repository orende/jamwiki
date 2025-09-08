/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.db;

import org.apache.commons.lang3.StringUtils;
import org.jamwiki.WikiException;
import org.jamwiki.WikiMessage;
import org.jamwiki.utils.WikiLogger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import java.sql.Connection;
import java.util.List;

/**
 * This class simply contains utility methods for upgrading database schemas
 * (if needed) between JAMWiki versions.  These methods are typically called automatically
 * by the UpgradeServlet when an upgrade is detected and will automatically upgrade the
 * database schema without the need for manual intervention from the user.
 *
 * In general upgrade methods will only be maintained for two major releases and then
 * deleted - for example, JAMWiki version 0.9.0 will not support upgrading from versions
 * prior to 0.7.0.
 */
public class DatabaseUpgrades {

	private static final WikiLogger logger = WikiLogger.getLogger(java.lang.invoke.MethodHandles.lookup().lookupClass());

	/**
	 *
	 */
	private DatabaseUpgrades() {
	}

	/**
     * This method should be called only during upgrades and provides the capability
     * to execute update SQL from a QueryHandler-specific property file.
     *
     * @param prop        The name of the SQL property file value to execute.
     * @param dataHandler
     * @return true if action actually performed and false otherwise.
     */
	private static boolean executeUpgradeUpdate(String prop, DataHandler dataHandler) {
		String sql = dataHandler.queryHandler().sql(prop);
		if (StringUtils.isBlank(sql)) {
			// some queries such as validation queries are not defined on all databases
			return false;
		}
		DatabaseConnection.getJdbcTemplate().update(sql);
		return true;
	}

	/**
	 * Perform the required database upgrade steps when upgrading from versions
	 * older than JAMWiki 1.2.
	 */
	public static void upgrade120(List<WikiMessage> messages, DataHandler dataHandler) throws WikiException {
		try {
            DatabaseConnection.getTransactionTemplate().execute(
                    new TransactionCallbackWithoutResult() {
                        protected void doInTransactionWithoutResult(TransactionStatus status) {
                            try {
                                Connection conn = DatabaseConnection.getConnection();
                                // initialize sequences
                                if (DatabaseUpgrades.executeUpgradeUpdate("STATEMENT_CREATE_SEQUENCES", dataHandler)) {
                                    messages.add(new WikiMessage("upgrade.message.db.object.added", "sequences"));
                                }
                                // create ROLE_REGISTER
                                DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_120_ADD_ROLE_REGISTER", dataHandler);
                                messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_role"));
                                DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_120_ADD_ROLE_REGISTER_TO_ANONYMOUS", dataHandler);
                                messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_group_authorities"));
                                // add the jam_file_data table
                                DatabaseUpgrades.executeUpgradeUpdate("STATEMENT_CREATE_FILE_DATA_TABLE", dataHandler);
                                messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_file_data"));
                            } catch (Exception e) {
                                status.setRollbackOnly();
                                throw new TransactionRuntimeException(e);
                            }
                        }
                    });
		} catch (TransactionRuntimeException e) {
			logger.error("Database failure during upgrade", e);
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
	}

	/**
	 * Perform the required database upgrade steps when upgrading from versions
	 * older than JAMWiki 1.3.
	 */
	public static void upgrade130(final List<WikiMessage> messages, DataHandler dataHandler) throws WikiException {
		try {
			DatabaseConnection.getTransactionTemplate().execute(
				new TransactionCallbackWithoutResult() {
					protected void doInTransactionWithoutResult(TransactionStatus status) {
						try {
							// New tables as of JAMWiki 1.3
							DatabaseUpgrades.executeUpgradeUpdate("STATEMENT_CREATE_USER_PREFERENCES_DEFAULTS_TABLE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_user_preferences_defaults"));
							DatabaseUpgrades.executeUpgradeUpdate("STATEMENT_CREATE_USER_PREFERENCES_TABLE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.table.added", "jam_user_preferences"));
							DatabaseUtils.setupUserPreferencesDefaults(dataHandler);
							// Create default values for user preferences.
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_user_preferences_defaults"));
							// Migrate existing user preferences to new tables
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_MIGRATE_USER_PREFERENCES_DEFAULT_LOCALE", dataHandler);
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_MIGRATE_USER_PREFERENCES_EDITOR", dataHandler);
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_MIGRATE_USER_PREFERENCES_SIGNATURE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_user_preferences"));
							// Drop old user preference columns from jam_wiki_user
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_REMOVE_WIKI_USER_TABLE_COLUMN_DEFAULT_LOCALE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_wiki_user"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_REMOVE_WIKI_USER_TABLE_COLUMN_EDITOR", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_wiki_user"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_REMOVE_WIKI_USER_TABLE_COLUMN_SIGNATURE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_wiki_user"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_ADD_USER_TABLE_COLUMN_CHALLENGE_VALUE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_users"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_ADD_USER_TABLE_COLUMN_CHALLENGE_DATE", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_users"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_ADD_USER_TABLE_COLUMN_CHALLENGE_IP", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_users"));
							DatabaseUpgrades.executeUpgradeUpdate("UPGRADE_130_ADD_USER_TABLE_COLUMN_CHALLENGE_TRIES", dataHandler);
							messages.add(new WikiMessage("upgrade.message.db.data.updated", "jam_users"));
						} catch (WikiException e) {
							status.setRollbackOnly();
							throw new TransactionRuntimeException(e);
						}
					}
				}
			);
		} catch (TransactionRuntimeException e) {
			throw new WikiException(new WikiMessage("upgrade.error.fatal", e.getMessage()));
		}
	}
}

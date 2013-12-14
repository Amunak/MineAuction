package net.amunak.bukkit.mineauction;

/**
 * Copyright 2013 Jiří Barouš
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * PrefixedPlayerlist is main plugin class
 *
 * @version 1.1.5
 * @author Amunak
 */
public final class MineAuction extends LoggableJavaPlugin {

    protected Connection db;
    public FileConfiguration config;
    final static int SQL_TIMEOUT = 0;

    @Override
    public void onEnable() {
        log = new Log(this);
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.reloadConfig();

        if (this.config.getBoolean("options.general.checkVersion")) {
            CheckVersion.check(this);
        }

        checkDatabaseConnection();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.config = this.getConfig();
        log.raiseFineLevel = this.config.getBoolean("options.general.verboseLogging");

        log.fine("reloading config...");

        if (!this.config.isString("database.url") || !this.config.isString("database.user")) {
            log.warning("Loading configuration failed: database url/user missing or invalid");
            log.warning(">>>disabling plugin");
            getPluginLoader().disablePlugin(this);
        }

        if (this.config.getString("database.password").length() < 1) {
            log.fine("Database password is empty");
        }

        log.fine("configuration reloaded");
    }

    protected void checkDatabaseConnection() {
        try {
            this.db = DriverManager.getConnection(this.config.getString("database.url"), this.config.getString("database.user"), this.config.getString("database.password"));
            if (this.db.isValid(SQL_TIMEOUT)) {
                log.fine("database connection established successfully");
            } else {
                log.warning("database connection invalid: unknown error");
            }

        } catch (SQLException ex) {
            log.warning("database connection failed: " + ex.getMessage());
            log.fine("url: '" + this.config.getString("database.url") + "'");
            log.fine("user: '" + this.config.getString("database.user")
                    + "' using password: " + ((this.config.getString("database.password").length() > 0) ? "yes" : "no"));
            if (this.config.getString("database.password").length() < 1) {
                log.warning("Database password is empty"
                        + "(that might be the cause of this problem - "
                        + "fill your password in the config file)");
            }
            log.warning(">>>disabling plugin");
            getPluginLoader().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        log.fine("disabling plugin...");
        try {
            if (db.isValid(SQL_TIMEOUT)) {
                db.close();
            }
        } catch (SQLException ex) {
            log.warning("could not close database connection: " + ex.getMessage());
        }
        log.fine("plugin disabled");
    }
}

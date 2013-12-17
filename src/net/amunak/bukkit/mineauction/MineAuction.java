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
import net.amunak.bukkit.mineauction.sign.SignInteractionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import net.amunak.bukkit.mineauction.sign.SignType;
import net.amunak.bukkit.mineauction.sign.SignStorage;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * This is MineAuction's main plugin class
 *
 * @version 1.0
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public final class MineAuction extends LoggableJavaPlugin {

    protected Connection db;
    public FileConfiguration config;
    protected SignStorage signsStorage;
    public final static int SQL_TIMEOUT = 0;
    public final static String SIGN_IDENTIFIER = "[MineAuction]";
    public final static String SIGN_INVALID_IDENTIFIER = "*MineAuction*";

    @Override
    public void onEnable() {

        //init load and config
        log = new Log(this);
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.reloadConfig();

        //checks, other inits
        if (this.config.getBoolean("options.general.checkVersion")) {
            CheckVersion.check(this);
        }

        //checkDatabaseConnection();

        //load data
        if (this.config.getBoolean("options.signs.enable")) {
            this.signsStorage = new SignStorage(this);
            this.signsStorage.load();
        }

        //register listeners, run
        //getCommand(null)
        if (this.config.getBoolean("options.signs.enable")) {
            getServer().getPluginManager().registerEvents(new SignInteractionListener(this), this);
        }
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

        if (this.config.getBoolean("options.signs.enable")) {
            String signTextsConfigurationNode = "options.signs.signTexts.";
            String tmpFullConfigurationNode = signTextsConfigurationNode + "header";
            List<String> identifierWarning = new ArrayList<>(2);
            if (!this.config.isList(tmpFullConfigurationNode)
                    || this.config.getStringList(tmpFullConfigurationNode).size() != 2) {
                log.warning(tmpFullConfigurationNode + " is not a list of 2 lines (items)");
                log.warning("using placeholder warning for " + tmpFullConfigurationNode);
                identifierWarning.add(SIGN_IDENTIFIER);
                identifierWarning.add("<config error>");
                this.config.set(tmpFullConfigurationNode, identifierWarning);
            }

            for (SignType signType : SignType.values()) {
                tmpFullConfigurationNode = signTextsConfigurationNode + "types." + signType.getName();
                if (!this.config.isList(tmpFullConfigurationNode)) {
                    log.warning(tmpFullConfigurationNode + " is not a list of 2 lines (items)");
                    log.warning("using placeholder warning for " + tmpFullConfigurationNode);
                    identifierWarning.clear();
                    identifierWarning.add(SIGN_IDENTIFIER);
                    identifierWarning.add("<config error>");

                }
            }
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
        if (db != null) {
            try {
                if (db.isValid(SQL_TIMEOUT)) {
                    db.close();
                }
            } catch (SQLException ex) {
                log.warning("could not close database connection: " + ex.getMessage());
            }
        }
        log.fine("plugin disabled");
    }

    /**
     * Returns the SignsStorage of this plugin
     *
     * @return the SignsStorage of this plugin
     */
    public SignStorage getSignsStorage() {
        return signsStorage;
    }
}

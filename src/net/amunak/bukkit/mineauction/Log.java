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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This class sends messages to player or console. It uses common Logger for
 * console messages and sendMessage to communicate with the Player. It is
 * capable of using log levels and it has bindings for the most common levels
 * (warn, info, fine). Player messages are marked based on log level.
 *
 * @author Amunak
 * @version 2.0
 */
public final class Log {

    private static final Logger log = Logger.getLogger("Minecraft");
    public String consolePrefix;
    public String playerPrefix;
    public Boolean raiseFineLevel = false;

    /**
     * This method initializes log line prefixes
     *
     * @param plugin the plugin
     */
    Log(JavaPlugin plugin) {
        consolePrefix = "[" + plugin.getDescription().getName() + "] ";
        playerPrefix = ChatColor.RESET + "[" + ChatColor.GOLD + plugin.getDescription().getName() + ChatColor.RESET + "]: ";
    }

    /**
     * Sends a message to player or logs to console. Colors messages based on
     * log level.
     *
     * @param level log level
     * @param player player to send message to (null for console)
     * @param message the message
     * @param forceConsole always send the message to console (even if already
     * sent to player)
     */
    public void log(Level level, Player player, String message, Boolean forceConsole) {
        if (player instanceof Player) {
            if (level.intValue() >= Level.WARNING.intValue()) {
                player.sendMessage(ChatColor.WHITE + "[" + ChatColor.DARK_RED + "!" + ChatColor.WHITE + "]" + playerPrefix + message);
            } else if (level.intValue() <= Level.FINE.intValue()) {
                player.sendMessage(playerPrefix + ChatColor.GRAY + message);
            } else {
                player.sendMessage(playerPrefix + message);
            }
        }
        if (forceConsole || !(player instanceof Player)) {
            if (this.raiseFineLevel && (level.intValue() < Level.INFO.intValue())) {
                level = Level.INFO;
            }
            log.log(level, (consolePrefix + message));
        }
    }

    /**
     * @see log(l, p, m, false)
     */
    public void log(Level l, Player p, String m) {
        this.log(l, p, m, false);
    }

    /**
     * @see log(l, null, m, true)
     */
    public void log(Level l, String m) {
        this.log(l, null, m, true);
    }

    /**
     * @see log(Level.WARNING, p, m, c)
     */
    public void warning(Player p, String m, Boolean c) {
        this.log(Level.WARNING, p, m, c);
    }

    /**
     * @see log(Level.WARNING, p, m, false)
     */
    public void warning(Player p, String m) {
        this.log(Level.WARNING, p, m, false);
    }

    /**
     * @see log(Level.WARNING, null, m, true)
     */
    public void warning(String m) {
        this.log(Level.WARNING, null, m, true);
    }

    /**
     * @see log(Level.INFO, p, m, c)
     */
    public void info(Player p, String m, Boolean c) {
        this.log(Level.INFO, p, m, c);
    }

    /**
     * @see log(Level.INFO, p, m, false)
     */
    public void info(Player p, String m) {
        this.log(Level.INFO, p, m, false);
    }

    /**
     * @see log(Level.INFO, null, m, true)
     */
    public void info(String m) {
        this.log(Level.INFO, null, m, true);
    }

    /**
     * @see log(Level.FINE, p, m, c)
     */
    public void fine(Player p, String m, Boolean c) {
        this.log(Level.FINE, p, m, c);
    }

    /**
     * @see log(Level.FINE, p, m, false)
     */
    public void fine(Player p, String m) {
        this.log(Level.FINE, p, m, false);
    }

    /**
     * @see log(Level.FINE, null, m, true)
     */
    public void fine(String m) {
        this.log(Level.FINE, null, m, true);
    }
}

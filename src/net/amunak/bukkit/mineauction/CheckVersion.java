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
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import org.bukkit.entity.Player;

/**
 * This class checks for version
 *
 * @author Amunak
 * @version 2.0
 */
public final class CheckVersion {

    /**
     * Checks version, returns output to player (or console if no player)
     *
     * @param player the player (null for console)
     * @param plugin the plugin
     */
    public static void check(Player player, LoggableJavaPlugin plugin) {
        URL u;
        InputStream is;
        String s;
        Integer version;

        version = (int) (Integer.parseInt(plugin.getDescription().getVersion().replace(".", "")));

        plugin.log.info(player, "Checking for updates...", true);

        try {
            u = new URL("http://mc.amunak.net/query.php?v=" + version + "&p=" + plugin.getDescription().getName());
            is = u.openStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            while ((s = br.readLine()) != null) {
                plugin.log.info(player, s, true);
            }
            is.close();
        } catch (Exception e) {
            plugin.log.warning(player, "No response", true);
        }
    }

    /**
     * Checks version, logs to console
     *
     * @param plugin the plugin
     * @see check(Player null, LoggableJavaPlugin plugin)
     */
    public static void check(LoggableJavaPlugin plugin) {
        check(null, plugin);
    }
}

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
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is an extension to JavaPlugin, which makes it keep one custom logging
 * class Log
 *
 * @author Amunak
 * @version 1.0
 */
public class LoggableJavaPlugin extends JavaPlugin {

    /**
     * The instance of the logging class Log
     */
    public Log log;

    @Override
    public void onEnable() {
        log = new Log(this);
    }
}

package net.amunak.bukkit.mineauction.sign;

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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.amunak.bukkit.mineauction.MineAuction;
import org.bukkit.Location;
import org.bukkit.block.Sign;

/**
 * A storage class for sets of signs' locations and their types
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public class SignStorage {

    protected MineAuction plugin;
    protected HashMap<Location, SignType> listOfSigns;
    protected final static String LIST_FILENAME = "signsstorage.bin";

    /**
     * Constructs an empty SignStorage for the specified plugin
     *
     * @param plugin the plugin
     */
    public SignStorage(MineAuction plugin) {
        this.plugin = plugin;
        this.listOfSigns = new HashMap<>();
    }

    public SignType getSignType(Sign sign) {
        return this.listOfSigns.get(sign.getLocation());
    }

    public SignType addItem(Sign sign, SignType type) {
        return this.addItem(sign.getLocation(), type);
    }

    public SignType addItem(Location location, SignType type) {
        return this.listOfSigns.put(location, type);
    }

    public SignType removeItem(Location location) {
        return this.listOfSigns.remove(location);
    }

    public SignType removeItem(Sign sign) {
        return this.removeItem(sign.getLocation());
    }

    public boolean containsItem(Location location) {
        return this.listOfSigns.containsKey(location);
    }

    public boolean containsItem(Sign sign) {
        return this.containsItem(sign.getLocation());
    }

    /**
     * Removes all items from this storage. The internal {@link HashMap} will be
     * empty after this call returns.
     */
    public void purge() {
        this.listOfSigns.clear();
    }

    /**
     * Composes a path to this storage's file
     *
     * @return the path as {@code File}
     */
    public File getDataFilePath() {
        return new File(this.plugin.getDataFolder(), LIST_FILENAME);
    }

    /**
     * Saves the storage to its corresponding file, overwriting the original (or
     * creating a new file if it doesn't exist). Outputs warning if the file
     * cannot be saved.
     */
    public void save() {
        try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(this.getDataFilePath()))) {
            stream.writeObject(this.listOfSigns);
            stream.flush();
        } catch (IOException ex) {
            this.plugin.log.warning("sign storage file " + this.getDataFilePath().getName() + " could not be written: " + ex.getMessage());
        }
    }

    /**
     * Reads the storage from its corresponding file.
     *
     * Current contents of the storage (and any changes) will be lost. Will
     * create an empty storage if the file doesn't exist or can't be opened, and
     * will try to save it.
     *
     * @see save()
     */
    public void load() {
        if (this.getDataFilePath().exists()) {
            try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream(this.getDataFilePath()))) {
                this.listOfSigns = (HashMap<Location, SignType>) stream.readObject();
            } catch (IOException ex) {
                this.plugin.log.warning("sign storage file " + this.getDataFilePath().getName() + " could not be read: " + ex.getMessage());
            } catch (ClassNotFoundException ex) {
                this.plugin.log.warning("encountered ClassNotFoundException: " + ex.getMessage());
                Logger.getLogger(Sign.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.plugin.log.info("loaded " + this.listOfSigns.size() + " signs");
        } else {
            this.plugin.log.info("sign storage file not found, attempting to save (create) one");
            this.save();
        }
    }

    /**
     * Returns the underlying {@link HashMap} of this storage.
     *
     * @return the hashmap
     */
    public HashMap<Location, SignType> getUnderlyingHashMap() {
        return this.listOfSigns;
    }

    /**
     * Sets the underlying {@link HashMap} of this storage. All previous
     * contents are lost.
     *
     * @param map the new {@code HashMap}
     */
    public void setUnderlyingHashMap(HashMap<Location, SignType> map) {
        this.listOfSigns = map;
    }
}

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import net.amunak.bukkit.mineauction.MineAuction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * Helper class with static methods to simplify work with MineAuction's signs
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public class MineAuctionSign {

    public final static String VALID_SIGN_IDENTIFIER = "[MineAuction]";
    public final static String INVALID_SIGN_IDENTIFIER = "*MineAuction*";

    /**
     * Reads the current formatting from the plugin's config file for the given
     * sign type
     *
     * @param type the SignType to get formatting for
     * @param plugin the plugin
     * @return list of four elements (each representing one line)
     */
    protected static List<String> getConfiguredFormatting(SignType type, MineAuction plugin) {
        List<String> lines = new ArrayList<>(4);
        lines.addAll(plugin.config.getStringList("options.signs.signTexts.header"));
        lines.addAll(2, plugin.config.getStringList("options.signs.signTexts.types." + type.getName()));
        for (String string : lines) {
            ChatColor.translateAlternateColorCodes('&', string);
        }
        return lines;
    }

    /**
     * Formats a sign using the plugin's config, rewriting all lines
     *
     * @param sign the sign to be formatted
     * @param type the type of the sign
     * @param plugin plugin to get config values from
     */
    public static void format(Sign sign, SignType type, MineAuction plugin) {
        List<String> lines = getConfiguredFormatting(type, plugin);
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, lines.get(i));
        }
    }

    /**
     * Handles the creation of a MineAuction sign
     *
     * @param sign the sign
     * @param type the type of sign
     * @param plugin plugin with signStorage
     */
    public static void handleCreation(Sign sign, SignType type, MineAuction plugin) {
        plugin.getSignsStorage().addItem(sign, type);
        plugin.getSignsStorage().save();
        format(sign, type, plugin);
    }

    /**
     * Handles the removal of a MineAuction sign
     *
     * @param sign the sign
     * @param plugin plugin with signStorage
     */
    public static void handleRemoval(Sign sign, MineAuction plugin) {
        plugin.getSignsStorage().removeItem(sign);
        plugin.getSignsStorage().save();
        invalidate(sign);
    }

    /**
     * Marks the sign as invalid, and outputs a message on its last line
     *
     * @param sign the sign
     * @param message the message
     */
    public static void invalidate(Sign sign, String message) {
        sign.setLine(0, INVALID_SIGN_IDENTIFIER);
        sign.setLine(3, ChatColor.RED + message);
    }

    /**
     * Convenience method, invalidates the sign with an empty message
     *
     * @see invalidate(MineAuctionSign s, String "")
     */
    public static void invalidate(Sign s) {
        invalidate(s, "");
    }

    /**
     * Checks whether the block is a valid sign (i.e. wall sign or sign post)
     *
     * @param block the block to check
     * @return true when it's a sign, false otherwise
     */
    public static boolean isSign(Block block) {
        return block.getType().equals(Material.SIGN_POST)
                || block.getType().equals(Material.WALL_SIGN);
    }

    /**
     * Validates the sign against the sign storage
     *
     * @param sign the sign
     * @param plugin plugin with signStorage
     * @return true if the sign is present in the storage, false otherwise
     */
    public static boolean isValidMineAuctionSign(Sign sign, MineAuction plugin) {
        return plugin.getSignsStorage().containsItem(sign);
    }

    /**
     * Convenience method, accepts {@link Block} instead of
     * {@link MineAuctionSign}.
     *
     * @see isValidMineAuctionSign(MineAuctionSign sign, MineAuction plugin)
     */
    public static boolean isValidMineAuctionSign(Block block, MineAuction plugin) {
        return isSign(block) && isValidMineAuctionSign((Sign) block.getState(), plugin);
    }

    /**
     * Checks if this sign's formatting is the same as defined in the config.
     *
     * @param sign the sign
     * @param plugin the plugin
     * @return true if correctly formatted, false otherwise
     */
    public static boolean isCorrectlyFormatted(Sign sign, SignType type, MineAuction plugin) {
        List<String> lines = getConfiguredFormatting(type, plugin);
        for (int i = 0; i < 4; i++) {
            if (!sign.getLine(i).equals(lines.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * A base for all Callable iterative operations on MineAuction's
     * MineAuctionSign
     */
    public abstract static class IterativeBase implements Callable {

        protected Sign sign;
        protected SignType signType;

        public final void setSign(Sign sign) {
            this.sign = sign;
        }

        public final void setSignType(SignType signType) {
            this.signType = signType;
        }

        public final Sign getSign() {
            return this.sign;
        }

        public final SignType getSignType() {
            return this.signType;
        }
    }

    private static class IterativeValidation extends IterativeBase {

        private final boolean checkFormatting;
        private final boolean forceCorrectFormat;
        private final MineAuction plugin;
        private final List<Integer> resultList;

        public IterativeValidation(boolean checkFormatting, boolean forceCorrectFormat, MineAuction plugin, List<Integer> resultList) {
            this.checkFormatting = checkFormatting;
            this.forceCorrectFormat = forceCorrectFormat;
            this.plugin = plugin;
            if (resultList == null) {
                this.resultList = new ArrayList<>(3);
            } else {
                this.resultList = resultList;
            }
        }

        @Override
        public Object call() {
            if (this.checkFormatting && !isCorrectlyFormatted(sign, signType, plugin)) {
                this.resultList.set(2, this.resultList.get(1) + 1);
                if (this.forceCorrectFormat) {
                    format(this.sign, this.signType, this.plugin);
                }
            } else if (this.forceCorrectFormat) {
                format(this.sign, this.signType, this.plugin);
            }
            return null;
        }
    }

    /**
     * Validates all known MineAuctionSign of this plugin using its
     * {@link SignStorage}, optionally removing invalid entries, checking
     * formatting and forcing the correct format on all the signs, optionally
     * logging the results
     *
     * @param removeInvalidEntries remove invalid entries (true) or just skip
     * them (false)?
     * @param checkFormatting check the formatting in the process? (this only
     * counts the incorrectly-formatted signs)
     * @param forceCorrectFormat force the correct format? (i.e. re-apply
     * {@code format} to all signs)
     * @param logResults log the results visibly?
     * @param logTarget the log target (null for console, or a valid
     * {@link Player}
     * @param plugin the plugin
     * @return an ordered list of all gathered data (results) of the validation.
     * Not applicable entries will show -1 as the result. {@code list[0]} is the
     * original number of entries, {@code list[1]} contains the number of
     * skipped/removed entries and {@code list[2]} contains the number of
     * entries which have/had wrong formatting (if applicable). Some entries may
     * contain -1. That indicates not applicable or errorneous result.
     */
    public static List<Integer> validateAll(boolean removeInvalidEntries, boolean checkFormatting, boolean forceCorrectFormat, boolean logResults, Player logTarget, MineAuction plugin) {
        List<Integer> resultList = new ArrayList<>(3);
        Level logLevel = Level.FINE;
        int operationResult;

        resultList.add(0, plugin.getSignsStorage().getUnderlyingHashMap().size());

        try {
            operationResult = iterateOverStorage(plugin.getSignsStorage(), removeInvalidEntries, new IterativeValidation(checkFormatting, forceCorrectFormat, plugin, resultList));
        } catch (Exception ex) {
            plugin.log.warning("Exception encountered in formatAll when iterating throiugh signs: " + ex.getMessage());
            operationResult = -1;
        }
        resultList.add(1, operationResult);
        if (!checkFormatting) {
            resultList.set(2, -1);
        }

        if (logResults) {
            logLevel = Level.INFO;
        }

        plugin.log.log(logLevel, logTarget, "All Storage entries validated. " + resultList.get(0) + " entries total, " + resultList.get(1) + " invalid (not sign), " + resultList.get(2) + " wrong formatting (different text on lines)");

        return resultList;
    }

    /**
     * Iterates through all signs in {@link SignStorage} and calls
     * {@link Callable} on them. When it encounters an invalid (i.e.
     * nonexistent) sign, it either removes it or skips it silently (depending
     * on {@code removeInvalidEntries}. {@code callable} is therefore guaranteed
     * to get an existing MineAuctionSign. The storage gets saved if any keys
     * get removed.
     *
     * @param storage the storage
     * @param removeInvalidEntries decides if invalid entries should be skipped
     * (false) or removed (true)
     * @param callable the method that gets called
     * @throws Exception
     * @return Number representing the amount of invalid sings that were present
     * in the storage
     */
    protected static int iterateOverStorage(SignStorage storage, boolean removeInvalidEntries, IterativeBase callable) throws Exception {
        int invalidEntries = 0;
        for (Map.Entry<Location, SignType> entry : storage.getUnderlyingHashMap().entrySet()) {
            if (isSign(entry.getKey().getBlock())) {
                callable.setSign((Sign) entry.getKey().getBlock().getState());
                callable.setSignType(entry.getValue());
                callable.call();
            } else {
                invalidEntries++;
                if (removeInvalidEntries) {
                    storage.removeItem(entry.getKey());
                }
            }
            if (removeInvalidEntries && invalidEntries > 0) {
                storage.save();
            }
        }
        return invalidEntries;
    }

    /**
     * Convenience method, doesn't remove invalid entries
     *
     * @see iterateOverStorage(SignStorage storage, boolean false, IterativeBase
     * callable)
     */
    protected static int iterateOverStorage(SignStorage storage, IterativeBase callable) throws Exception {
        return iterateOverStorage(storage, false, callable);
    }
}

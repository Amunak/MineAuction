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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import net.amunak.bukkit.mineauction.MineAuction;
import net.amunak.bukkit.mineauction.actions.VirtualInventory;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles all the interaction with MineAuction signs: their creation, removal
 * and player-interaction (right-clicking)
 *
 * Thanks Wolvereness for some of the block destruction code
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public final class SignInteractionListener implements Listener {

    protected MineAuction plugin;

    public SignInteractionListener(MineAuction p) {
        this.plugin = p;
        plugin.log.fine("SignInteractionListener registered");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void playerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                && MineAuctionSign.isValidMineAuctionSign(event.getClickedBlock(), plugin)) {
            switch (plugin.getSignsStorage().getSignType((Sign) event.getClickedBlock().getState())) {
                case DEPOSIT:
                    VirtualInventory.deposit(event.getPlayer());
                    break;
                case WITHDRAW:
                    VirtualInventory.withdraw(event.getPlayer());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Handles sign creation through signChangeEvent
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void signChangeEvent(SignChangeEvent event) {
        plugin.log.fine("SignChangeEvent'" + event.getLine(0).trim() + "' vs '" + MineAuctionSign.VALID_SIGN_IDENTIFIER + "'");
        if (event.getLine(0).trim().equalsIgnoreCase(MineAuctionSign.VALID_SIGN_IDENTIFIER)) {
            Player player = event.getPlayer();
            if (player.hasPermission("mineauction.signs.modify.place")) {
                SignType type = SignType.getByName(event.getLine(1).trim());
                if (type == null) {
                    MineAuctionSign.invalidate(event, "wrong type");
                    plugin.log.info(player, "Sign creation failed:" + ChatColor.RED + " wrong sign type");
                } else {
                    MineAuctionSign.handleCreation(event, type, plugin);
                    plugin.log.info(player.getName() + " created a MineAuction sign of type '" + type.toString() + "' at " + event.getBlock().getLocation().toString());
                }
            } else {
                MineAuctionSign.invalidate(event, "no permission");
                plugin.log.info(player, "Sign creation failed:" + ChatColor.RED + " insufficient permission");
                plugin.log.info(player.getName() + " tried to create interactive sign, but had no permission");
            }
        }


        //debug logging
        if (plugin.log.raiseFineLevel) {
            for (String string : event.getLines()) {
                char[] b = new char[100];
                for (int i = 0; i < string.length(); i++) {
                    b[i * 2] = string.charAt(i);
                    b[(i * 2) + 1] = '-';
                }
                plugin.log.fine(new String(b));
            }
        }
    }

    /**
     * Handles block breaking - both actual MA sign break and tries to break the
     * sign indirectly
     *
     * @param event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void blockBreakEvent(BlockBreakEvent event) {
        //prevent signs from getting removed
        if (!onBlockDestroyed(event, event.getPlayer(), MineAuctionSign.findAttachedSigns(event.getBlock()), event.getEventName())) {
            if (MineAuctionSign.isSign(event.getBlock())) {
                if (MineAuctionSign.isValidMineAuctionSign(event.getBlock(), plugin)) {
                    Player player = event.getPlayer();
                    if (player.hasPermission("mineauction.signs.modify.break")) {
                        Sign sign = (Sign) event.getBlock().getState();
                        MineAuctionSign.handleRemoval(sign, plugin);
                        plugin.log.info(player.getName() + " removed a MineAuction sign which was located at " + sign.getLocation().toString());
                    } else {
                        event.setCancelled(true);
                        plugin.log.info(player, "Sign removal failed:" + ChatColor.RED + " insufficient permission");
                        plugin.log.info(player.getName() + " tried to remove interactive sign, but had no permission");
                    }
                }
            }
        }
    }

    /**
     * @see onBlockDestroyed(Cancellable e, Entity entity, Collection<Block>
     * block, String eventName)
     */
    public boolean onBlockDestroyed(final Cancellable e, final Entity p, final Block b, final String eventName) {
        return onBlockDestroyed(e, p, Collections.singleton(b), eventName);
    }

    /**
     * Central place for checking for an event cancellation
     *
     * @param e Event
     * @param entity Player or Entity causing the event
     * @param blocks a Collection of blocks to check
     * @return true if event is cancelled
     */
    public boolean onBlockDestroyed(final Cancellable e, final Entity entity, final Collection<Block> blocks, final String eventName) {
        if (blocks != null) {
            HashSet<Block> allBlocks = new HashSet<>();
            for (Block block : blocks) {
                allBlocks.addAll(MineAuctionSign.findAttachedSigns(block));
            }
            allBlocks.addAll(blocks);
            for (Block block : allBlocks) {
                if (MineAuctionSign.isValidMineAuctionSign(block, this.plugin)) {
                    e.setCancelled(true);
                    String eName = "";
                    if (entity != null) {
                        eName = entity.getType().toString();
                        if (entity instanceof Player) {
                            eName += " {" + ((Player) entity).getName() + "} ";
                            this.plugin.log.warning((Player) entity, "Event cancelled - remove the MineAuction sign first if you intended to do that.");
                        }
                    }
                    this.plugin.log.fine(eName + " would destroy MineAuction sign by " + eventName + "; action prevented");
                    break;
                }
            }
        }
        return e.isCancelled();
    }

    /**
     * Block BlockBurnEvent if it destroyed sign
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBlockBurn(final BlockBurnEvent e) {
        onBlockDestroyed(e, null, e.getBlock(), e.getEventName());
    }

    /**
     * Block LeavesDecayEvent if it destroyed sign
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBlockFade(final BlockFadeEvent e) {
        onBlockDestroyed(e, null, e.getBlock(), e.getEventName());
    }

    /**
     * Block the BlockPistonExtendEvent if it moved a block with sign on it
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBlockPistonExtend(final BlockPistonExtendEvent e) {
        onBlockDestroyed(e, null, e.getBlocks(), e.getEventName());
    }

    /**
     * Block the BlockPistonRetractEvent if it moved a block with sign on it
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBlockPistonRetract(final BlockPistonRetractEvent e) {
        final BlockFace direction = e.getDirection();

        final HashSet<Block> blocks = new HashSet<>(2);
        // We need to check to see if a sign is attached to the piston piece
        final Block b = e.getBlock();
        blocks.add(b.getRelative(direction));
        if (!e.isSticky()) { // We only care about the second block if sticky piston is retracting.
            blocks.add(b.getRelative(direction, 2));
        }

        onBlockDestroyed(e, null, blocks, e.getEventName());
    }

    /**
     * Block the BlockPhysicsEvent if it would break a sign
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBlockPhysics(final BlockPhysicsEvent e) {
        onBlockDestroyed(e, null, e.getBlock(), e.getEventName());
    }

    /**
     * Stop EntityChangeBlockEvents from affecting signs. Prevents Endermen from
     * breaking signs
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockEntityChangeBlock(final EntityChangeBlockEvent e) {
        onBlockDestroyed(e, e.getEntity(), e.getBlock(), e.getEventName());
    }

    /**
     * Entity Explode event
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockEntityExplode(final EntityExplodeEvent e) {
        onBlockDestroyed(e, e.getEntity(), e.blockList(), e.getEventName());
    }

    /**
     * Block LeavesDecayEvent if it destroyed a sign
     *
     * @param e Event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockLeavesDecay(final LeavesDecayEvent e) {
        onBlockDestroyed(e, null, e.getBlock(), e.getEventName());
    }
}
package net.amunak.bukkit.mineauction.actions;

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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public class VirtualInventory {

    public static void deposit(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static void withdraw(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 8, "§4MineAuction Withdrawal inventory");
        inventory.setMaxStackSize(Integer.MAX_VALUE);
        inventory.addItem(new ItemStack(Material.STONE, 64));
        inventory.addItem(new ItemStack(Material.DIRT, 512));
        inventory.addItem(new ItemStack(Material.SIGN, 256));
        inventory.setItem(13, new ItemStack(Material.DIAMOND_HOE, 4));
        inventory.setItem((9 * 8) - 4, new ItemStack(Material.IRON_SWORD, 1));

        player.openInventory(inventory);
    }
}

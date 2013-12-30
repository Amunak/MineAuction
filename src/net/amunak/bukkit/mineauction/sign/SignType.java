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
/**
 * Enum containing all the MineAuction's sign types.
 *
 * @author Jiri Barous (Amunak) < http://amunak.net >
 */
public enum SignType {

    DEPOSIT("deposit"),
    WITHDRAW("withdraw");
    protected String name;

    private SignType(final String name) {
        this.name = name;
    }

    /**
     * Returns the string representation of this SignType
     *
     * @return the string representation of this SignType
     */
    public String getName() {
        return this.name;
    }

    /**
     * Looks up all the SignType and tries to match the name with a type. This
     * method is case-insensitive.
     *
     * @param name the name to search for
     * @return the type found or null if a type with such name doesn't exist
     */
    public static SignType getByName(String name) {
        for (SignType type : SignType.values()) {
            if (name.equalsIgnoreCase(type.getName())) {
                return type;
            }
        }
        return null;
    }
}

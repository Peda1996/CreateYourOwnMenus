/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.xhawk87.CreateYourOwnMenus.utils;

import java.util.ArrayList;
import java.util.List;

import me.xhawk87.CreateYourOwnMenus.CreateYourOwnMenus;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author XHawk87
 */
public class MenuScriptUtils {

    public static final String commandStart = "/";
    public static final String playerCommand = "@p/";
    public static final String hiddenCommand = ChatColor.COLOR_CHAR + "/";
    public static final String hiddenPlayerCommand = ChatColor.COLOR_CHAR + "@"
            + ChatColor.COLOR_CHAR + "p" + ChatColor.COLOR_CHAR + "/";

    /**
     * Strip all colour chars used to hide the lines
     *
     * @param firstLine The first line of the script
     * @return The unpacked hidden lines
     */
    public static List<String> unpackHiddenLines(String firstLine) {
        String[] lines = firstLine.split(ChatColor.COLOR_CHAR + "\r", -1);
        List<String> unpacked = new ArrayList<>();
        for (int i = 0; i < lines.length - 1; i++) {
            unpacked.add(unpackHiddenText(lines[i]));
        }
        unpacked.add(lines[lines.length - 1]);
        return unpacked;
    }

    /**
     * Remove colour chars from a line of text that are used to hide it, and
     * stop when reaching clear-text
     *
     * @param line The line to unpack
     * @return The unpacked line
     */
    public static String unpackHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        boolean expectColourChar = true;
        boolean clearText = false;
        for (char c : line.toCharArray()) {
            if (clearText) {
                sb.append(c);
            } else {
                if (expectColourChar) {
                    if (c != ChatColor.COLOR_CHAR) {
                        // Since there is no colour char, we must have reached 
                        // the end of the hidden section
                        sb.append(c);
                        clearText = true;
                    }
                } else {
                    sb.append(c);
                }
                expectColourChar = !expectColourChar;
            }
        }
        return sb.toString();
    }

    /**
     * Add a colour char to the front of each character in order to hide it
     *
     * @param line The line to pack
     * @return The packed line
     */
    public static String packHiddenText(String line) {
        StringBuilder sb = new StringBuilder();
        // Place a color char in front of each char in order to hide the comments
        for (char c : line.toCharArray()) {
            sb.append(ChatColor.COLOR_CHAR).append(c);
        }
        return sb.toString();
    }

    /**
     * Checks if the given ItemStack is a valid menu script item
     *
     * @param item The ItemStack to check
     * @return True if it is a valid menu item, otherwise false
     */
    public static boolean isValidMenuItem(ItemStack item) {
        boolean isValid = false;
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                // Check that the lore contains at least one scripted command
                List<String> loreStrings = meta.getLore();
                if (loreStrings.isEmpty()) {
                    return false;
                }
                loreStrings.addAll(unpackHiddenLines(loreStrings.get(0)));

                boolean onlyPlayerCommands = CreateYourOwnMenus.getConfigFile().getBoolean("only-playercommands");
                for (String loreString : loreStrings) {
                    if (loreString.startsWith(playerCommand)){
                        if (!onlyPlayerCommands) return true; // Skip the excess loop
                        isValid = true;
                    }
                    if (loreString.startsWith(commandStart)
                            || loreString.startsWith(hiddenPlayerCommand)
                            || loreString.startsWith(hiddenCommand)) { //hidden commands are not supported for now
                        return !onlyPlayerCommands; // Non player command, obey setting to determine validity
                    }
                }
            }
        }
        return isValid;
    }
}

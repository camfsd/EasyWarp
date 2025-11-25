package com.collinsrichard.easywarp;

import com.collinsrichard.easywarp.managers.WarpManager;
import com.collinsrichard.easywarp.objects.Warp;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class EWListener implements Listener {

    private static final String WARP_SIGN_IDENTIFIER = "[Warp]";

    public EasyWarp plugin;

    public EWListener(EasyWarp pt) {
        plugin = pt;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            cancelWarps(player);
        }
    }

    @EventHandler
    public void onHurt(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) e.getEntity();
        cancelWarps(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        // Ensure event is right-clicking
        if (!(e.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        // Ensure clicked block is a sign
        if (!(e.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        Player player = e.getPlayer();
        Sign sign = (Sign) e.getClickedBlock().getState();

        // Ensure sign begins with warp identifier
        if (!(sign.getLine(0).equals(WARP_SIGN_IDENTIFIER))) {
            return;
        }

        e.setCancelled(true);

        String warpName = ChatColor.stripColor(sign.getLine(1));

        // Cancel if warp does not exist
        if (!WarpManager.isWarp(warpName)) {
            sign.setLine(0, ChatColor.DARK_RED + "!ERROR!");
            sign.setLine(1, "Warp does");
            sign.setLine(2, "not exist.");
            sign.setLine(3, "");
            return;
        }

        Warp warp = WarpManager.getWarp(warpName);

        // Check general permission of warper
        String perms = "easywarp.sign.use";
        if (Settings.signsReqPerms && !player.hasPermission(perms)) {
            // Create error message for no permission
            HashMap<String, String> values = new HashMap<>();
            values.put("node", perms);
            Helper.sendParsedMessage(player, Settings.getMessage("error.no-permission"), values);
            return;
        }

        // Check warp-specific permission of warper
        perms = "easywarp.warp." + warpName;
        if (Settings.signsPerWarpPerms && !player.hasPermission(perms)) {
            // Create error message for no permission
            HashMap<String, String> values = new HashMap<>();
            values.put("node", perms);
            Helper.sendParsedMessage(player, Settings.getMessage("error.no-permission"), values);
            return;
        }

        // Do warp
        Helper.warpSign(player, warp);
    }

    @EventHandler
    public void onSignCreate(SignChangeEvent e) {
        // Check if sign begins with warp sign identifiers
        if (!(match(e.getLine(0),
                new String[]{"[EASYWARP]", "[EW]", "[EASY WARP]", "[EWARP]", "[E WARP]", "[E W]", "[WARP]"}))) {
            return;
        }

        Player player = e.getPlayer();

        // Check permission to create warp signs
        if (!player.hasPermission("easywarp.sign.create")) {
            e.setLine(0, ChatColor.DARK_RED + "!ERROR!");
            e.setLine(1, "You do not have");
            e.setLine(2, "permission to");
            e.setLine(3, "make warp signs");
            return;
        }

        String warpName = e.getLine(1);

        // Cancel if warp does not exist
        if (!WarpManager.isWarp(warpName)) {
            e.setLine(0, ChatColor.DARK_RED + "!ERROR!");
            e.setLine(1, "This warp");
            e.setLine(2, "does not");
            e.setLine(3, "exist");
            return;
        }

        // Format the sign to enable warp
        e.setLine(0, ChatColor.DARK_BLUE + WARP_SIGN_IDENTIFIER);
        e.setLine(1, ChatColor.BLACK + e.getLine(1));
        e.setLine(2, ChatColor.DARK_GRAY + e.getLine(2));
        e.setLine(3, ChatColor.DARK_GRAY + e.getLine(3));

        // Create warp and notify player
        HashMap<String, String> values = new HashMap<>();
        Helper.sendParsedMessage(player, Settings.getMessage("sign.create"), values);
    }

    private void cancelWarps(Player player) {
        // Do nothing if player isn't warping
        if (!Helper.isWarping(player)) {
            return;
        }

        // Stop the warp and notify the player
        Helper.stopWarping(player);
        Helper.sendParsedMessage(player, Settings.getMessage("warp.cancelled"), new HashMap<>());
    }

    private static boolean match(String x, String[] split) {
        String xx = ChatColor.stripColor(x);

        for (String y : split) {
            String yy = ChatColor.stripColor(y);

            if (xx.equalsIgnoreCase(yy)) {
                return true;
            }
        }

        return false;
    }

}

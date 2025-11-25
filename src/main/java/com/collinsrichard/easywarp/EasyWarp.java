package com.collinsrichard.easywarp;

import com.collinsrichard.easywarp.commands.*;
import com.collinsrichard.easywarp.managers.FileManager;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyWarp extends JavaPlugin {

    // TODO: Inject dependencies, avoid static abuse
    public static final String NAME = "EasyWarp";
    public static FileManager fileManager;

    @Override
    public void onEnable() {
        // Register events
        getServer().getPluginManager().registerEvents(new EWListener(this), this);

        // Load config
        saveDefaultConfig();
        reloadConfig();
        Settings.load(this);

        // Load warps
        fileManager = new FileManager();
        fileManager.loadWarps();

        registerCommands();
    }

    @Override
    public void onDisable() {
        fileManager.saveWarps();
    }

    private void registerCommands() {
        getCommand("delwarp").setExecutor(new DeleteWarpCommand());
        getCommand("easywarp").setExecutor(new EasyWarpCommand());
        getCommand("listwarp").setExecutor(new ListWarpsCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand());
        getCommand("warp").setExecutor(new WarpCommand());
    }
}

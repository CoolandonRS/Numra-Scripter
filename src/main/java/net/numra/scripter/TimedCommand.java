package net.numra.scripter;

import org.bukkit.Server;
import org.bukkit.scheduler.BukkitRunnable;

public class TimedCommand extends BukkitRunnable {
    private Scripter plugin;
    private String command;
    private long time;
    
    public TimedCommand(String commandStr, long time, Scripter plugin) {
        this.plugin = plugin;
        this.command = commandStr;
        this.time = time;
    }
    
    public void start() {
        this.runTaskLater(plugin, time * 20);
    }
    
    @Override
    public void run() {
        Server server = plugin.getServer();
        server.dispatchCommand(server.getConsoleSender(), command);
        plugin.getLogger().info("Ran scheduled command: " + command);
        this.start();
    }
}

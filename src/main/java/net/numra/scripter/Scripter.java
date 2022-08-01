package net.numra.scripter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import java.util.*;

public final class Scripter extends JavaPlugin {
    private FileConfiguration mainConfig = this.getConfig();
    private Config scriptConfig = new Config("scripts", this);
    private Config scheduleConfig = new Config("schedules", this);
    private boolean enabled;
    private File scriptDir;
    private HashMap<String, File> scripts = new HashMap<>(Map.of());
    private ArrayList<TimedCommand> timedCommands = new ArrayList<>();
    
    @Override
    public void onEnable() {
        mainConfig.addDefault("enabled", true);
        mainConfig.addDefault("script_dir", new File(this.getDataFolder(), "scripts").getAbsolutePath());
        mainConfig.options().copyDefaults(true);
        saveConfig();
    
        if (!scriptConfig.getFile().exists()) {
            scriptConfig.get().options().setHeader(List.of("[Script name]: [Script file]"));
            scriptConfig.get().createSection("scripts", Map.of("example", "example.bat"));
            scriptConfig.save();
        }
        
        if (!scheduleConfig.getFile().exists()) {
            scheduleConfig.get().options().setHeader(List.of("For timed:"," - time: [time in seconds]","    command: [command to run]"));
            ConfigurationSection scheduleSection = new MemoryConfiguration();
            scheduleSection.set("time", 86400);
            scheduleSection.set("command", "example");
            scheduleConfig.get().set("timed", List.of(scheduleSection));
            scheduleConfig.save();
        }
        
        loadConfigVars();
        
        ScripterCommand scripterCommand = new ScripterCommand(this);
        Objects.requireNonNull(this.getCommand("scripter")).setExecutor(scripterCommand);
        Objects.requireNonNull(this.getCommand("scripter")).setTabCompleter(scripterCommand);
        
        if (enabled) this.getLogger().info(ChatColor.GREEN + this.getName() + " enabled!");
    }
    
    public HashMap<String, File> getScripts() {
        return scripts;
    }
    
    /**
     * Loose disabling disables all features except /scripter reload
     * @return Whether the plugin is loose disabled or not
     */
    public boolean isLooseDisabled() {
        return !this.enabled;
    }
    
    public void reloadConfigs() {
        this.getLogger().info("Reloading Config");
        
        reloadConfig();
        scriptConfig.reload();
    
        mainConfig = this.getConfig();
    
        loadConfigVars();
    }
    
    private void loadConfigVars() {
        // config.yml
        enabled = mainConfig.getBoolean("enabled");
        if (!enabled) {
            this.getLogger().info(ChatColor.RED + this.getName() + " disabled by config.");
            return;
        }
        scriptDir = new File(Objects.requireNonNull(mainConfig.getString("script_dir")));
        if (!scriptDir.exists() && !scriptDir.mkdirs()) this.getLogger().severe("Script Dir Creation Failed");
        
        // scripts.yml
        ConfigurationSection scriptSection = scriptConfig.get().getConfigurationSection("scripts");
        scripts.clear();
        assert scriptSection != null;
        for (String key : scriptSection.getKeys(false)) {
            scripts.put(key, new File(scriptDir, Objects.requireNonNull(scriptSection.getString(key))));
        }
        
        // schedules.yml
        ConfigurationSection scheduleSection = scheduleConfig.get().getConfigurationSection("timed");
    }
    
    @Override
    public void onDisable() {
        if (enabled) this.getLogger().info( ChatColor.RED + this.getName() + " disabled!");
    }
}

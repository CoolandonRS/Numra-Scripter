package net.numra.scripter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public final class Scripter extends JavaPlugin {
    private FileConfiguration mainConfig = this.getConfig();
    private Config scriptConfig = new Config("scripts", this);
    private boolean enabled;
    private File scriptDir;
    private HashMap<String, File> scripts = new HashMap<>(Map.of());
    
    @Override
    public void onEnable() {
        mainConfig.addDefault("enabled", true);
        mainConfig.addDefault("script_dir", new File(this.getDataFolder(), "scripts").getAbsolutePath());
        mainConfig.options().copyDefaults(true);
        saveConfig();
    
        if (!scriptConfig.getFile().exists()) {
            scriptConfig.get().options().setHeader(List.of(new String[]{"Script name: Script to run"}));
            scriptConfig.get().createSection("scripts", Map.of("example", "example.bat"));
            scriptConfig.save();
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
        // commands.yml
        ConfigurationSection section = scriptConfig.get().getConfigurationSection("scripts");
        scripts.clear();
        assert section != null;
        for (String key : section.getKeys(false)) {
            scripts.put(key, new File(scriptDir, Objects.requireNonNull(section.getString(key))));
        }
    }
    
    @Override
    public void onDisable() {
        if (enabled) this.getLogger().info( ChatColor.RED + this.getName() + " disabled!");
    }
}

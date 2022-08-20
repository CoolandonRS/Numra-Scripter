package net.numra.scripter;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import java.util.*;

public final class Scripter extends JavaPlugin {
    public static final Map<ConfigType, Integer> configVersions = Map.of(ConfigType.Main, 1, ConfigType.Script, 1, ConfigType.Schedule, 1);
    
    private Config mainConfig = new Config("config", this);
    private Config scriptConfig = new Config("scripts", this);
    private Config scheduleConfig = new Config("schedules", this);
    private boolean enabled;
    private File scriptDir;
    private HashMap<String, File> scripts = new HashMap<>(Map.of());
    private ArrayList<TimedCommand> timedCommands = new ArrayList<>();
    
    @Override
    public void onEnable() {
        if (!mainConfig.getFile().exists()) {
            generateMainConfig();
        }
    
        if (!scriptConfig.getFile().exists()) {
            generateScriptConfig();
        }
        
        if (!scheduleConfig.getFile().exists()) {
            generateScheduleConfig();
        }
        
        reloadConfigs();
        
        ScripterCommand scripterCommand = new ScripterCommand(this);
        Objects.requireNonNull(this.getCommand("scripter")).setExecutor(scripterCommand);
        Objects.requireNonNull(this.getCommand("scripter")).setTabCompleter(scripterCommand);
        
        if (enabled) this.getLogger().info(ChatColor.GREEN + this.getName() + " enabled!");
    }
    
    private void generateMainConfig() {
        mainConfig.get().set("enabled", true);
        mainConfig.get().set("script_dir", new File(this.getDataFolder(), "scripts").getAbsolutePath());
        mainConfig.get().set("version", configVersions.get(ConfigType.Main));
        mainConfig.get().setComments("version", List.of("Don't change me!"));
        mainConfig.save();
    }
    
    private void generateScriptConfig() {
        scriptConfig.get().options().setHeader(List.of("[Script name]: [Script file]"));
        scriptConfig.get().createSection("scripts", Map.of("example", "example.bat"));
        scriptConfig.get().set("version", configVersions.get(ConfigType.Script));
        scriptConfig.get().setComments("version", List.of("Don't change me!"));
        scriptConfig.save();
    }
    
    private void generateScheduleConfig() {
        scheduleConfig.get().options().setHeader(List.of("For timed:"," - time: [time in seconds]","   command: [command to run]"));
        ConfigurationSection scheduleSection = new MemoryConfiguration();
        scheduleSection.set("time", 86400);
        scheduleSection.set("command", "example");
        scheduleConfig.get().set("timed", List.of(scheduleSection));
        scheduleConfig.get().set("version", configVersions.get(ConfigType.Schedule));
        scheduleConfig.get().setComments("version", List.of("Don't change me!"));
        scheduleConfig.save();
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
        
        mainConfig.reload();
        scriptConfig.reload();
        scheduleConfig.reload();
    
        if (!mainConfig.compatibleVersion(configVersions.get(ConfigType.Main), ConfigType.Main)) {
            mainConfig.delete();
            generateMainConfig();
            mainConfig.reload();
            getLogger().warning("Main config reset");
        }
    
        if (!scriptConfig.compatibleVersion(configVersions.get(ConfigType.Script), ConfigType.Script)) {
            scriptConfig.delete();
            generateScriptConfig();
            scriptConfig.reload();
            getLogger().warning("Script config reset");
        }
    
        if (!scheduleConfig.compatibleVersion(configVersions.get(ConfigType.Schedule), ConfigType.Schedule)) {
            scheduleConfig.delete();
            generateScheduleConfig();
            scheduleConfig.reload();
            getLogger().warning("Schedule config reset");
        }
    
        loadConfigVars();
    }
    
    private void loadConfigVars() {
        // config.yml
        enabled = mainConfig.get().getBoolean("enabled");
        if (!enabled) {
            this.getLogger().info(ChatColor.RED + this.getName() + " disabled by config.");
            return;
        }
        scriptDir = new File(Objects.requireNonNull(mainConfig.get().getString("script_dir")));
        if (!scriptDir.exists() && !scriptDir.mkdirs()) this.getLogger().severe("Script Dir Creation Failed");
        
        // scripts.yml
        ConfigurationSection scriptSection = scriptConfig.get().getConfigurationSection("scripts");
        scripts.clear();
        assert scriptSection != null;
        for (String key : scriptSection.getKeys(false)) {
            scripts.put(key, new File(scriptDir, Objects.requireNonNull(scriptSection.getString(key))));
        }
        // schedules.yml
        timedCommands.forEach(TimedCommand::cancel);
        timedCommands.clear();
        for (Map<?, ?> m : scheduleConfig.get().getMapList("timed")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) m;
            if (map.get("time") == null || map.get("command") == null) {
                getLogger().warning("Invalid entry in schedules.yml");
                continue;
            }
            timedCommands.add(new TimedCommand((String) map.get("command"), (Integer) map.get("time"), this));
        }
        timedCommands.forEach(TimedCommand::start);
    }
    
    @Override
    public void onDisable() {
        timedCommands.forEach(TimedCommand::cancel);
        if (enabled) this.getLogger().info( ChatColor.RED + this.getName() + " disabled!");
    }
}

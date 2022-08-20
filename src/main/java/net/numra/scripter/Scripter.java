package net.numra.scripter;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.CommentedConfigurationNode;

import java.io.File;

import java.util.*;

import static java.util.Map.entry;

public final class Scripter extends JavaPlugin {
    public static final Map<ConfigType, Integer> configVersions = Map.ofEntries(
            entry(ConfigType.Main, 1),
            entry(ConfigType.Script, 1),
            entry(ConfigType.Schedule, 1)
    );
    private final Map<ConfigType, Config> configs = Map.ofEntries(
            entry(ConfigType.Main, new Config("config", this, config -> {
                config.node("enabled").raw(true);
                config.node("scriptDir").raw(new File(this.getDataFolder(), "scripts").getAbsolutePath());
                CommentedConfigurationNode versionNode = config.node("version");
                versionNode.raw(configVersions.get(ConfigType.Main));
                versionNode.comment("Don't change me!");
            })),
            entry(ConfigType.Script, new Config("scripts", this, config -> {
                config.comment("[Script name]: [Script file]");
                config.node("scripts").node("example").raw("example.bat");
                CommentedConfigurationNode versionNode = config.node("version");
                versionNode.raw(configVersions.get(ConfigType.Script));
                versionNode.comment("Don't change me!");
            })),
            entry(ConfigType.Schedule, new Config("schedules", this, config -> {
                config.comment("time: [time in seconds]\ncommand: [command to run]");
                CommentedConfigurationNode listNode = config.node("timed").appendListNode();
                listNode.node("time").raw(86400);
                listNode.node("command").raw("example");
                CommentedConfigurationNode versionNode = config.node("version");
                versionNode.raw(configVersions.get(ConfigType.Schedule));
                versionNode.comment("Don't change me!");
            }))
    );
    
    private boolean enabled;
    private File scriptDir;
    private HashMap<String, File> scripts = new HashMap<>(Map.of());
    private ArrayList<TimedCommand> timedCommands = new ArrayList<>();
    
    @Override
    public void onEnable() {
        configs.forEach((type, config) -> config.verifyVersion(configVersions.get(type), type));
        
        reloadConfigs();
        
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
        
        configs.values().forEach(Config::reload);
    
        loadConfigVars();
    }
    
    private void loadConfigVars() {
        // config.yml
        CommentedConfigurationNode main = configs.get(ConfigType.Main).get();
        enabled = main.node("enabled").getBoolean();
        if (!enabled) {
            this.getLogger().info(ChatColor.RED + this.getName() + " disabled by config.");
            return;
        }
        scriptDir = new File(main.node("scriptDir").getString(new File(this.getDataFolder(), "scripts").getAbsolutePath()));
        if (!scriptDir.exists() && !scriptDir.mkdirs()) this.getLogger().severe("Script Dir Creation Failed");
        
        // scripts.yml
        CommentedConfigurationNode script = configs.get(ConfigType.Script).get();
        scripts.clear();
        script.node("scripts").childrenMap().forEach((key, node) -> scripts.put((String) key, new File(scriptDir, Objects.requireNonNull(node.getString()))));
        // schedules.yml
        timedCommands.forEach(TimedCommand::cancel);
        timedCommands.clear();
        CommentedConfigurationNode schedule = configs.get(ConfigType.Schedule).get();
        schedule.node("timed").childrenList().forEach(node -> {
            int time = node.node("time").getInt();
            String commandStr = node.node("command").getString();
            if (time <= 0 || commandStr == null) {
                getLogger().warning("Invalid entry in schedule! Skipping.");
            } else {
                timedCommands.add(new TimedCommand(commandStr, time, this));
            }
        });
        timedCommands.forEach(TimedCommand::start);
    }
    
    @Override
    public void onDisable() {
        timedCommands.forEach(TimedCommand::cancel);
        if (enabled) this.getLogger().info( ChatColor.RED + this.getName() + " disabled!");
    }
}

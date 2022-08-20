package net.numra.scripter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public class Config {
    private static final Map<ConfigType, Map<Integer, List<Integer>>> compatibleVersions = Map.ofEntries(
            entry(ConfigType.Main, Map.ofEntries(
                    entry(1, List.of(1))
            )),
            entry(ConfigType.Script, Map.ofEntries(
                    entry(1, List.of(1))
            )),
            entry(ConfigType.Schedule, Map.ofEntries(
                    entry(1, List.of(1))
            ))
    );
    
    private final Scripter plugin;
    private FileConfiguration config;
    private final File file;
    
    public void reload() {
        config = YamlConfiguration.loadConfiguration(file);
    }
    
    public FileConfiguration get() {
        return config;
    }
    
    public File getFile() {
        return file;
    }
    
    public void unsafeSave() throws IOException {
        config.save(file);
    }
    
    public boolean save() {
        try {
            this.unsafeSave();
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save config " + file.getName());
            return false;
        }
    }
    
    public boolean delete() {
        boolean success = file.delete();
        if (success) this.config = new YamlConfiguration();
        return success;
    }
    
    public Config(String name, Scripter plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }
    
    public boolean compatibleVersion(Integer version, ConfigType type) {
        if (!config.isSet("version")) return false;
        return compatibleVersions.get(type).get(version).contains(config.getInt("version"));
    }
}

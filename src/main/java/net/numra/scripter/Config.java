package net.numra.scripter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {
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
    
    public void save() {
        try {
            this.unsafeSave();
        } catch (IOException e) {
            plugin.getLogger().severe("Unable to save config " + file.getName());
        }
    }
    
    public Config(String name, Scripter plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}

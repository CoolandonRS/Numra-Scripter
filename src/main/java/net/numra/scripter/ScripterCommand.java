package net.numra.scripter;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScripterCommand implements TabExecutor {
    private final Scripter plugin;
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) return false;
        switch (args[0]) {
            case "reload" -> {
                if (args.length != 1) return false;
                plugin.reloadConfigs();
                sender.sendMessage(plugin.getName() + " Configs Reloaded!");
            }
            case "run" -> {
                if (args.length != 2) return false;
                if (plugin.isLooseDisabled()) {
                    sender.sendMessage(plugin.getName() + " is disabled.");
                    return true;
                }
                if (plugin.getScripts().containsKey(args[1])) {
                    File file = plugin.getScripts().get(args[1]);
                    if (!file.exists()) {
                        sender.sendMessage("File not found");
                        return true;
                    }
                    if (!file.canExecute()) {
                        sender.sendMessage("File is not executable");
                    }
                    try {
                        Runtime.getRuntime().exec(file.getAbsolutePath());
                        sender.sendMessage("Executing " + args[1]);
                        plugin.getLogger().info(sender.getName() + " ran script " + args[1]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    sender.sendMessage("Unknown script");
                }
            }
            default -> {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {
            case 1 -> {
                ArrayList<String> tabList = new ArrayList<>();
                if (sender.permissionValue("scripter.run").toBooleanOrElse(sender.isOp()) && !this.plugin.isLooseDisabled()) tabList.add("run");
                if (sender.permissionValue("scripter.reload").toBooleanOrElse(sender.isOp()))  tabList.add("reload");
                
                return StringUtil.copyPartialMatches(args[0], tabList, new ArrayList<>());
            }
            case 2 -> {
                if (args[0].equals("run")) {
                    if (sender.permissionValue("scripter.run").toBooleanOrElse(sender.isOp()) && !this.plugin.isLooseDisabled()) return plugin.getScripts().keySet().stream().toList();
                }
            }
        }
        return List.of();
    }
    
    public ScripterCommand(Scripter plugin) {
        this.plugin = plugin;
    }
}

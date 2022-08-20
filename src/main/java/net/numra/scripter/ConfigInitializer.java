package net.numra.scripter;

import org.spongepowered.configurate.CommentedConfigurationNode;

public interface ConfigInitializer {
    void init(CommentedConfigurationNode node);
}

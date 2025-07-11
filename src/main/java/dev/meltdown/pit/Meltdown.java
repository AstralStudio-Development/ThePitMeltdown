package dev.meltdown.pit;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.mode.Mode;
import cn.charlotte.pit.perk.type.streak.beastmode.BeastModeMegaStreak;
import cn.charlotte.pit.scoreboard.Scoreboard;
import cn.charlotte.pit.util.chat.CC;
import cn.charlotte.pit.util.command.util.ClassUtil;
import cn.charlotte.pit.util.nametag.NametagHandler;
import cn.charlotte.pit.util.scoreboard.Assemble;
import dev.meltdown.pit.config.FileManager;
import dev.meltdown.pit.listener.DataListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Meltdown {

    protected String version = "1.0.2";

    public static FileManager fileManager;
    public static YamlConfiguration config;

    public static final String prefix = "&c[Meltdown] &f";

    public static void init() {
        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Meltdown starting..."));
        //Meltdown Configuration
        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Starting load Meltdown configuration..."));
        fileManager = new FileManager(ThePit.getInstance());
        config = fileManager.getConfig();

        ThePit.mode = Mode.valueOf(config.getString("pit-mode"));
        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "PitMode: " + ThePit.mode));

        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Configuration Done."));

        //Meltdown Listener
        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Starting load Meltdown listeners..."));
        Bukkit.getServer().getPluginManager().registerEvents(new DataListener(), ThePit.getInstance());

        registerListeners("cn.charlotte.pit.enchantment.type.alternative");
        registerListeners("cn.charlotte.pit.enchantment.type.addon");
        registerListeners("cn.charlotte.pit.enchantment.type.normal");
        registerListeners("cn.charlotte.pit.perk.type");

        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Listeners Done."));

        //End
        Bukkit.getConsoleSender().sendMessage(CC.translate(prefix + "Meltdown Done."));
    }

    private static void registerListeners(String packageName) {
        Collection<Class<?>> classes = ClassUtil.getClassesInPackage(ThePit.getInstance(), packageName);
        classes.stream()
                .filter(Listener.class::isAssignableFrom)
                .map(clazz -> {
                    try {
                        return (Listener) clazz.newInstance();
                    } catch (Exception ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, ThePit.getInstance()));
    }
}

package de.daniel.backpack;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import de.daniel.backpack.Command.BackpackCommand;
import de.daniel.backpack.Management.MySQL;

import java.io.File;

public class BackpackSystemMain extends PluginBase {

    private static BackpackSystemMain instance;
    public static BackpackSystemMain getInstance() {
        return instance;
    }

    public static Config cfg;
    public static Config cfgMessages;

    public boolean configMode;

    @Override
    public void onEnable() {
        instance = this;
        saveResource("config.yml");
        saveResource("messages.yml");

        cfg = new Config(new File(instance.getDataFolder(), "config.yml"));
        cfgMessages = new Config(new File(instance.getDataFolder(), "messages.yml"));

        if (cfg.getString("database").equalsIgnoreCase("mysql")) {
            MySQL.connect();

            if (!MySQL.isConnected()) {
                getLogger().alert("§4No MySQL-Server connection. Please check the file \"§econfig.yml§4\"");
                getPluginLoader().disablePlugin(this);
                return;
            }

            MySQL.update("CREATE TABLE IF NOT EXISTS PlayerInventories (UUID VARCHAR(50) PRIMARY KEY, Playername VARCHAR(100), Slot1 VARCHAR(10), Slot2 VARCHAR(10), Slot3 VARCHAR(10), Slot4 VARCHAR(10), Slot5 VARCHAR(10))");
            this.configMode = false;

        } else if (cfg.getString("database").equalsIgnoreCase("config")) {
            this.configMode = true;
            getLogger().info("ConfigMode acivated!");
        } else {
            getLogger().alert("§4No Database. Please check the file \"§econfig.yml§4\"");
            getPluginLoader().disablePlugin(this);
            return;
        }

        getServer().getCommandMap().register("backpack", new BackpackCommand());
        getServer().getPluginManager().registerEvents(new BackpackCommand(), this);

        getLogger().info("§b" + getName() + " §fwas successfully §aEnabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("§b" + getName() + " §fwas successfully §cDisabled");
    }
}
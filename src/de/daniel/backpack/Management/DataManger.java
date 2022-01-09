package de.daniel.backpack.Management;

import cn.nukkit.IPlayer;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.utils.Config;
import de.daniel.backpack.BackpackSystemMain;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataManger {

    private static boolean configMode = BackpackSystemMain.getInstance().configMode;

    public static void saveInventory(IPlayer p, Map<Integer, Item> map) {

        for (int i = 0; i < 5; i++) {

            Item item = map.get(i);

            StringBuilder itemString = new StringBuilder();
            boolean b = false;

            if (item != null) {

                StringBuilder enchantments = new StringBuilder();

                for (Enchantment e : item.getEnchantments()) {
                    if (b) {
                        enchantments.append("&" + e.getId() + ":" + e.getLevel());
                    } else {
                        enchantments.append(e.getId() + ":" + e.getLevel());
                        b = true;
                    }
                }

                itemString.append(item.getId() + ":");
                itemString.append(item.getDamage() + "/");
                itemString.append(item.getCount() + "/");
                if (item.hasCustomName()) {
                    itemString.append(item.getCustomName() + "/");
                } else {
                    itemString.append("null/");
                }
                if (item.getLore().length != 0) {
                    itemString.append(Arrays.toString(item.getLore()) + "/");
                } else {
                    itemString.append("null/");
                }
                if (enchantments.length() != 0) {
                    itemString.append(enchantments.toString());
                } else {
                    itemString.append("null");
                }
            }

            if (configMode) {
                Config cfg = new Config(new File(BackpackSystemMain.getInstance().getDataFolder(), "db.yml"));

                cfg.set(p.getUniqueId() + ".slot" + i, itemString.toString());
                cfg.save();
            } else {
                MySQL.update("REPLACE INTO PlayerInventories (UUID, Playername, Slot" + i + ") " +
                        "VALUES ('" + p.getUniqueId() + "','" + p.getName() + "','" + itemString.toString() + "')");
            }

        }
    }

    public static HashMap<Integer, Item> getItemMap(UUID uuid) {
        HashMap<Integer, Item> itemMap = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            itemMap.put(i, getItem(uuid, i));
        }

        return itemMap;
    }

    public static Item getItem(UUID uuid, int slot) {
        String itemString = null;

        if (configMode) {
            Config cfg = new Config(new File(BackpackSystemMain.getInstance().getDataFolder(), "db.yml"));
            itemString = cfg.getString(uuid + ".slot" + slot);
            if (itemString.length() == 0) {
                itemString = null;
            }
        } else {
            ResultSet rs = MySQL.getResult("SELECT * FROM PlayerInventories WHERE UUID='" + uuid + "'");
            try {
                if (rs.next()) {
                    itemString = rs.getString("Slot" + slot);
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if (itemString == null) {
            return new Item(0);
        }

        String[] itemargs = itemString.split("/");

        Item item = Item.fromString(itemargs[0]);
        item.setCount(Integer.parseInt(itemargs[1]));
        if (!itemargs[2].equals("null")) {
            item.setCustomName(itemargs[2]);
        }
        if (!itemargs[3].equals("null")) {
            item.setLore(itemargs[3]);
        }

        if (!itemargs[4].equals("null")) {
            String[] enchantmentargs = itemargs[4].split("&");

            for (int i = 0; i < enchantmentargs.length; i++) {
                String[] enchantargs = enchantmentargs[i].split(":");
                item.addEnchantment(Enchantment.get(Integer.parseInt(enchantargs[0])).setLevel(Integer.parseInt(enchantargs[1]), true));
            }
        }

        return item;
    }
}

package de.daniel.backpack.Command;

import cn.nukkit.IPlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.inventory.InventoryCloseEvent;
import cn.nukkit.item.Item;
import cn.nukkit.utils.Config;
import com.nukkitx.fakeinventories.inventory.FakeInventoryListener;
import com.nukkitx.fakeinventories.inventory.FakeSlotChangeEvent;
import de.daniel.backpack.BackpackSystemMain;
import de.daniel.backpack.Management.DataManger;
import de.daniel.blockInventories.HopperInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BackpackCommand extends Command implements Listener {

    private static final ConcurrentHashMap<Player, HopperInventory> playerInventoryMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Player, IPlayer> openOtherInventory = new ConcurrentHashMap<>();

    private static Config cfgMessages = BackpackSystemMain.cfgMessages;

    public BackpackCommand() {
        super(cfgMessages.getString("command.name"),
                cfgMessages.getString("command.description"),
                cfgMessages.getString("command.usageMessage"),
                cfgMessages.getString("command.aliases").split(","));

        commandParameters.clear();
        commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("player", CommandParamType.TARGET, false)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] args) {
        String prefix = cfgMessages.getString("prefix");

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only Ingame!");
            return false;
        }

        if (args.length > 1) {
            sender.sendMessage(prefix + usageMessage);
            return false;
        }

        Player p = (Player) sender;

        if (!p.hasPermission("command.backpack")) {
            p.sendMessage(prefix + cfgMessages.getString("messages.nopermission"));
            return false;
        }

        IPlayer target = p;

        if (args.length == 1) {
            if (!p.hasPermission("command.backpack.view.other")) {
                p.sendMessage(prefix + cfgMessages.getString("messages.nopermission"));
                return false;
            }

            target = Server.getInstance().getOfflinePlayer(args[0]);

            openOtherInventory.put(p, target);
        }

        HopperInventory hi = new HopperInventory();
        hi.setName(target.getName() + "'s backpack");



        /*
        if (openOtherInventory.containsValue(p)) {
            for (Player watchers : openOtherInventory.keySet()) {
                if (p == openOtherInventory.get(watchers)) {
                    for (int i : playerInventoryMap.get(watchers).slots.keySet()) {
                        Item item = playerInventoryMap.get(watchers).slots.get(i);
                        hi.setItem(i, item);
                    }
                }
            }
        } else {

         */

        Map<Integer, Item> itemMap = null;
        if (playerInventoryMap.containsKey(target)) {
            itemMap = playerInventoryMap.get(target).slots;
        } else {
            if (openOtherInventory.containsValue(p)) {
                for (Player player : openOtherInventory.keySet()) {
                    IPlayer iPlayer = openOtherInventory.get(player);
                    if (iPlayer == p) {
                        itemMap = playerInventoryMap.get(player).slots;
                    }
                }
            } else {
                itemMap = DataManger.getItemMap(target.getUniqueId());
            }
        }
        
        
        for (int i : itemMap.keySet()) {
            Item item = itemMap.get(i);
            hi.setItem(i, item);
        }
        //}


        hi.addListener(new FakeInventoryListener() {
            @Override
            public void onSlotChange(FakeSlotChangeEvent fakeSlotChangeEvent) {
                Player p = fakeSlotChangeEvent.getPlayer();
                int slot = fakeSlotChangeEvent.getAction().getSlot();
                //Item itemBefore = fakeSlotChangeEvent.getAction().getSourceItem();
                Item itemAfter = fakeSlotChangeEvent.getAction().getTargetItem();

                if (openOtherInventory.containsKey(p)) {
                    if (!p.hasPermission("command.backpack.edit.other")) {
                        fakeSlotChangeEvent.setCancelled();
                        return;
                    }
                    if (!playerInventoryMap.containsKey(openOtherInventory.get(p))) {
                        DataManger.saveInventory(openOtherInventory.get(p), fakeSlotChangeEvent.getInventory().slots);
                        return;
                    }
                    playerInventoryMap.get(openOtherInventory.get(p)).setItem(slot, itemAfter, true);
                }

                if (openOtherInventory.containsValue(p)) {
                    for (Player watchers : openOtherInventory.keySet()) {
                        IPlayer target = openOtherInventory.get(watchers);
                        if (p == target) {
                            playerInventoryMap.get(watchers).setItem(slot, itemAfter, true);
                        }
                    }
                }
            }
        });

        p.addWindow(hi);
        playerInventoryMap.put(p, hi);
        return true;
    }

    @EventHandler
    public void onCloseWindow(InventoryCloseEvent event) {
        Player p = event.getPlayer();

        if (!playerInventoryMap.containsKey(p)) {
            return;
        }

        HopperInventory hi = playerInventoryMap.get(p);

        if (!openOtherInventory.containsKey(p)) {
            DataManger.saveInventory(p, hi.slots);
        } else {
            IPlayer iP = openOtherInventory.get(p);
            DataManger.saveInventory(iP, hi.slots);
            openOtherInventory.remove(p, iP);
        }
        playerInventoryMap.remove(p, hi);
        hi.close(p);
    }
}

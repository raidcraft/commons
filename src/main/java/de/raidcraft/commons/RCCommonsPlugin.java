package de.raidcraft.commons;

import de.raidcraft.api.BasePlugin;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Plugin for testing various stuff and creating proof of concepts.
 */
public class RCCommonsPlugin extends BasePlugin implements Listener {

    @Override
    public void enable() {
        registerEvents(this);
    }

    @Override
    public void disable() {

    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Inventory inventory = Bukkit.createInventory(event.getPlayer(), InventoryType.CHEST, CustomItemUtil.encodeItemId(1) + "Test Kiste");
        inventory.setItem(22, new ItemStack(Material.BONE));
        event.getPlayer().openInventory(inventory);
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        try {
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            String name = inventory.getName();
            int id = CustomItemUtil.decodeItemId(name);
            event.getWhoClicked().sendMessage("Clicked in inventory " + name + " (ID: " + id + ") - Slot: " + event.getSlot());
        } catch (CustomItemException e) {
            e.printStackTrace();
        }
    }
}

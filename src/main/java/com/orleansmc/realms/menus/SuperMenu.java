package com.orleansmc.realms.menus;

import com.orleansmc.realms.OrleansRealms;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class SuperMenu {
    public final OrleansRealms plugin;
    public SuperMenu(OrleansRealms plugin) {
        this.plugin = plugin;
    }

    public Component title(String text) {
        return plugin.getComponent("<color:#dee2e6>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public String titleLegacy(String text) {
        return "§r" + LegacyComponentSerializer.legacySection().serialize(title(text));
    }

    public Component description(String text) {
        return plugin.getComponent("<color:#adb5bd>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public Component green(String text) {
        return plugin.getComponent("<color:#51cf66>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public String greenLegacy(String text) {
        return "§r" + LegacyComponentSerializer.legacySection().serialize(green(text));
    }

    public Component red(String text) {
        return plugin.getComponent("<color:#f5365c>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public String redLegacy(String text) {
        return "§r" + LegacyComponentSerializer.legacySection().serialize(red(text));
    }

    public Component yellow(String text) {
        return plugin.getComponent("<color:#ffd600>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public Component white(String text) {
        return plugin.getComponent("<color:#ffffff>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public Component blue(String text) {
        return plugin.getComponent("<color:#5e72e4>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    public Component purple(String text) {
        return plugin.getComponent("<color:#AE89EB>" + text + "</color>")
                .decoration(TextDecoration.ITALIC, false);
    }

    protected abstract void open();

    public ItemStack getEmptyItem(String title, List<Component> lore) {
        return getItemsAdderItem("ui:empty", title, lore);
    }

    public ItemStack getItemsAdderItem(String nameId, String title, List<Component> lore) {
        CustomStack customStack = CustomStack.getInstance(nameId);
        ItemStack itemStack = customStack.getItemStack();
        customStack.setDisplayName(title);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}

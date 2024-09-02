package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.quests.objectives.ConvertGemObjective;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConverterMenu extends SuperMenu {
    private final Player player;
    private final AtomicInteger gemAmount = new AtomicInteger(0);
    private final ChestGui gui = new ChestGui(5, getTitle());
    private final int[] gemSlotPositions = new int[]{2, 3, 4, 5, 6, 11, 12, 13, 14, 15};

    public ConverterMenu(Player player, OrleansRealms plugin) {
        super(plugin);
        this.player = player;
    }

    public void open() {
        player.closeInventory();
        CustomStack orleansGem = CustomStack.getInstance("custom_ores:orleans_gem");
        gui.setOnGlobalDrag(event -> {
            if (gemAmount.get() >= 640) {
                event.setCancelled(true);
            }

            ItemStack cursorItem = event.getCursor();

            if (cursorItem != null) {
                cursorItem = cursorItem.clone();
                cursorItem.setAmount(1);
            }

            if (cursorItem != null && cursorItem.isSimilar(orleansGem.getItemStack())) {
                if (gemAmount.get() >= 640 && !event.getInventory().equals(player.getInventory())) {
                    event.setCancelled(true);
                    return;
                }
                Bukkit.getScheduler().runTaskLater(plugin, this::updateGemAmount, 1L);
            }
        });
        gui.setOnGlobalClick(event -> {
            ItemStack cursorItem = event.getCursor();
            if (cursorItem != null) {
                cursorItem = cursorItem.clone();
                cursorItem.setAmount(1);
            }
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null) {
                clickedItem = clickedItem.clone();
                clickedItem.setAmount(1);
            }

            if (cursorItem == null || cursorItem.isEmpty()) {
                if (clickedItem != null && clickedItem.isSimilar(orleansGem.getItemStack())) {
                    Bukkit.getScheduler().runTaskLater(plugin, this::updateGemAmount, 1L);
                    return;
                }
            }

            if (clickedItem == null || clickedItem.isEmpty()) {
                if (cursorItem != null && cursorItem.isSimilar(orleansGem.getItemStack())) {
                    if (gemAmount.get() >= 640 && !event.getClickedInventory().equals(player.getInventory())) {
                        event.setCancelled(true);
                        return;
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, this::updateGemAmount, 1L);
                    return;
                }
            }

            event.setCancelled(true);
        });
        gui.setOnClose(event -> {
            for (int gemSlotPosition : gemSlotPositions) {
                ItemStack item = gui.getInventory().getItem(gemSlotPosition);
                if (item != null && !item.isEmpty()) {
                    player.getInventory().addItem(item);
                }
            }
        });
        StaticPane blankPane1 = new StaticPane(0, 0, 2, 5);
        ItemStack blankItem = getEmptyItem(" ", null);
        blankPane1.fillWith(blankItem);
        gui.addPane(blankPane1);
        StaticPane blankPane2 = new StaticPane(7, 0, 2, 5);
        blankPane2.fillWith(blankItem);
        gui.addPane(blankPane2);
        StaticPane blankPane3 = new StaticPane(2, 2, 5, 2);
        blankPane3.fillWith(blankItem);
        gui.addPane(blankPane3);
        StaticPane convertButtonPane = new StaticPane(2, 4, 5, 1);
        ItemStack convertButton = getEmptyItem(titleLegacy("Dönüştür"), null);
        convertButtonPane.fillWith(convertButton);
        convertButtonPane.setOnClick(event -> {
            event.setCancelled(true);
            updateGemAmount();
            gui.getInventory().clear();
            gui.update();
            plugin.redisEconomyAPI.getDefaultCurrency().depositPlayer(
                    player.getName(),
                    gemAmount.get(),
                    "Orleans Gem conversion"
            );
            ConvertGemObjective.instance.onPlayerGemConvert(player, gemAmount.get());
            player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0F, 1.0F);
            updateGemAmount();
        });
        gui.addPane(convertButtonPane);
        gui.show(player);
    }

    private void updateGemAmount() {
        CustomStack orleansGem = CustomStack.getInstance("custom_ores:orleans_gem");
        gemAmount.set(0);
        AtomicInteger itemStackAmount = new AtomicInteger();
        AtomicInteger lastItemAmount = new AtomicInteger();

        gui.getInventory().forEach(item -> {
            if (item != null && item.isSimilar(orleansGem.getItemStack())) {
                int amount = gemAmount.addAndGet(item.getAmount());
                if (gemAmount.get() >= 640) {
                    int excess = gemAmount.get() - 640;
                    for (int i = 0; i < excess; i++) {
                        player.getInventory().addItem(orleansGem.getItemStack());
                    }
                    gemAmount.set(640);
                    amount = 640;
                }
                itemStackAmount.set((int) Math.ceil((double) amount / 64));
                lastItemAmount.set(amount % 64);
                if (lastItemAmount.get() == 0) {
                    lastItemAmount.set(64);
                }
            }
        });

        gui.setTitle(getTitle());
        for (int gemSlotPosition : gemSlotPositions) {
            gui.getInventory().setItem(gemSlotPosition, null);
        }
        gui.update();
        for (int i = 0; i < itemStackAmount.get(); i++) {
            ItemStack gemItem = orleansGem.getItemStack().clone();
            gemItem.setAmount(64);
            if (i == itemStackAmount.get() - 1) {
                gemItem.setAmount(lastItemAmount.get());
            }
            gui.getInventory().setItem(gemSlotPositions[i], gemItem);
        }
    }

    private String getTitle() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:converter_menu");
        List<String> chars = new ArrayList<>();
        int firstCharOffset = -122 - (String.valueOf(gemAmount.get()).length() * 3);

        for (char c : String.valueOf(gemAmount.get()).toCharArray()) {
            FontImageWrapper charWrapper = new FontImageWrapper("ui:converter_digit_" + c);
            chars.add(charWrapper.applyPixelsOffset(firstCharOffset));
        }

        return "§f" + fontImageWrapper.applyPixelsOffset(-25) + String.join("", chars);
    }
}

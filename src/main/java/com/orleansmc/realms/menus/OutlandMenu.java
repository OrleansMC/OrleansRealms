package com.orleansmc.realms.menus;

import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class OutlandMenu extends SuperMenu {
    private final Player player;
    private final ChestGui gui;
    private final boolean reload;

    public OutlandMenu(Player player, OrleansRealms plugin, ChestGui gui) {
        super(plugin);
        this.player = player;
        this.reload = gui == null;
        this.gui = Objects.requireNonNullElseGet(gui, () -> new ChestGui(5, ""));
    }

    public void open() {
        FontImageWrapper fontImageWrapper = new FontImageWrapper("ui:outland_menu");
        gui.getPanes().clear();
        gui.setRows(5);
        gui.setTitle("§f" + fontImageWrapper.applyPixelsOffset(-25));
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.setOnGlobalDrag(event -> event.setCancelled(true));
        if (reload) {
            gui.show(player);
        }

        StaticPane outlandNavigator = new StaticPane(0, 1, 4, 4);
        ItemStack outlandButton = getEmptyItem(titleLegacy("Vahşi Dünya"),
                List.of(blue("Vahşi dünyaya gitmek için tıkla."))
        );
        outlandNavigator.fillWith(outlandButton);
        outlandNavigator.setOnClick(event -> {
            if (!player.hasPermission("orleansmc.outland.access")) {
                player.sendMessage(Util.getExclamation() + "Vahşi dünyaya gidebilmek için görevlerinizde ilerleyin.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            String availableServer = plugin.serversManager.getServerStates().values()
                    .stream()
                    .filter(serverState -> ServerType.valueOf(serverState.type) == ServerType.REALMS_OUTLAND)
                    .min((o1, o2) -> o2.players.size() - o1.players.size())
                    .map(serverState -> serverState.name)
                    .orElse("lobby");
            plugin.serversManager.serversProvider.switchServer(player.getName(), availableServer);
        });
        gui.addPane(outlandNavigator);

        StaticPane outlandNetherNavigator = new StaticPane(5, 1, 4, 4);
        ItemStack outlandNetherButton = getEmptyItem(titleLegacy("Çok yakında!"),
                List.of(blue("Düşmüş dünya çok yakında!"))
        );
        outlandNetherNavigator.fillWith(outlandNetherButton);
        outlandNetherNavigator.setOnClick(event -> {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1);
        });
        gui.addPane(outlandNetherNavigator);

        gui.update();
    }
}

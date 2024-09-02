package com.orleansmc.realms.commands.admins;

import com.google.common.collect.ImmutableList;
import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.utils.Util;
import dev.lone.itemsadder.api.CustomStack;
import me.lucko.helper.Commands;
import org.bukkit.entity.Player;

public class InventoryToStringsCommand {
    public static void setup(OrleansRealms plugin) {
        Commands.create()
                .assertPlayer()
                .assertOp()
                .tabHandler(c -> ImmutableList.of())
                .handler(c -> {
                    Player player = c.sender();
                    StringBuilder sb = new StringBuilder();
                    player.getInventory().forEach(item -> {
                        if (item == null) return;
                        if (Util.hasCustomTexture(item)) {
                            CustomStack customStack = CustomStack.byItemStack(item);
                            sb.append(customStack.getNamespacedID()).append("{count:").append(item.getAmount()).append("}");
                        } else {
                            sb.append("minecraft:").append(item.getType().name().toLowerCase()).append("{count:").append(item.getAmount()).append("}");
                        }
                        sb.append(" ");
                    });
                    player.sendMessage(plugin.getComponent(
                            "<click:copy_to_clipboard:'" + sb + "'>Envanterinize ait eşyaları kopyalamak için tıklayın.</click>"
                    ));
                })
                .registerAndBind(plugin, "invtostrings", "invtostr", "envanter-metinleri");
    }
}

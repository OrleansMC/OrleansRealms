package com.orleansmc.realms.managers.realm;

import com.orleansmc.realms.OrleansRealms;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.configs.texts.Texts;
import com.orleansmc.realms.models.config.TextModel;
import com.orleansmc.realms.models.temporary.PendingRealmModel;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class RealmsChunkyManager {
    RealmsRedisManager realmsRedisManager;

    public RealmsChunkyManager(RealmsRedisManager realmsRedisManager) {
        this.realmsRedisManager = realmsRedisManager;
        OrleansRealms plugin = realmsRedisManager.plugin;

        final AtomicInteger lastProgress = new AtomicInteger(-1);
        plugin.chunkyAPI.onGenerationProgress(event -> {
            if (!plugin.isEnabled()) return;
            int progress = (int) event.progress();
            if (progress == lastProgress.get()) return;
            lastProgress.set(progress);
            if (progress % 10 != 0) return;
            plugin.getLogger().info("Realm creation progress: " + event.progress());

            PendingRealmModel pendingRealmModel = realmsRedisManager.pendingRealms.values().stream()
                    .filter(pendingRealmModel1 -> pendingRealmModel1.executed)
                    .findFirst().orElse(null);

            if (pendingRealmModel == null) {
                plugin.getLogger().warning("CHUNKY API GENERATION PROGRESS BUT NO PENDING REALM CREATION");
                return;
            }
            TextModel textModel = new TextModel(
                    Texts.REALM_CREATION_PROGRESS.en,
                    Texts.REALM_CREATION_PROGRESS.tr
            );
            textModel.addReplacement("{progress}", String.valueOf(progress));
            plugin.realmsManager.messageManager.sendMessage(
                    pendingRealmModel.owner, pendingRealmModel.type == PendingRealmModel.PendingType.CREATE ? textModel :
                            new TextModel(
                                    "<color:#00ff00>Realm update progress: </color> %" + progress,
                                    "<color:#00ff00>Diyar güncelleme ilerlemesi: </color> %" + progress
                            )
            );
        });

        plugin.chunkyAPI.onGenerationComplete(event -> Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            if (!plugin.isEnabled()) return;
            lastProgress.set(-1);
            PendingRealmModel pendingRealmModel = getExecutedPendingRealmModel();
            if (pendingRealmModel == null) {
                plugin.getLogger().warning("CHUNKY API GENERATION COMPLETED BUT NO PENDING REALM CREATION");
                return;
            }
            plugin.getLogger().info("Realm creation completed.");
            realmsRedisManager.handleGeneratedRealm(pendingRealmModel.owner);
        }, 20 * 4));
    }

    public PendingRealmModel getExecutedPendingRealmModel() {
        return realmsRedisManager.pendingRealms.values().stream()
                .filter(pendingRealmModel -> pendingRealmModel.executed && Objects.equals(pendingRealmModel.server, Settings.SERVER_NAME))
                .findFirst().orElse(null);
    }
}

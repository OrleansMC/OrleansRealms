package com.orleansmc.realms;

import com.github.yannicklamprecht.worldborder.api.WorldBorderApi;
import com.orleansmc.common.servers.ServerType;
import com.orleansmc.realms.commands.CommandLoader;
import com.orleansmc.realms.commands.players.RealmCommand;
import com.orleansmc.realms.configs.ConfigLoader;
import com.orleansmc.realms.configs.settings.Settings;
import com.orleansmc.realms.listeners.ListenerLoader;
import com.orleansmc.realms.managers.server.GameRulesManager;
import com.orleansmc.realms.managers.common.LuckPermsManager;
import com.orleansmc.realms.managers.player.PlayerDataManager;
import com.orleansmc.realms.managers.realm.RealmsManager;
import com.orleansmc.realms.managers.realm.RegionManager;
import com.orleansmc.realms.managers.server.*;
import com.orleansmc.realms.placeholders.RealmsExpansion;
import com.orleansmc.realms.quests.objectives.ObjectiveLoader;
import dev.unnm3d.rediseconomy.api.RedisEconomyAPI;
import dev.unnm3d.rediseconomy.currency.Currency;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.william278.huskhomes.api.HuskHomesAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.popcraft.chunky.api.ChunkyAPI;

import java.util.Objects;

public final class OrleansRealms extends ExtendedJavaPlugin {
    public RealmsManager realmsManager;
    public RedstoneManager redstoneManager;
    public ServersManager serversManager;
    public PlayerDataManager playerDataManager;
    public EntityLagManager entityLagManager;
    public OutlandManager outlandManager;
    public QuestManager questManager;
    public HuskHomesAPI huskHomesAPI;
    public ChunkyAPI chunkyAPI;
    public WorldBorderApi worldBorderApi;
    public RedisEconomyAPI redisEconomyAPI;
    public LeaderboardManager leaderboardManager;

    @Override
    protected void enable() {
        this.getLogger().info("OrleansRealms is starting...");
        this.reloadSettings();
        this.huskHomesAPI = HuskHomesAPI.getInstance();
        chunkyAPI = Bukkit.getServer().getServicesManager().load(ChunkyAPI.class);
        if (chunkyAPI == null) {
            throw new RuntimeException("Chunky service not found");
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("orleansrealms.admin")) {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                for (int i = 0; i < 5; i++) {
                    player.sendMessage(
                            getComponent(
                                    "<gray>[<yellow>OrleansRealms</yellow>]:</gray> <red>Bu bir yeniden başlatmaysa, lütfen sunucuyu yeniden başlatın.</red>"
                            ));
                }
            }
        }

        RegisteredServiceProvider<WorldBorderApi> worldBorderApiRegisteredServiceProvider = getServer().getServicesManager().getRegistration(WorldBorderApi.class);

        if (worldBorderApiRegisteredServiceProvider == null) {
            throw new RuntimeException("WorldBorder service not found");
        }

        worldBorderApi = worldBorderApiRegisteredServiceProvider.getProvider();
        redisEconomyAPI = RedisEconomyAPI.getAPI();
        if (redisEconomyAPI == null) {
            throw new RuntimeException("RedisEconomyAPI service not found");
        }
        this.serversManager = new ServersManager(this);
        this.playerDataManager = new PlayerDataManager(this);
        this.realmsManager = new RealmsManager(this);
        this.redstoneManager = new RedstoneManager(this);
        this.entityLagManager = new EntityLagManager(this);
        this.questManager = new QuestManager(this);
        this.outlandManager = new OutlandManager(this);

        if (this.serversManager.getCurrentServerType() == ServerType.REALMS_SPAWN) {
            this.leaderboardManager = new LeaderboardManager(this);
        }

        CommandLoader.load(this);
        ListenerLoader.load(this);
        ObjectiveLoader.load();

        RealmsExpansion realmsExpansion = new RealmsExpansion(this);
        if (realmsExpansion.isRegistered()) {
            realmsExpansion.unregister();
        }
        realmsExpansion.register();

        GameRulesManager.setup(this);
        LuckPermsManager.setup(this);
    }

    @Override
    protected void disable() {
        this.getLogger().info("OrleansRealms is stopping...");
        if (this.realmsManager != null) {
            this.realmsManager.realmsRedisManager.realmStateChannelAgent.close();
            this.realmsManager.realmsRedisManager.pendingRealmsChannelAgent.close();
            this.realmsManager.messageManager.channelAgent.close();
            this.realmsManager.realms.clear();
            RegionManager.removedRegions.clear();
        }
        if (RealmCommand.channelAgent != null) {
            RealmCommand.channelAgent.close();
        }

        if (this.redstoneManager != null) {
            this.redstoneManager.clearRedstoneActivity();
        }
    }

    public void reloadSettings() {
        ConfigLoader.load(this);
    }

    public Currency getCreditCurrency() {
        return Objects.requireNonNull(redisEconomyAPI.getCurrencyByName("CREDIT"));
    }

    public Currency getGemCurrency() {
        return Objects.requireNonNull(redisEconomyAPI.getCurrencyByName("GEM"));
    }

    public Component getComponent(String text) {
        final MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize(text);
    }

    public String getString(Component component) {
        final MiniMessage mm = MiniMessage.miniMessage();
        return mm.serialize(component);
    }
}

package com.nylofreezer;

import com.google.inject.Inject;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
    name = "Nylo Freezer",
    description = "Calculate the minimum Magic attack bonus needed to freeze Maiden nylocas at Theatre of Blood",
    tags = {"tob", "theatre of blood", "maiden", "nylo", "freeze", "magic", "calculator"}
)
public class NyloFreezerPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ItemManager itemManager;

    @Inject
    private NyloFreezerPanel panel;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private LiveFreezeOverlay liveOverlay;

    @Inject
    private NyloFreezerConfig config;

    private NavigationButton navButton;
    private Integer currentMagicAttack;
    private boolean useVoid;
    private boolean iceSceptre;
    private volatile boolean running;

    @Provides
    NyloFreezerConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(NyloFreezerConfig.class);
    }

    @Override
    protected void startUp()
    {
        running = true;
        navButton = NavigationButton.builder()
            .tooltip("Nylo Freezer")
            .icon(createSidebarIcon())
            .priority(7)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
        overlayManager.add(liveOverlay);
        clientThread.invoke(() ->
        {
            if (running)
            {
                syncPlayerStateFromClient();
            }
        });
    }

    @Override
    protected void shutDown()
    {
        running = false;
        overlayManager.remove(liveOverlay);
        liveOverlay.clear();
        currentMagicAttack = null;
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }
        navButton = null;
        setPanelCurrentMagicAttackBonus(null);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            syncPlayerStateFromClient();
        }
        else
        {
            currentMagicAttack = null;
            liveOverlay.clear();
            setPanelCurrentMagicAttackBonus(null);
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (event.getSkill() == Skill.MAGIC)
        {
            // getLevel() is the real/static level. The panel clamps anything below 82 to 82.
            setPanelMagicLevel(event.getLevel());
            updateLiveOverlay();
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() == InventoryID.WORN)
        {
            syncCurrentMagicAttackBonus();
            updateLiveOverlay();
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        // Equipment may not be available when LOGGED_IN fires. Retry until it is loaded,
        // without requiring the player to change gear after login or a world hop.
        if (running && currentMagicAttack == null && client.getGameState() == GameState.LOGGED_IN)
        {
            syncPlayerStateFromClient();
            return;
        }

        // Read active prayers and boosted Magic once per tick, never in the per-frame renderer.
        updateLiveOverlay();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if (NyloFreezerConfig.GROUP.equals(event.getGroup()))
        {
            clientThread.invoke(this::updateLiveOverlay);
        }
    }

    private void updateLiveOverlay()
    {
        if (!running || !config.liveFreezeOverlay() || client.getGameState() != GameState.LOGGED_IN
            || currentMagicAttack == null)
        {
            liveOverlay.clear();
            return;
        }
        liveOverlay.update(client.getRealSkillLevel(Skill.MAGIC), client.getBoostedSkillLevel(Skill.MAGIC),
            currentMagicAttack, currentPrayer(), useVoid, iceSceptre);
    }

    private FreezeCalculator.Prayer currentPrayer()
    {
        if (client.isPrayerActive(net.runelite.api.Prayer.AUGURY))
        {
            return FreezeCalculator.Prayer.AUGURY;
        }
        if (client.isPrayerActive(net.runelite.api.Prayer.MYSTIC_VIGOUR))
        {
            return FreezeCalculator.Prayer.MYSTIC_VIGOUR;
        }
        if (client.isPrayerActive(net.runelite.api.Prayer.MYSTIC_MIGHT))
        {
            return FreezeCalculator.Prayer.MYSTIC_MIGHT;
        }
        if (client.isPrayerActive(net.runelite.api.Prayer.MYSTIC_LORE))
        {
            return FreezeCalculator.Prayer.MYSTIC_LORE;
        }
        if (client.isPrayerActive(net.runelite.api.Prayer.MYSTIC_WILL))
        {
            return FreezeCalculator.Prayer.MYSTIC_WILL;
        }
        return FreezeCalculator.Prayer.NONE;
    }

    private void syncPlayerStateFromClient()
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            setPanelMagicLevel(client.getRealSkillLevel(Skill.MAGIC));
            syncCurrentMagicAttackBonus();
            updateLiveOverlay();
        }
    }

    private void syncCurrentMagicAttackBonus()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.WORN);
        if (equipment == null)
        {
            currentMagicAttack = null;
            setPanelCurrentMagicAttackBonus(null);
            return;
        }

        int magicAttack = 0;
        for (Item item : equipment.getItems())
        {
            if (item == null || item.getId() <= 0)
            {
                continue;
            }

            ItemStats itemStats = itemManager.getItemStats(item.getId());
            if (itemStats == null)
            {
                continue;
            }

            ItemEquipmentStats equipmentStats = itemStats.getEquipment();
            if (equipmentStats != null)
            {
                magicAttack += equipmentStats.getAmagic();
            }
        }

        currentMagicAttack = magicAttack;
        useVoid = FreezerEquipment.hasVoidMage(equipment);
        iceSceptre = FreezerEquipment.hasIceSceptre(equipment);
        setPanelCurrentMagicAttackBonus(magicAttack);
    }

    private void setPanelMagicLevel(int level)
    {
        SwingUtilities.invokeLater(() -> panel.setMagicLevelFromClient(level));
    }

    private void setPanelCurrentMagicAttackBonus(Integer bonus)
    {
        SwingUtilities.invokeLater(() -> panel.setCurrentMagicAttackBonus(bonus));
    }

    private static BufferedImage createSidebarIcon()
    {
        return ImageUtil.loadImageResource(NyloFreezerPlugin.class, "nylo_freezer_icon.png");
    }

}

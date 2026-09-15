package com.nylofreezer;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.NavigationButton;

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

    private NavigationButton navButton;

    @Override
    protected void startUp()
    {
        navButton = NavigationButton.builder()
            .tooltip("Nylo Freezer")
            .icon(createSidebarIcon())
            .priority(7)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
        syncPlayerStateFromClient();
    }

    @Override
    protected void shutDown()
    {
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
        else if (event.getGameState() == GameState.LOGIN_SCREEN)
        {
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
        }
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event)
    {
        if (event.getContainerId() == InventoryID.EQUIPMENT.getId())
        {
            syncCurrentMagicAttackBonus();
        }
    }

    private void syncPlayerStateFromClient()
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            setPanelMagicLevel(client.getRealSkillLevel(Skill.MAGIC));
            syncCurrentMagicAttackBonus();
        }
    }

    private void syncCurrentMagicAttackBonus()
    {
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipment == null)
        {
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
        final int size = 24;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(2.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(ColorScheme.BRAND_ORANGE);

            int cx = size / 2;
            int cy = size / 2;
            int r = 8;

            for (int i = 0; i < 3; i++)
            {
                double angle = Math.toRadians(i * 60.0);
                int dx = (int) Math.round(Math.cos(angle) * r);
                int dy = (int) Math.round(Math.sin(angle) * r);
                g.drawLine(cx - dx, cy - dy, cx + dx, cy + dy);
            }
        }
        finally
        {
            g.dispose();
        }

        return image;
    }
}

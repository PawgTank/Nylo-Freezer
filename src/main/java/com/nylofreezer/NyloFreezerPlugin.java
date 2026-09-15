package com.nylofreezer;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
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
        syncMagicLevelFromClient();
    }

    @Override
    protected void shutDown()
    {
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }
        navButton = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGGED_IN)
        {
            syncMagicLevelFromClient();
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged event)
    {
        if (event.getSkill() == Skill.MAGIC)
        {
            // getLevel() is the real/static level. Do not use getBoostedLevel() here.
            setPanelMagicLevel(event.getLevel());
        }
    }

    private void syncMagicLevelFromClient()
    {
        if (client.getGameState() == GameState.LOGGED_IN)
        {
            setPanelMagicLevel(client.getRealSkillLevel(Skill.MAGIC));
        }
    }

    private void setPanelMagicLevel(int level)
    {
        SwingUtilities.invokeLater(() -> panel.setMagicLevelFromClient(level));
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

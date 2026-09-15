package com.nylofreezer;

import com.google.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@PluginDescriptor(
    name = "Nylo Freezer",
    description = "Calculate the minimum Magic attack bonus needed to freeze Maiden nylocas at Theatre of Blood",
    tags = {"tob", "theatre of blood", "maiden", "nylo", "freeze", "magic", "calculator"}
)
public class NyloFreezerPlugin extends Plugin
{
    @Inject
    private ClientToolbar clientToolbar;

    private NyloFreezerPanel panel;
    private NavigationButton navButton;

    @Override
    protected void startUp()
    {
        panel = new NyloFreezerPanel();

        navButton = NavigationButton.builder()
            .tooltip("Nylo Freezer")
            .icon(createSidebarIcon())
            .priority(7)
            .panel(panel)
            .build();

        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown()
    {
        if (navButton != null)
        {
            clientToolbar.removeNavigation(navButton);
        }

        navButton = null;
        panel = null;
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
            g.setColor(new Color(204, 62, 82));

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

            g.setColor(new Color(236, 211, 178));
            g.fillOval(cx - 2, cy - 2, 4, 4);
        }
        finally
        {
            g.dispose();
        }

        return image;
    }
}

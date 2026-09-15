package com.nylofreezer;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Locale;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

@Singleton
class LiveFreezeOverlay extends OverlayPanel
{
    private final NyloFreezerConfig config;
    private String label;
    private String value;
    private Color color;

    @Inject
    LiveFreezeOverlay(NyloFreezerPlugin plugin, NyloFreezerConfig config)
    {
        super(plugin);
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        panelComponent.setPreferredSize(new Dimension(230, 0));
    }

    void update(int baseLevel, int visibleLevel, int magicAttack,
        FreezeCalculator.Prayer prayer, boolean useVoid, boolean iceSceptre)
    {
        double chance = FreezeCalculator.calculateFreezeChance(
            baseLevel, visibleLevel, magicAttack, prayer, useVoid, iceSceptre);
        if (config.showFreezeChance())
        {
            label = "Freeze chance";
            value = formatChance(chance);
            color = chanceColor(chance);
            return;
        }

        int additional = Math.max(0, FreezeCalculator.calculateLiveRequiredAttack(
            baseLevel, visibleLevel, prayer, useVoid, iceSceptre) - magicAttack);
        if (additional > 0)
        {
            label = "Additional required bonus";
            value = "+" + additional;
            color = chanceColor(chance);
        }
        else
        {
            label = "Magic levels to spare";
            value = Integer.toString(FreezeCalculator.calculateLevelsToSpare(
                baseLevel, visibleLevel, magicAttack, prayer, useVoid, iceSceptre));
            color = Color.GREEN;
        }
    }

    static String formatChance(double chance)
    {
        // Truncate so 99.99% never displays as a guaranteed freeze.
        return chance >= 1 ? "100%" : String.format(Locale.ROOT, "%.1f%%", Math.floor(chance * 1000) / 10);
    }

    static Color chanceColor(double chance)
    {
        return chance >= 1 ? Color.GREEN : chance >= 0.95 ? Color.YELLOW : Color.RED;
    }

    void clear()
    {
        value = null;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.liveFreezeOverlay() || value == null)
        {
            return null;
        }

        panelComponent.getChildren().add(TitleComponent.builder().text("Nylo Freezer").build());
        panelComponent.getChildren().add(LineComponent.builder()
            .left(label)
            .right(value)
            .rightColor(color)
            .build());
        return super.render(graphics);
    }
}

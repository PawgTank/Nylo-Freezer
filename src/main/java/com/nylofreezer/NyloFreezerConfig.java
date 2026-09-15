package com.nylofreezer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(NyloFreezerConfig.GROUP)
public interface NyloFreezerConfig extends Config
{
    String GROUP = "nylo-freezer";

    @ConfigItem(
        keyName = "liveFreezeOverlay",
        name = "Live freeze overlay",
        description = "Show additional Magic attack bonus needed for 100% Maiden nylo freeze accuracy, "
            + "or Magic levels to spare, using current stats, equipment and prayer. "
            + "Levels to spare describe accuracy only, not ice spell level requirements.",
        position = 0
    )
    default boolean liveFreezeOverlay()
    {
        return false;
    }

    @ConfigItem(
        keyName = "showFreezeChance",
        name = "Show freeze chance",
        description = "Show only freeze accuracy in the live overlay. Green: 100%; yellow: 95% to below 100%; "
            + "red: below 95%. Assumes you can cast an ice spell; does not check runes or spell requirements.",
        position = 1
    )
    default boolean showFreezeChance()
    {
        return false;
    }
}

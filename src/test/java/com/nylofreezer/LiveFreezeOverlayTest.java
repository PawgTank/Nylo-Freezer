package com.nylofreezer;

import java.awt.Color;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LiveFreezeOverlayTest
{
    @Test
    public void colorsUseExactAccuracyBoundaries()
    {
        assertEquals(Color.GREEN, LiveFreezeOverlay.chanceColor(1));
        assertEquals(Color.YELLOW, LiveFreezeOverlay.chanceColor(0.99999));
        assertEquals(Color.YELLOW, LiveFreezeOverlay.chanceColor(0.95));
        assertEquals(Color.RED, LiveFreezeOverlay.chanceColor(0.94999));
        assertEquals(Color.RED, LiveFreezeOverlay.chanceColor(0));
    }

    @Test
    public void percentageNeverRoundsUpAcrossAColorBoundary()
    {
        assertEquals("100%", LiveFreezeOverlay.formatChance(1));
        assertEquals("99.9%", LiveFreezeOverlay.formatChance(0.99999));
        assertEquals("95.0%", LiveFreezeOverlay.formatChance(0.95));
        assertEquals("94.9%", LiveFreezeOverlay.formatChance(0.94999));
        assertEquals("0.0%", LiveFreezeOverlay.formatChance(0));
    }
}

package com.nylofreezer;

import org.junit.Test;

import static com.nylofreezer.FreezeCalculator.Prayer.AUGURY;
import static com.nylofreezer.FreezeCalculator.Prayer.NONE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FreezeCalculatorTest
{
    @Test
    public void unboostedAccuracyUsesMaidenInterpolation()
    {
        assertEquals(0, chance(99, 99, 0), 0);
        assertEquals(0.5, chance(99, 99, 70), 0);
        assertEquals(0.95, chance(99, 99, 133), 0);
        assertEquals(1, chance(99, 99, 140), 0);
        assertEquals(0, chance(99, 99, -100), 0);
        assertEquals(1, chance(99, 99, 200), 0);
    }

    @Test
    public void liveBoostUsesVisibleLevelAndKeepsBaseThresholdFixed()
    {
        assertEquals(140, FreezeCalculator.calculateLiveRequiredAttack(99, 99, NONE, false, false));
        assertEquals(119, FreezeCalculator.calculateLiveRequiredAttack(99, 112, NONE, false, false));
        assertEquals(71, FreezeCalculator.calculateLiveRequiredAttack(99, 112, AUGURY, false, true));
        assertTrue(chance(99, 98, 140) < 1);
    }

    @Test
    public void liveAndSidebarThresholdsAgreeForTheSameStats()
    {
        for (int base = 82; base <= 99; base++)
        {
            for (FreezeCalculator.Boost boost : FreezeCalculator.Boost.values())
            {
                int visible;
                switch (boost)
                {
                    case SATURATED_HEART: visible = (int) (base * 1.1 + 4); break;
                    case IMBUED_HEART: visible = base + 1 + (int) (base * 0.10); break;
                    case FORGOTTEN_BREW: visible = base + 3 + (int) (base * 0.08); break;
                    case ANCIENT_BREW: visible = base + 2 + (int) (base * 0.05); break;
                    case MAGIC_POTION: visible = base + 4; break;
                    default: visible = base; break;
                }
                for (FreezeCalculator.Prayer prayer : FreezeCalculator.Prayer.values())
                {
                    for (boolean useVoid : new boolean[]{false, true})
                    {
                        for (boolean sceptre : new boolean[]{false, true})
                        {
                            assertEquals(FreezeCalculator.calculateRequiredAttack(base, boost, prayer, useVoid, sceptre),
                                FreezeCalculator.calculateLiveRequiredAttack(base, visible, prayer, useVoid, sceptre));
                        }
                    }
                }
            }
        }
    }

    @Test
    public void requiredBonusIsTheFirstGuaranteedFreezeAcrossPrayerAndGearCombinations()
    {
        for (int base = 82; base <= 99; base++)
        {
            for (int visible = 0; visible <= 125; visible++)
            {
                for (FreezeCalculator.Prayer prayer : FreezeCalculator.Prayer.values())
                {
                    for (boolean useVoid : new boolean[]{false, true})
                    {
                        for (boolean sceptre : new boolean[]{false, true})
                        {
                            int required = FreezeCalculator.calculateLiveRequiredAttack(base, visible, prayer, useVoid, sceptre);
                            assertEquals(1, FreezeCalculator.calculateFreezeChance(base, visible, required, prayer, useVoid, sceptre), 0);
                            assertTrue(FreezeCalculator.calculateFreezeChance(base, visible, required - 1, prayer, useVoid, sceptre) < 1);
                        }
                    }
                }
            }
        }
    }

    @Test
    public void levelsToSpareIncludesLastLevelThatStillMeetsTarget()
    {
        assertEquals(13, FreezeCalculator.calculateLevelsToSpare(99, 112, 140, NONE, false, false));
        assertEquals(0, FreezeCalculator.calculateLevelsToSpare(99, 99, 140, NONE, false, false));
        assertEquals(0, FreezeCalculator.calculateLevelsToSpare(99, 112, 119, NONE, false, false));
        assertEquals(13, FreezeCalculator.calculateLevelsToSpare(99, 112, 103, AUGURY, false, false));
    }

    private static double chance(int base, int visible, int bonus)
    {
        return FreezeCalculator.calculateFreezeChance(base, visible, bonus, NONE, false, false);
    }
}

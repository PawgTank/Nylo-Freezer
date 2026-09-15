package com.nylofreezer;

final class FreezeCalculator
{
    enum Boost
    {
        SATURATED_HEART("Saturated Heart"),
        IMBUED_HEART("Imbued Heart"),
        FORGOTTEN_BREW("Forgotten Brew"),
        ANCIENT_BREW("Ancient Brew"),
        MAGIC_POTION("Magic Potion"),
        NONE("No Boost");

        private final String label;

        Boost(String label)
        {
            this.label = label;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    enum Prayer
    {
        AUGURY("Augury", 1.25),
        MYSTIC_VIGOUR("Mystic Vigour", 1.18),
        MYSTIC_MIGHT("Mystic Might", 1.15),
        MYSTIC_LORE("Mystic Lore", 1.10),
        MYSTIC_WILL("Mystic Will", 1.05),
        NONE("No Prayer", 1.00);

        private final String label;
        private final double multiplier;

        Prayer(String label, double multiplier)
        {
            this.label = label;
            this.multiplier = multiplier;
        }

        double getMultiplier()
        {
            return multiplier;
        }

        @Override
        public String toString()
        {
            return label;
        }
    }

    private FreezeCalculator()
    {
    }

    /**
     * This intentionally mirrors the original Nylo Freezer JavaScript operation-for-operation.
     * The extra boosts are inserted using the formulas supplied for them, without changing the
     * original target/effective-level calculation order.
     */
    static int calculateRequiredAttack(
        int magicLevel,
        Boost boost,
        Prayer prayer,
        boolean useVoid,
        boolean iceSceptre)
    {
        final int toBeat = (magicLevel + 9) * 204;
        final double visual;

        switch (boost)
        {
            case SATURATED_HEART:
                // Original calculator: (staticLevel * 1.1) + 4, then floor the visual level.
                visual = (magicLevel * 1.1) + 4;
                break;
            case IMBUED_HEART:
                visual = magicLevel + 1 + Math.floor(magicLevel * 0.10);
                break;
            case FORGOTTEN_BREW:
                visual = magicLevel + 3 + Math.floor(magicLevel * 0.08);
                break;
            case ANCIENT_BREW:
                visual = magicLevel + 2 + Math.floor(magicLevel * 0.05);
                break;
            case MAGIC_POTION:
                visual = magicLevel + 4;
                break;
            case NONE:
            default:
                visual = magicLevel;
                break;
        }

        int effective = calculateEffectiveLevel((int) Math.floor(visual), prayer, useVoid);

        if (iceSceptre)
        {
            return (int) Math.ceil(((toBeat / 1.1) / effective) - 64);
        }

        return (int) Math.ceil((toBeat / (double) effective) - 64);
    }

    private static int calculateEffectiveLevel(int visibleLevel, Prayer prayer, boolean useVoid)
    {
        int effective = (int) Math.floor(visibleLevel * prayer.getMultiplier());

        if (useVoid)
        {
            effective = (int) Math.floor(effective * 1.45);
        }

        // Original calculator adds 9 after prayer and Void.
        return effective + 9;
    }

    static int calculateLiveRequiredAttack(
        int baseLevel, int visibleLevel, Prayer prayer, boolean useVoid, boolean iceSceptre)
    {
        double targetRoll = (baseLevel + 9) * 204.0;
        double effective = calculateEffectiveLevel(visibleLevel, prayer, useVoid);
        return (int) Math.ceil(targetRoll / (iceSceptre ? 1.1 : 1.0) / effective - 64);
    }

    /**
     * Maiden ice-spell accuracy interpolates between the unboosted rolls at +0 and +140.
     * https://oldschool.runescape.wiki/w/User:Mc/Mechanics/ToB
     * Keep the base level fixed when evaluating boosts and drains.
     */
    static double calculateFreezeChance(
        int baseLevel, int visibleLevel, int magicAttack, Prayer prayer, boolean useVoid, boolean iceSceptre)
    {
        double roll = calculateEffectiveLevel(visibleLevel, prayer, useVoid) * (magicAttack + 64.0);
        if (iceSceptre)
        {
            roll *= 1.1;
        }
        double minimumRoll = (baseLevel + 9) * 64.0;
        double rollRange = (baseLevel + 9) * 140.0;
        return Math.max(0, Math.min(1, (roll - minimumRoll) / rollRange));
    }

    /** Number of levels that can be lost while still meeting the 100% accuracy threshold. */
    static int calculateLevelsToSpare(
        int baseLevel, int visibleLevel, int magicAttack, Prayer prayer, boolean useVoid, boolean iceSceptre)
    {
        int minimumLevel = visibleLevel;
        while (minimumLevel > 0 && magicAttack >= calculateLiveRequiredAttack(
            baseLevel, minimumLevel - 1, prayer, useVoid, iceSceptre))
        {
            minimumLevel--;
        }
        return visibleLevel - minimumLevel;
    }
}

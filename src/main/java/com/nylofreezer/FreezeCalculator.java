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

        int effective = (int) Math.floor(visual);
        effective = (int) Math.floor(effective * prayer.getMultiplier());

        if (useVoid)
        {
            effective = (int) Math.floor(effective * 1.45);
        }

        // Original calculator adds 9 after prayer and Void.
        effective += 9;

        if (iceSceptre)
        {
            return (int) Math.ceil(((toBeat / 1.1) / effective) - 64);
        }

        return (int) Math.ceil((toBeat / (double) effective) - 64);
    }
}

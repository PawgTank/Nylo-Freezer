package com.nylofreezer;

final class FreezeCalculator
{
    enum Boost
    {
        SATURATED_HEART("Saturated Heart", "10% + 4"),
        IMBUED_HEART("Imbued Heart", "10% + 1"),
        FORGOTTEN_BREW("Forgotten Brew", "8% + 3"),
        ANCIENT_BREW("Ancient Brew", "5% + 2"),
        MAGIC_POTION("Magic Potion", "+4"),
        NONE("No Boost", "—");

        private final String label;
        private final String formula;

        Boost(String label, String formula)
        {
            this.label = label;
            this.formula = formula;
        }

        String getLabel()
        {
            return label;
        }

        String getFormula()
        {
            return formula;
        }
    }

    enum Prayer
    {
        AUGURY("Augury", "25%", 1.25),
        MYSTIC_VIGOUR("Mystic Vigour", "18%", 1.18),
        MYSTIC_MIGHT("Mystic Might", "15%", 1.15),
        NONE("No Prayer", "—", 1.00);

        private final String label;
        private final String formula;
        private final double multiplier;

        Prayer(String label, String formula, double multiplier)
        {
            this.label = label;
            this.formula = formula;
            this.multiplier = multiplier;
        }

        String getLabel()
        {
            return label;
        }

        String getFormula()
        {
            return formula;
        }

        double getMultiplier()
        {
            return multiplier;
        }
    }

    static final class Result
    {
        private final int drain;
        private final int boostedLevel;
        private final int effectiveMagic;
        private final int requiredAttack;

        Result(int drain, int boostedLevel, int effectiveMagic, int requiredAttack)
        {
            this.drain = drain;
            this.boostedLevel = boostedLevel;
            this.effectiveMagic = effectiveMagic;
            this.requiredAttack = requiredAttack;
        }

        int getDrain()
        {
            return drain;
        }

        int getBoostedLevel()
        {
            return boostedLevel;
        }

        int getEffectiveMagic()
        {
            return effectiveMagic;
        }

        int getRequiredAttack()
        {
            return requiredAttack;
        }
    }

    private FreezeCalculator()
    {
    }

    static Result calculate(
        int magicLevel,
        Boost boost,
        Prayer prayer,
        boolean voidMage,
        boolean iceSceptre,
        int drain)
    {
        int boostAmount = calculateBoostAmount(magicLevel, boost);
        int boostedLevel = magicLevel + boostAmount - drain;
        int effectiveMagic = (int) Math.floor(boostedLevel * prayer.getMultiplier());

        if (voidMage)
        {
            effectiveMagic = (int) Math.floor(effectiveMagic * 1.45);
        }

        // Preserve the original Nylo Freezer formula: +9 is applied after prayer and Void.
        effectiveMagic += 9;

        int targetScore = (magicLevel + 9) * 204;
        double sceptreMultiplier = iceSceptre ? 1.1 : 1.0;
        int requiredAttack = (int) Math.ceil((targetScore / sceptreMultiplier / effectiveMagic) - 64);

        return new Result(drain, boostedLevel, effectiveMagic, requiredAttack);
    }

    static int calculateBoostAmount(int magicLevel, Boost boost)
    {
        switch (boost)
        {
            case SATURATED_HEART:
                return 4 + (int) Math.floor(magicLevel * 0.10);
            case IMBUED_HEART:
                return 1 + (int) Math.floor(magicLevel * 0.10);
            case FORGOTTEN_BREW:
                return 3 + (int) Math.floor(magicLevel * 0.08);
            case ANCIENT_BREW:
                return 2 + (int) Math.floor(magicLevel * 0.05);
            case MAGIC_POTION:
                return 4;
            case NONE:
            default:
                return 0;
        }
    }
}

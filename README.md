# Nylo Freezer — RuneLite plugin

A native RuneLite sidebar calculator for the minimum Magic attack bonus needed to freeze nylocas at Maiden in Theatre of Blood.

## Features

- Void Mage and Ice Sceptre toggles
- Saturated Heart, Imbued Heart, Forgotten Brew, Ancient Brew, Magic Potion, and no-boost options
- Augury, Mystic Vigour, Mystic Might, and no-prayer options
- Boost decay / stat drain
- Optional live freeze overlay in the plugin's settings
- No network access and no third-party dependencies

## Live freeze overlay

Enable **Live freeze overlay** in RuneLite's **Nylo Freezer settings**. The movable overlay uses your
current visible Magic level (including boosts and drains), equipped Magic attack bonus, and active
Magic accuracy prayer. It detects a complete Void mage set (including elite, ornamented, and locked
variants) and an equipped ice ancient sceptre automatically. Sidebar selections do not affect it.

- Below 100% accuracy: shows **Additional required bonus** to reach 100%.
- At 100% accuracy: shows **Magic levels to spare**, the number of visible levels you can lose while
  retaining 100% accuracy with the same gear and prayer. Zero means the next level lost drops below target.
- Enable **Show freeze chance** to replace those values with a percentage: green at 100%, yellow from
  95% to below 100%, and red below 95%. Percentages below 100% are truncated to one decimal place.

The overlay updates every game tick while logged in, including outside Theatre of Blood, and hides
when logged out or disabled. Its target is always a Maiden Nylocas Matomenos. Accuracy uses the existing
calculator's rounding and the [Maiden freeze interpolation](https://oldschool.runescape.wiki/w/User:Mc/Mechanics/ToB).
It assumes you can cast an ice spell: it does not check runes, spellbook, spell level requirements,
or a target's current freeze immunity. Levels to spare describe accuracy only.

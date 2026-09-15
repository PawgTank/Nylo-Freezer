# Nylo Freezer — RuneLite plugin

A native RuneLite sidebar calculator for the minimum Magic attack bonus needed to freeze nylocas at Maiden in Theatre of Blood.

## Features

- Native RuneLite sidebar panel (Swing / `PluginPanel`)
- Theatre of Blood-inspired dark red / burgundy UI
- Base Magic level 82–99
- Void Mage and Ice Sceptre toggles
- Saturated Heart, Imbued Heart, Forgotten Brew, Ancient Brew, Magic Potion, and no-boost options
- Augury, Mystic Vigour, Mystic Might, and no-prayer options
- Live required Magic attack result at drain 0
- Table for boost decay / stat drain 1 through 10
- Copyable result table
- No network access and no third-party dependencies

## Development setup

This source is intended to be placed into a repository generated from RuneLite's official `example-plugin` template.

1. Generate a repository from: https://github.com/runelite/example-plugin/generate
2. Replace the generated repository's `src`, `build.gradle`, `settings.gradle`, and `runelite-plugin.properties` with the files from this project.
3. Keep the template's `gradlew`, `gradlew.bat`, and `gradle/wrapper/` files.
4. Open the project in IntelliJ IDEA.
5. Run the Gradle `run` task.

The build uses `latest.release`, Java 11 compatibility, and `build=standard` for Plugin Hub compatibility.

## Calculator formulas

Boosts:

- Saturated Heart: `4 + floor(staticLevel * 10%)`
- Imbued Heart: `1 + floor(staticLevel * 10%)`
- Forgotten Brew: `3 + floor(staticLevel * 8%)`
- Ancient Brew: `2 + floor(staticLevel * 5%)`
- Magic Potion: `+4`

The remaining freeze calculation is preserved from the Nylo Freezer web app.

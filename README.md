# The Hollow Expanse  (Forge 1.21.1 / Java 21)

A void dimension of floating islands lit by living light.

## Get a jar (easiest, works from a phone)
1. Create a GitHub repo and upload this whole folder (unzipped).
2. Open **Actions → Build mod jar → Run workflow**.
3. Download the `hollow_expanse-jar` artifact. That jar is what you upload to CurseForge.
If the build fails, open the log and send me the first red error.

## Build locally
Copy `gradlew`, `gradlew.bat` and the `gradle/wrapper/gradle-wrapper.jar` from the Forge 1.21.1 MDK
(files.minecraftforge.net) into this folder, then:

    ./gradlew build          # jar appears in build/libs/
    ./gradlew runClient      # test in-game

`forge_version` in gradle.properties is 52.1.0 (the recommended 1.21.1 build).
**Set `mod_authors` in gradle.properties before publishing.**

## Playing it
1. Craft Reinforced Obsidian (8 obsidian + ender pearl = 4) and a Void Ignition Core
   (4 amethyst shards, 4 crying obsidian, 1 ender eye).
2. Build a closed frame (min 2x3 inside) of Reinforced Obsidian, right-click it with the Core.
3. Stand in the membrane for ~3 seconds. You arrive on a hub platform with a return portal.
4. Craft a Tether Hook (iron, string, ender pearl). Right-click = reel toward the block you look at.
   Sneak + right-click = recall to your last Anchor Stone.
5. Voidsteel Ore hangs from the undersides of islands. Smelt it, craft tools and armor.
   Full armor set = Weightless Step (slow fall, no fall damage).
6. Staying in the dark thins you out (Fading). Voidmoss, Lumen Blooms and Anchor Stones give light.
7. Fall off the bottom and you land in the Underlayer. Climb the five tiers with tethers; above y=300 you
   are lifted back out.
8. Hollow Sigil (4 voidsteel, 4 obsidian, 1 nether star) used on the ground awakens the Hollow Warden.
   Its drops: Warden's Core, Void Heart, voidsteel, and a book of Echo Strike. Smithing: Warden's Core +
   voidsteel tool + netherite ingot = Hollow-forged gear.

Creative-mode shortcuts: `/execute in hollow_expanse:hollow_expanse run tp @s 0 140 0`
and the spawn eggs in the creative tab.

## Config (`config/hollow_expanse-common.toml`)
fallToUnderlayer, fadingEnabled, fadingSeconds, wardenHealth, wardenCrumblesArena, portalOnlyInEnd.

## Tuning the islands (`data/hollow_expanse/worldgen/density_function/`, edit via `tools/gen_data.py`)
In `tiers(...)` each tuple is (top height, mask strength, bottom bias).
Higher mask strength = more/larger islands. A more negative bottom bias = thinner islands.
Tier spacing decides how far you must tether between layers.

## Regenerating assets
    python3 tools/gen_data.py       # all JSON (worldgen, recipes, models, lang...)
    python3 tools/gen_textures.py   # all glowing textures + logo
    python3 tools/verify.py         # cross-reference check

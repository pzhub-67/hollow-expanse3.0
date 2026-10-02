# What changed compared with the Gemini/DeepSeek drafts

Fixed (would have crashed, failed to compile, or made the mod unplayable)
- Terrain: the old noise settings did not produce floating islands and had invalid router/dimension-type/biome fields.
  Rebuilt as data-driven floating island tiers (flat tops, tapered undersides) for both dimensions.
- Wrong or missing 1.21.1 APIs: `LivingKnockbackEvent` is actually `LivingKnockBackEvent`; `changeDimension(ServerLevel)` no longer
  exists; enchantments cannot be registered in Java in 1.21 (they are data-driven); several overridden methods had wrong signatures.
- Softlock: the portal ingredients required Voidsteel, which only exists inside the dimension. New recipes use vanilla items.
- Voidsteel ore never generated. Added worldgen so veins hang from island undersides (as designed).
- Recipes: duplicate recipes/ and recipe/ folders, and no recipes at all for Voidsteel tools, armor, Hollow-forged gear, Sigil.
- The Warden could spawn naturally. Now it is summoned with the Hollow Sigil.
- Missing placeholder sounds/particles referenced files that did not exist; replaced with vanilla sounds/particles.
- Textures: none were generated for most items/entities. All textures are now generated, smooth and glowing, with emissive layers on mobs.

Added to finish the design
- Underlayer dimension + climb-out mechanic, hub platform + return portal, Anchor recall, Fading tied to light,
  Weightless Step, Echo Strike, Void Siphon, Gravity Anchor, arena crumbling, boss phases, advancements, config,
  CurseForge logo/description/checklist, cloud build workflow, static verification script.

Not verified here (no Minecraft/Forge jars or network in my sandbox)
- I could not compile or launch the game. JSON, cross-references and Java syntax were checked statically only.
  Expect to possibly fix a few small API mismatches on the first build, and to tune island noise values in game.

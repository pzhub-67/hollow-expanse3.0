# CurseForge upload checklist

Before building
- [ ] `gradle.properties`: set `mod_authors` to your name (it ends up in the jar and mods.toml).
- [ ] `mods.toml` license is "All Rights Reserved". Pick the license you actually want and use the same one on CurseForge.
- [ ] Build the jar (README: GitHub Actions route works from a phone). File: `hollow_expanse-1.0.0.jar`.

Test before you upload (10 minutes in a creative world)
- [ ] World loads with no crash. Creative tab "The Hollow Expanse" shows every item with a texture.
- [ ] `/execute in hollow_expanse:hollow_expanse run tp @s 0 140 0`: islands generate, you land on one.
- [ ] Craft and light the portal in the Overworld; arrive on the hub; the return portal works.
- [ ] Look under islands for glowing Voidsteel ore; mine it with an iron pickaxe.
- [ ] Tether Hook pulls you; sneak + use recalls to an Anchor Stone.
- [ ] Fall off the edge: land in the Underlayer; climb past y=300 and get lifted out.
- [ ] Spawn egg + Hollow Sigil: boss bar appears, phases trigger, arena crumbles, loot drops.
- [ ] Enchant a sword with Void Siphon (`/enchant @s hollow_expanse:void_siphon`).

On CurseForge (Create Project -> Minecraft -> Mods)
- Name: The Hollow Expanse    Slug: hollow-expanse
- Categories: World Gen, Adventure and RPG (and Mobs / Armor, Tools, and Weapons if offered)
- Summary (short): A void dimension of floating islands, a grappling tether mechanic, glowing Voidsteel and a three-phase boss.
- Description: paste `curseforge/DESCRIPTION.md`.  Project avatar: `curseforge/project_avatar_256.png`.
- Add 3+ screenshots (portal, islands at night, the boss). Moderators like screenshots.
- Upload file: release type Release (or Beta for a first test), Game version 1.21.1, Mod loader Forge, Java 21.
- Changelog: paste `curseforge/CHANGELOG.md`.
- Dependencies: none.

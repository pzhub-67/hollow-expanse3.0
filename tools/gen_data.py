"""Writes all JSON data/assets for The Hollow Expanse. Run from the project root:  python3 tools/gen_data.py"""
import json, os, shutil
M = "hollow_expanse"
RES = "src/main/resources"
D, A = f"data/{M}", f"assets/{M}"

# wipe generated folders so stale files can never ship
for sub in ("data", "assets"):
    shutil.rmtree(os.path.join(RES, sub), ignore_errors=True)

def w(path, obj):
    path = os.path.join(RES, path)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w") as f:
        json.dump(obj, f, indent=2)

def rgb(r, g, b): return (r << 16) | (g << 8) | b
def item(i): return {"item": i}
def ns(x): return f"{M}:{x}"

# ===================================================================== DIMENSIONS
w(f"{D}/dimension_type/hollow_expanse.json", {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0,
    "has_skylight": True, "has_ceiling": False, "ambient_light": 0.1, "fixed_time": 18000,
    "piglin_safe": False, "bed_works": False, "respawn_anchor_works": False, "has_raids": False,
    "min_y": -64, "height": 384, "logical_height": 384,
    "infiniburn": "#minecraft:infiniburn_end", "effects": "minecraft:overworld",
    "monster_spawn_light_level": 7, "monster_spawn_block_light_limit": 15})
w(f"{D}/dimension_type/underlayer.json", {
    "ultrawarm": False, "natural": False, "coordinate_scale": 1.0,
    "has_skylight": False, "has_ceiling": False, "ambient_light": 0.06,
    "piglin_safe": False, "bed_works": False, "respawn_anchor_works": False, "has_raids": False,
    "min_y": -64, "height": 384, "logical_height": 384,
    "infiniburn": "#minecraft:infiniburn_end", "effects": "minecraft:the_end",
    "monster_spawn_light_level": 7, "monster_spawn_block_light_limit": 15})
for dim in ("hollow_expanse", "underlayer"):
    w(f"{D}/dimension/{dim}.json", {
        "type": ns(dim),
        "generator": {"type": "minecraft:noise", "settings": ns(dim),
                      "biome_source": {"type": "minecraft:fixed", "biome": ns(dim)}}})

# ===================================================================== BIOMES
def biome(sky, fog, water, water_fog, monsters, ambient, features):
    return {
        "has_precipitation": False, "temperature": 0.5, "downfall": 0.0,
        "effects": {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog,
                    "particle": {"probability": 0.005, "options": {"type": "minecraft:end_rod"}}},
        "spawners": {"monster": monsters, "creature": [], "ambient": ambient, "axolotls": [],
                     "underground_water_creature": [], "water_creature": [], "water_ambient": [], "misc": []},
        "spawn_costs": {}, "carvers": [], "features": features}

def feats(ore, bloom):
    f = [[] for _ in range(11)]
    f[6] = [ns(ore)]
    f[9] = [ns(bloom)]
    return f

w(f"{D}/worldgen/biome/hollow_expanse.json", biome(
    rgb(0x0b, 0x0a, 0x1f), rgb(0x16, 0x12, 0x33), rgb(0x1a, 0x6b, 0x6b), rgb(0x0a, 0x1a, 0x2a),
    [{"type": ns("husk_stalker"), "weight": 30, "minCount": 1, "maxCount": 2}],
    [{"type": ns("drifter"), "weight": 20, "minCount": 1, "maxCount": 3}],
    feats("voidsteel_ore_hanging", "lumen_bloom_patch")))
w(f"{D}/worldgen/biome/underlayer.json", biome(
    rgb(0x02, 0x08, 0x0c), rgb(0x05, 0x14, 0x1a), rgb(0x0d, 0x33, 0x33), rgb(0x03, 0x0c, 0x10),
    [{"type": ns("husk_stalker"), "weight": 100, "minCount": 2, "maxCount": 4}],
    [{"type": ns("drifter"), "weight": 6, "minCount": 1, "maxCount": 2}],
    feats("voidsteel_ore_hanging_rich", "lumen_bloom_patch_sparse")))

# ===================================================================== FEATURES
bloom_state = {"type": "minecraft:simple_state_provider", "state": {"Name": ns("lumen_bloom")}}
bloom_filter = {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
    {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"},
    {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ns("voidmoss")}]}}
w(f"{D}/worldgen/configured_feature/lumen_bloom_patch.json", {
    "type": "minecraft:random_patch",
    "config": {"tries": 40, "xz_spread": 6, "y_spread": 3, "feature": {
        "feature": {"type": "minecraft:simple_block", "config": {"to_place": bloom_state}},
        "placement": [bloom_filter]}}})
w(f"{D}/worldgen/configured_feature/voidsteel_ore.json", {
    "type": "minecraft:ore",
    "config": {"size": 9, "discard_chance_on_air_exposure": 0.0, "targets": [{
        "target": {"predicate_type": "minecraft:block_match", "block": ns("hollow_stone")},
        "state": {"Name": ns("voidsteel_ore")}}]}})

def height(lo, hi):
    return {"type": "minecraft:height_range", "height": {
        "type": "minecraft:uniform", "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}}
def scan(direction, steps):
    return {"type": "minecraft:environment_scan", "direction_of_search": direction, "max_steps": steps,
            "target_condition": {"type": "minecraft:solid"},
            "allowed_search_condition": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}
BIOME = {"type": "minecraft:biome"}

# Voidsteel: random air pocket -> scan UP to the ceiling -> vein embedded in the island undersides
for name, count in (("voidsteel_ore_hanging", 22), ("voidsteel_ore_hanging_rich", 55)):
    w(f"{D}/worldgen/placed_feature/{name}.json", {"feature": ns("voidsteel_ore"), "placement": [
        {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
        height(0, 300 if name.endswith("rich") else 170), scan("up", 24), BIOME]})
for name, count in (("lumen_bloom_patch", 3), ("lumen_bloom_patch_sparse", 1)):
    w(f"{D}/worldgen/placed_feature/{name}.json", {"feature": ns("lumen_bloom_patch"), "placement": [
        {"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
        height(0, 300 if name.endswith("sparse") else 175), scan("down", 40), BIOME]})

# ===================================================================== TERRAIN (floating island tiers)
w(f"{D}/worldgen/noise/island_top.json",    {"firstOctave": -5, "amplitudes": [1.0, 0.5, 0.25]})
w(f"{D}/worldgen/noise/island_detail.json", {"firstOctave": -4, "amplitudes": [1.0, 0.5, 0.5]})
DF = f"{D}/worldgen/density_function"
w(f"{DF}/island_top_noise.json", {"type": "minecraft:flat_cache", "argument": {
    "type": "minecraft:noise", "noise": ns("island_top"), "xz_scale": 1.0, "y_scale": 0.0}})

def mul(a, b): return {"type": "minecraft:mul", "argument1": a, "argument2": b}
def add(a, b): return {"type": "minecraft:add", "argument1": a, "argument2": b}
def grad(y0, y1, v0, v1): return {"type": "minecraft:y_clamped_gradient", "from_y": y0, "to_y": y1, "from_value": v0, "to_value": v1}

def tiers(dim, spec):
    """spec: list of (top_y, mask_strength, bottom_bias).  Larger bias magnitude -> thinner islands."""
    names = []
    for i, (base, k, g0) in enumerate(spec):
        t = f"{dim}_t{i}"
        w(f"{D}/worldgen/noise/{t}_cells.json", {"firstOctave": -7, "amplitudes": [1.0, 1.0, 0.5, 0.25]})
        w(f"{DF}/{t}_mask.json", {"type": "minecraft:flat_cache", "argument": {
            "type": "minecraft:noise", "noise": ns(f"{t}_cells"), "xz_scale": 1.0, "y_scale": 0.0}})
        mask = ns(f"{t}_mask")
        w(f"{DF}/{t}_lower.json", add(
            add(mul(k, mask), grad(base - 78, base - 2, g0, -0.35)),
            mul(0.6, {"type": "minecraft:noise", "noise": ns("island_detail"), "xz_scale": 1.0, "y_scale": 0.6})))
        w(f"{DF}/{t}_cut.json", add(
            add(grad(base - 10, base + 10, 1.0, -1.0), mul(1.0, ns("island_top_noise"))),
            mul(0.6, mask)))
        w(f"{DF}/{t}.json", {"type": "minecraft:min", "argument1": ns(f"{t}_lower"), "argument2": ns(f"{t}_cut")})
        names.append(ns(t))
    combined = names[0]
    for n in names[1:]:
        combined = {"type": "minecraft:max", "argument1": combined, "argument2": n}
    w(f"{DF}/{dim}_final.json", {"type": "minecraft:interpolated", "argument": combined})

tiers("hollow_expanse", [(70, 2.4, -2.4), (150, 2.1, -2.5)])
tiers("underlayer", [(60, 2.3, -2.6), (115, 2.3, -2.6), (170, 2.3, -2.6), (225, 2.3, -2.6), (280, 2.3, -2.6)])

def surface(top, soil):
    return {"type": "minecraft:sequence", "sequence": [
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": False, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": ns(top)}}},
        {"type": "minecraft:condition",
         "if_true": {"type": "minecraft:stone_depth", "offset": 0, "surface_type": "floor",
                     "add_surface_depth": True, "secondary_depth_range": 0},
         "then_run": {"type": "minecraft:block", "result_state": {"Name": ns(soil)}}}]}

for dim in ("hollow_expanse", "underlayer"):
    w(f"{D}/worldgen/noise_settings/{dim}.json", {
        "sea_level": 0, "disable_mob_generation": False, "aquifers_enabled": False,
        "ore_veins_enabled": False, "legacy_random_source": True,
        "default_block": {"Name": ns("hollow_stone")}, "default_fluid": {"Name": "minecraft:air"},
        "noise": {"min_y": -64, "height": 384, "size_horizontal": 2, "size_vertical": 1},
        "noise_router": {
            "barrier": 0.0, "fluid_level_floodedness": 0.0, "fluid_level_spread": 0.0, "lava": 0.0,
            "temperature": 0.0, "vegetation": 0.0, "continents": 0.0, "erosion": 0.0, "depth": 0.0, "ridges": 0.0,
            "initial_density_without_jaggedness": 0.0, "final_density": ns(f"{dim}_final"),
            "vein_toggle": 0.0, "vein_ridged": 0.0, "vein_gap": 0.0},
        "spawn_target": [], "surface_rule": surface("voidmoss", "hollow_soil")})

# ===================================================================== ENCHANTMENTS (1.21 data-driven)
def enchant(name, items, weight, max_level, slots, min_base, min_step, anvil):
    w(f"{D}/enchantment/{name}.json", {
        "description": {"translate": f"enchantment.{M}.{name}"},
        "supported_items": items, "weight": weight, "max_level": max_level,
        "min_cost": {"base": min_base, "per_level_above_first": min_step},
        "max_cost": {"base": min_base + 30, "per_level_above_first": min_step},
        "anvil_cost": anvil, "slots": slots, "effects": {}})
enchant("gravity_anchor", f"#{M}:enchantable/gravity_armor", 3, 3, ["chest", "feet"], 12, 10, 4)
enchant("void_siphon",    f"#{M}:enchantable/melee",         2, 2, ["mainhand"],       15, 12, 5)
enchant("echo_strike",    f"#{M}:enchantable/melee",         1, 1, ["mainhand"],       45, 0, 8)

vanilla_chest = [f"minecraft:{m}_chestplate" for m in ("leather", "chainmail", "iron", "golden", "diamond", "netherite")]
vanilla_feet = [f"minecraft:{m}_boots" for m in ("leather", "chainmail", "iron", "golden", "diamond", "netherite")]
w(f"{D}/tags/item/enchantable/gravity_armor.json", {"values": vanilla_chest + vanilla_feet + [ns("voidsteel_chestplate"), ns("voidsteel_boots")]})
w(f"{D}/tags/item/enchantable/melee.json", {"values": [
    "#minecraft:swords", "#minecraft:axes", ns("voidsteel_sword"), ns("voidsteel_axe"),
    ns("hollowforged_sword"), ns("void_mace")]})
w("data/minecraft/tags/enchantment/non_treasure.json", {"values": [ns("gravity_anchor"), ns("void_siphon")]})
w("data/minecraft/tags/enchantment/in_enchanting_table.json", {"values": [ns("gravity_anchor"), ns("void_siphon")]})
w("data/minecraft/tags/enchantment/treasure.json", {"values": [ns("echo_strike")]})
w("data/minecraft/tags/enchantment/on_random_loot.json", {"values": [ns("gravity_anchor"), ns("void_siphon"), ns("echo_strike")]})

# ===================================================================== ITEM / BLOCK TAGS
tools = {"swords": ["voidsteel_sword", "hollowforged_sword"], "pickaxes": ["voidsteel_pickaxe", "hollowforged_pickaxe"],
         "axes": ["voidsteel_axe"], "shovels": ["voidsteel_shovel"], "hoes": ["voidsteel_hoe"],
         "head_armor": ["voidsteel_helmet"], "chest_armor": ["voidsteel_chestplate"],
         "leg_armor": ["voidsteel_leggings"], "foot_armor": ["voidsteel_boots"]}
for tag, ids in tools.items():
    w(f"data/minecraft/tags/item/{tag}.json", {"values": [ns(i) for i in ids]})
ench = {"sword": tools["swords"], "sharp_weapon": ["voidsteel_sword", "hollowforged_sword", "voidsteel_axe"],
        "weapon": ["voidsteel_sword", "hollowforged_sword", "voidsteel_axe", "void_mace"],
        "mining": tools["pickaxes"] + tools["axes"] + tools["shovels"] + tools["hoes"],
        "mining_loot": tools["pickaxes"] + tools["axes"] + tools["shovels"] + tools["hoes"],
        "durability": sum(tools.values(), []) + ["void_mace", "tether_hook", "voidsteel_tether_hook"],
        "armor": sum([tools[k] for k in ("head_armor", "chest_armor", "leg_armor", "foot_armor")], []),
        "head_armor": tools["head_armor"], "chest_armor": tools["chest_armor"],
        "leg_armor": tools["leg_armor"], "foot_armor": tools["foot_armor"]}
for tag, ids in ench.items():
    w(f"data/minecraft/tags/item/enchantable/{tag}.json", {"values": [ns(i) for i in ids]})

w("data/minecraft/tags/block/mineable/pickaxe.json", {"values": [ns(b) for b in
    ("hollow_stone", "voidsteel_ore", "reinforced_obsidian", "anchor_stone")]})
w("data/minecraft/tags/block/mineable/shovel.json", {"values": [ns("hollow_soil"), ns("voidmoss")]})
w("data/minecraft/tags/block/needs_iron_tool.json", {"values": [ns("voidsteel_ore")]})
w("data/minecraft/tags/block/needs_diamond_tool.json", {"values": [ns("reinforced_obsidian")]})

# ===================================================================== LOOT
def self_drop(block):
    return {"type": "minecraft:block", "pools": [{"rolls": 1.0, "bonus_rolls": 0.0,
            "entries": [{"type": "minecraft:item", "name": ns(block)}],
            "conditions": [{"condition": "minecraft:survives_explosion"}]}]}
for b in ("hollow_stone", "hollow_soil", "voidmoss", "reinforced_obsidian", "anchor_stone", "lumen_bloom"):
    w(f"{D}/loot_table/blocks/{b}.json", self_drop(b))
silk = {"condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}
w(f"{D}/loot_table/blocks/voidsteel_ore.json", {"type": "minecraft:block", "pools": [{
    "rolls": 1.0, "bonus_rolls": 0.0, "entries": [{"type": "minecraft:alternatives", "children": [
        {"type": "minecraft:item", "name": ns("voidsteel_ore"), "conditions": [silk]},
        {"type": "minecraft:item", "name": ns("raw_voidsteel"), "functions": [
            {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
            {"function": "minecraft:explosion_decay"}]}]}]}]})

def pool(entries, rolls=1.0, conditions=None):
    p = {"rolls": rolls, "bonus_rolls": 0.0, "entries": entries}
    if conditions: p["conditions"] = conditions
    return p
def entry(i, lo=None, hi=None, functions=None):
    e = {"type": "minecraft:item", "name": i}
    fn = list(functions or [])
    if lo is not None:
        fn.insert(0, {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    if fn: e["functions"] = fn
    return e
w(f"{D}/loot_table/entities/hollow_warden.json", {"type": "minecraft:entity", "pools": [
    pool([entry(ns("wardens_core"), 1, 2)]),
    pool([entry(ns("void_heart"))]),
    pool([entry(ns("voidsteel_ingot"), 4, 8)]),
    pool([entry("minecraft:enchanted_book", functions=[
        {"function": "minecraft:set_enchantments", "enchantments": {ns("echo_strike"): 1}}])])]})
w(f"{D}/loot_table/entities/husk_stalker.json", {"type": "minecraft:entity", "pools": [
    pool([entry("minecraft:rotten_flesh", 0, 2)]),
    pool([entry(ns("raw_voidsteel"))], conditions=[{"condition": "minecraft:random_chance", "chance": 0.12}])]})
w(f"{D}/loot_table/entities/drifter.json", {"type": "minecraft:entity", "pools": [
    pool([entry("minecraft:glow_ink_sac", 0, 2)]),
    pool([entry(ns("lumen_bloom"))], conditions=[{"condition": "minecraft:random_chance", "chance": 0.25}])]})

# ===================================================================== RECIPES
R = f"{D}/recipe"
def shaped(name, pattern, key, result, count=1, cat="misc"):
    w(f"{R}/{name}.json", {"type": "minecraft:crafting_shaped", "category": cat, "pattern": pattern,
        "key": {k: item(v) for k, v in key.items()}, "result": {"id": result, "count": count}})
V, S = ns("voidsteel_ingot"), "minecraft:stick"
for kind, cook in (("smelting", 200), ("blasting", 100)):
    for src in ("raw_voidsteel", "voidsteel_ore"):
        w(f"{R}/voidsteel_ingot_from_{kind}_{src}.json", {"type": f"minecraft:{kind}", "category": "misc",
            "ingredient": item(ns(src)), "result": {"id": V}, "experience": 1.0, "cookingtime": cook})
shaped("reinforced_obsidian", ["OOO", "OPO", "OOO"], {"O": "minecraft:obsidian", "P": "minecraft:ender_pearl"}, ns("reinforced_obsidian"), 4, "building")
shaped("void_ignition_core", ["ACA", "CEC", "ACA"], {"A": "minecraft:amethyst_shard", "C": "minecraft:crying_obsidian", "E": "minecraft:ender_eye"}, ns("void_ignition_core"))
shaped("tether_hook", ["I I", "SPS", " S "], {"I": "minecraft:iron_ingot", "S": "minecraft:string", "P": "minecraft:ender_pearl"}, ns("tether_hook"), 1, "equipment")
w(f"{R}/voidsteel_tether_hook.json", {"type": "minecraft:crafting_shapeless", "category": "equipment",
    "ingredients": [item(ns("tether_hook"))] + [item(V)] * 3, "result": {"id": ns("voidsteel_tether_hook"), "count": 1}})
shaped("anchor_stone", ["VOV", "OEO", "VOV"], {"V": V, "O": "minecraft:obsidian", "E": "minecraft:ender_eye"}, ns("anchor_stone"), 2, "building")
shaped("hollow_sigil", ["VOV", "ONO", "VOV"], {"V": V, "O": "minecraft:obsidian", "N": "minecraft:nether_star"}, ns("hollow_sigil"))
shaped("voidsteel_sword", ["V", "V", "S"], {"V": V, "S": S}, ns("voidsteel_sword"), 1, "equipment")
shaped("voidsteel_pickaxe", ["VVV", " S ", " S "], {"V": V, "S": S}, ns("voidsteel_pickaxe"), 1, "equipment")
shaped("voidsteel_axe", ["VV", "VS", " S"], {"V": V, "S": S}, ns("voidsteel_axe"), 1, "equipment")
shaped("voidsteel_shovel", ["V", "S", "S"], {"V": V, "S": S}, ns("voidsteel_shovel"), 1, "equipment")
shaped("voidsteel_hoe", ["VV", " S", " S"], {"V": V, "S": S}, ns("voidsteel_hoe"), 1, "equipment")
shaped("voidsteel_helmet", ["VVV", "V V"], {"V": V}, ns("voidsteel_helmet"), 1, "equipment")
shaped("voidsteel_chestplate", ["V V", "VVV", "VVV"], {"V": V}, ns("voidsteel_chestplate"), 1, "equipment")
shaped("voidsteel_leggings", ["VVV", "V V", "V V"], {"V": V}, ns("voidsteel_leggings"), 1, "equipment")
shaped("voidsteel_boots", ["V V", "V V"], {"V": V}, ns("voidsteel_boots"), 1, "equipment")
shaped("void_mace", ["VCV", "VBV", " B "], {"V": V, "C": ns("wardens_core"), "B": "minecraft:blaze_rod"}, ns("void_mace"), 1, "equipment")
for kind in ("sword", "pickaxe"):
    w(f"{R}/hollowforged_{kind}.json", {"type": "minecraft:smithing_transform",
        "template": item(ns("wardens_core")), "base": item(ns(f"voidsteel_{kind}")),
        "addition": item("minecraft:netherite_ingot"), "result": {"id": ns(f"hollowforged_{kind}"), "count": 1}})

# ===================================================================== ADVANCEMENTS
def adv(name, parent, icon, frame, criteria, title, desc, **kw):
    d = {"display": {"icon": {"id": icon}, "title": {"translate": f"advancements.{M}.{name}.title"},
                     "description": {"translate": f"advancements.{M}.{name}.description"},
                     "frame": frame, "show_toast": True, "announce_to_chat": True, "hidden": False},
         "criteria": criteria}
    if parent: d["parent"] = parent
    w(f"{D}/advancement/{name}.json", d)
adv("root", None, ns("void_ignition_core"), "task",
    {"entered": {"trigger": "minecraft:changed_dimension", "conditions": {"to": ns("hollow_expanse")}}}, "", "")
adv("voidsteel_age", ns("root"), ns("voidsteel_ingot"), "task",
    {"has": {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": ns("voidsteel_ingot")}]}}}, "", "")
adv("tether_master", ns("root"), ns("tether_hook"), "goal", {"use": {"trigger": "minecraft:impossible"}}, "", "")
adv("bottomless", ns("root"), "minecraft:ender_pearl", "task", {"fall": {"trigger": "minecraft:impossible"}}, "", "")
adv("hollow_king", ns("voidsteel_age"), ns("void_heart"), "challenge",
    {"kill": {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": [
        {"condition": "minecraft:entity_properties", "entity": "this", "predicate": {"type": ns("hollow_warden")}}]}}}, "", "")

# ===================================================================== BLOCK MODELS / BLOCKSTATES
cube_blocks = ["hollow_stone", "hollow_soil", "voidsteel_ore", "reinforced_obsidian"]
for b in cube_blocks:
    w(f"{A}/blockstates/{b}.json", {"variants": {"": {"model": ns(f"block/{b}")}}})
    w(f"{A}/models/block/{b}.json", {"parent": "minecraft:block/cube_all", "textures": {"all": ns(f"block/{b}")}})
w(f"{A}/blockstates/voidmoss.json", {"variants": {"": {"model": ns("block/voidmoss")}}})
w(f"{A}/models/block/voidmoss.json", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": ns("block/voidmoss_top"), "side": ns("block/voidmoss_side"), "bottom": ns("block/hollow_soil")}})

def face(tex, uv=(0, 0, 16, 16), cull=None):
    f = {"uv": list(uv), "texture": tex}
    if cull: f["cullface"] = cull
    return f
w(f"{A}/blockstates/anchor_stone.json", {"variants": {"": {"model": ns("block/anchor_stone")}}})
w(f"{A}/models/block/anchor_stone.json", {"textures": {"side": ns("block/anchor_stone_side"),
    "top": ns("block/anchor_stone_top"), "particle": ns("block/anchor_stone_side")}, "elements": [
    {"from": [0, 0, 0], "to": [16, 5, 16], "faces": {
        "down": face("#top", cull="down"), "up": face("#top"), "north": face("#side", (0, 11, 16, 16), "north"),
        "south": face("#side", (0, 11, 16, 16), "south"), "west": face("#side", (0, 11, 16, 16), "west"),
        "east": face("#side", (0, 11, 16, 16), "east")}},
    {"from": [4, 5, 4], "to": [12, 16, 12], "faces": {
        "up": face("#top", (4, 4, 12, 12)), "north": face("#side", (4, 0, 12, 11)), "south": face("#side", (4, 0, 12, 11)),
        "west": face("#side", (4, 0, 12, 11)), "east": face("#side", (4, 0, 12, 11))}}]})

w(f"{A}/blockstates/lumen_bloom.json", {"variants": {"": {"model": ns("block/lumen_bloom")}}})
w(f"{A}/models/block/lumen_bloom.json", {"parent": "minecraft:block/cross", "render_type": "minecraft:cutout",
    "textures": {"cross": ns("block/lumen_bloom")}})

w(f"{A}/blockstates/hollow_portal.json", {"variants": {
    "axis=x": {"model": ns("block/hollow_portal_x")}, "axis=z": {"model": ns("block/hollow_portal_z")}}})
w(f"{A}/models/block/hollow_portal_x.json", {"render_type": "minecraft:translucent",
    "textures": {"portal": ns("block/hollow_portal"), "particle": ns("block/hollow_portal")}, "elements": [
    {"from": [0, 0, 6], "to": [16, 16, 10], "shade": False, "faces": {
        "north": face("#portal"), "south": face("#portal")}}]})
w(f"{A}/models/block/hollow_portal_z.json", {"render_type": "minecraft:translucent",
    "textures": {"portal": ns("block/hollow_portal"), "particle": ns("block/hollow_portal")}, "elements": [
    {"from": [6, 0, 0], "to": [10, 16, 16], "shade": False, "faces": {
        "west": face("#portal"), "east": face("#portal")}}]})

# ---- item models
for b in cube_blocks + ["voidmoss", "anchor_stone"]:
    w(f"{A}/models/item/{b}.json", {"parent": ns(f"block/{b}")})
w(f"{A}/models/item/lumen_bloom.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ns("block/lumen_bloom")}})
flat = ["raw_voidsteel", "voidsteel_ingot", "void_ignition_core", "void_shard", "hollow_sigil", "wardens_core", "void_heart",
        "tether_hook", "voidsteel_tether_hook", "voidsteel_helmet", "voidsteel_chestplate", "voidsteel_leggings", "voidsteel_boots"]
held = ["voidsteel_sword", "voidsteel_pickaxe", "voidsteel_axe", "voidsteel_shovel", "voidsteel_hoe",
        "hollowforged_sword", "hollowforged_pickaxe", "void_mace"]
for i in flat:
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": ns(f"item/{i}")}})
for i in held:
    w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/handheld", "textures": {"layer0": ns(f"item/{i}")}})
for e in ("hollow_warden", "drifter", "husk_stalker"):
    w(f"{A}/models/item/{e}_spawn_egg.json", {"parent": "minecraft:item/template_spawn_egg"})

# ===================================================================== LANG
lang = {
 "itemGroup.hollow_expanse": "The Hollow Expanse",
 "block.hollow_expanse.hollow_stone": "Hollow Stone", "block.hollow_expanse.hollow_soil": "Hollow Soil",
 "block.hollow_expanse.voidmoss": "Voidmoss", "block.hollow_expanse.voidsteel_ore": "Voidsteel Ore",
 "block.hollow_expanse.reinforced_obsidian": "Reinforced Obsidian", "block.hollow_expanse.anchor_stone": "Anchor Stone",
 "block.hollow_expanse.lumen_bloom": "Lumen Bloom", "block.hollow_expanse.hollow_portal": "Hollow Portal",
 "item.hollow_expanse.raw_voidsteel": "Raw Voidsteel", "item.hollow_expanse.voidsteel_ingot": "Voidsteel Ingot",
 "item.hollow_expanse.void_ignition_core": "Void Ignition Core", "item.hollow_expanse.void_shard": "Void Shard",
 "item.hollow_expanse.hollow_sigil": "Hollow Sigil", "item.hollow_expanse.wardens_core": "Warden's Core",
 "item.hollow_expanse.void_heart": "Void Heart", "item.hollow_expanse.tether_hook": "Tether Hook",
 "item.hollow_expanse.voidsteel_tether_hook": "Voidsteel Tether Hook",
 "item.hollow_expanse.voidsteel_sword": "Voidsteel Sword", "item.hollow_expanse.voidsteel_pickaxe": "Voidsteel Pickaxe",
 "item.hollow_expanse.voidsteel_axe": "Voidsteel Axe", "item.hollow_expanse.voidsteel_shovel": "Voidsteel Shovel",
 "item.hollow_expanse.voidsteel_hoe": "Voidsteel Hoe", "item.hollow_expanse.hollowforged_sword": "Hollow-forged Sword",
 "item.hollow_expanse.hollowforged_pickaxe": "Hollow-forged Pickaxe", "item.hollow_expanse.void_mace": "Void Mace",
 "item.hollow_expanse.voidsteel_helmet": "Voidsteel Helmet", "item.hollow_expanse.voidsteel_chestplate": "Voidsteel Chestplate",
 "item.hollow_expanse.voidsteel_leggings": "Voidsteel Leggings", "item.hollow_expanse.voidsteel_boots": "Voidsteel Boots",
 "item.hollow_expanse.hollow_warden_spawn_egg": "Hollow Warden Spawn Egg",
 "item.hollow_expanse.drifter_spawn_egg": "Drifter Spawn Egg", "item.hollow_expanse.husk_stalker_spawn_egg": "Husk Stalker Spawn Egg",
 "entity.hollow_expanse.hollow_warden": "The Hollow Warden", "entity.hollow_expanse.drifter": "Drifter",
 "entity.hollow_expanse.husk_stalker": "Husk Stalker", "entity.hollow_expanse.void_shard": "Void Shard",
 "effect.hollow_expanse.fading": "Fading",
 "enchantment.hollow_expanse.gravity_anchor": "Gravity Anchor", "enchantment.hollow_expanse.void_siphon": "Void Siphon",
 "enchantment.hollow_expanse.echo_strike": "Echo Strike",
 "message.hollow_expanse.anchor_bound": "Anchor bound. Sneak + use a Tether Hook to recall here.",
 "message.hollow_expanse.portal_invalid": "The frame must be a closed ring of Reinforced Obsidian, at least 2 wide and 3 tall inside.",
 "message.hollow_expanse.portal_end_only": "The portal will only ignite in the End.",
 "message.hollow_expanse.dimension_missing": "The Hollow Expanse could not be found.",
 "message.hollow_expanse.tether_no_target": "Nothing to tether to in range.",
 "message.hollow_expanse.no_anchor": "No Anchor Stone bound in this dimension.",
 "message.hollow_expanse.sigil_wrong_place": "The Sigil only awakens something in the Hollow Expanse.",
 "message.hollow_expanse.sigil_already": "The Warden already stirs nearby.",
 "message.hollow_expanse.warden_phase_2": "The ground begins to pull...",
 "message.hollow_expanse.warden_phase_3": "The island shatters. The Warden vanishes into the dark.",
 "message.hollow_expanse.fading": "The dark is thinning you. Find light.",
 "message.hollow_expanse.fell_into_underlayer": "You fall through the Hollow into the Underlayer.",
 "message.hollow_expanse.climbed_out": "You climb out of the Underlayer.",
 "advancements.hollow_expanse.root.title": "The Hollow Expanse",
 "advancements.hollow_expanse.root.description": "Step through the portal into the void",
 "advancements.hollow_expanse.voidsteel_age.title": "Light in the Dark",
 "advancements.hollow_expanse.voidsteel_age.description": "Smelt a Voidsteel Ingot",
 "advancements.hollow_expanse.tether_master.title": "Tether Master",
 "advancements.hollow_expanse.tether_master.description": "Reel yourself across the void 50 times",
 "advancements.hollow_expanse.bottomless.title": "Bottomless",
 "advancements.hollow_expanse.bottomless.description": "Fall through the bottom of the Hollow Expanse",
 "advancements.hollow_expanse.hollow_king.title": "Hollow King",
 "advancements.hollow_expanse.hollow_king.description": "Defeat the Hollow Warden",
}
w(f"{A}/lang/en_us.json", lang)
print("data + assets written")

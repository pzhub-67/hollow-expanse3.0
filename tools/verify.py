"""Static consistency checks (no Minecraft needed). Run from project root: python3 tools/verify.py"""
import json, re, glob, os, sys
M = "hollow_expanse"
RES = "src/main/resources"
J = "src/main/java"
errors = []
def err(m): errors.append(m)

# 1. all JSON parses
for p in glob.glob(f"{RES}/**/*.json", recursive=True) + [f"{RES}/pack.mcmeta"]:
    try: json.load(open(p))
    except Exception as e: err(f"bad JSON {p}: {e}")

# 2. registered ids in Java
blocks = re.findall(r'register(?:Block)?\("([a-z_]+)"', open(f"{J}/com/hollowexpanse/registry/ModBlocks.java").read())
blocks += re.findall(r'BLOCKS\.register\("([a-z_]+)"', open(f"{J}/com/hollowexpanse/registry/ModBlocks.java").read())
blocks = sorted(set(blocks))
items = sorted(set(re.findall(r'ITEMS\.register\("([a-z_]+)"', open(f"{J}/com/hollowexpanse/registry/ModItems.java").read())))
ents = sorted(set(re.findall(r'register\("([a-z_]+)"', open(f"{J}/com/hollowexpanse/registry/ModEntities.java").read())))
print("blocks:", blocks); print("items:", len(items)); print("entities:", ents)

lang = json.load(open(f"{RES}/assets/{M}/lang/en_us.json"))
for b in blocks:
    if f"block.{M}.{b}" not in lang: err(f"lang missing block {b}")
    if not os.path.exists(f"{RES}/assets/{M}/blockstates/{b}.json"): err(f"blockstate missing {b}")
    if b != "hollow_portal" and not os.path.exists(f"{RES}/assets/{M}/models/item/{b}.json"): err(f"item model missing for block {b}")
for i in items:
    if f"item.{M}.{i}" not in lang: err(f"lang missing item {i}")
    if not os.path.exists(f"{RES}/assets/{M}/models/item/{i}.json"): err(f"item model missing {i}")
for e in ents:
    if f"entity.{M}.{e}" not in lang: err(f"lang missing entity {e}")

# 3. every texture referenced by a model exists
for p in glob.glob(f"{RES}/assets/{M}/models/**/*.json", recursive=True):
    d = json.load(open(p))
    for k, v in d.get("textures", {}).items():
        if v.startswith(f"{M}:"):
            if not os.path.exists(f"{RES}/assets/{M}/textures/{v.split(':')[1]}.png"): err(f"{p}: missing texture {v}")
# every model parent in mod namespace exists
for p in glob.glob(f"{RES}/assets/{M}/**/*.json", recursive=True):
    d = json.load(open(p))
    par = d.get("parent", "")
    if par.startswith(f"{M}:") and not os.path.exists(f"{RES}/assets/{M}/models/{par.split(':')[1]}.json"): err(f"{p}: missing parent {par}")
    for var in (d.get("variants", {}) or {}).values():
        mdl = var.get("model", "")
        if mdl.startswith(f"{M}:") and not os.path.exists(f"{RES}/assets/{M}/models/{mdl.split(':')[1]}.json"): err(f"{p}: missing model {mdl}")

# 4. entity textures + armor layers + effect icon + logo
for e in ("husk_stalker", "hollow_warden", "drifter"):
    for suf in ("", "_glow"):
        if not os.path.exists(f"{RES}/assets/{M}/textures/entity/{e}{suf}.png"): err(f"entity texture missing {e}{suf}")
for n in (1, 2):
    if not os.path.exists(f"{RES}/assets/{M}/textures/models/armor/voidsteel_layer_{n}.png"): err(f"armor layer {n} missing")
for f in (f"{RES}/assets/{M}/textures/mob_effect/fading.png", f"{RES}/logo.png"):
    if not os.path.exists(f): err(f"missing {f}")

# 5. recipes/loot/tags reference real ids
known = set(f"{M}:{x}" for x in blocks + items)
def check_ids(path, obj):
    if isinstance(obj, dict):
        for k, v in obj.items():
            if k in ("item", "id", "name", "result") and isinstance(v, str) and v.startswith(f"{M}:") and v not in known and "entity" not in path:
                err(f"{path}: unknown id {v}")
            check_ids(path, v)
    elif isinstance(obj, list):
        for v in obj: check_ids(path, v)
    elif isinstance(obj, str) and obj.startswith(f"{M}:") and "recipe" in path and obj not in known:
        err(f"{path}: unknown id {obj}")
for p in glob.glob(f"{RES}/data/{M}/recipe/*.json") + glob.glob(f"{RES}/data/{M}/loot_table/**/*.json", recursive=True):
    check_ids(p, json.load(open(p)))
for p in glob.glob(f"{RES}/data/**/tags/item/**/*.json", recursive=True) + glob.glob(f"{RES}/data/**/tags/block/**/*.json", recursive=True):
    for v in json.load(open(p))["values"]:
        if isinstance(v, str) and v.startswith(f"{M}:") and v not in known: err(f"{p}: unknown {v}")

# 6. worldgen references
for p in glob.glob(f"{RES}/data/{M}/worldgen/**/*.json", recursive=True):
    txt = open(p).read()
    for ref in re.findall(rf'"{M}:([a-z0-9_]+)"', txt):
        if ref in blocks or ref in items or ref in ents: continue
        cands = glob.glob(f"{RES}/data/{M}/worldgen/*/{ref}.json") + glob.glob(f"{RES}/data/{M}/dimension*/{ref}.json")
        if not cands: err(f"{p}: unresolved reference {ref}")
for p in glob.glob(f"{RES}/data/{M}/dimension/*.json"):
    d = json.load(open(p))
    for need in (f"{RES}/data/{M}/dimension_type/{d['type'].split(':')[1]}.json",
                 f"{RES}/data/{M}/worldgen/noise_settings/{d['generator']['settings'].split(':')[1]}.json",
                 f"{RES}/data/{M}/worldgen/biome/{d['generator']['biome_source']['biome'].split(':')[1]}.json"):
        if not os.path.exists(need): err(f"{p}: missing {need}")
# noise_settings router completeness
need = ["barrier","fluid_level_floodedness","fluid_level_spread","lava","temperature","vegetation","continents","erosion","depth","ridges","initial_density_without_jaggedness","final_density","vein_toggle","vein_ridged","vein_gap"]
for p in glob.glob(f"{RES}/data/{M}/worldgen/noise_settings/*.json"):
    r = json.load(open(p))["noise_router"]
    for k in need:
        if k not in r: err(f"{p}: router missing {k}")
# biome features: 11 steps, referenced placed features exist
for p in glob.glob(f"{RES}/data/{M}/worldgen/biome/*.json"):
    d = json.load(open(p))
    if len(d["features"]) != 11: err(f"{p}: needs 11 feature steps")
    for step in d["features"]:
        for f in step:
            if not os.path.exists(f"{RES}/data/{M}/worldgen/placed_feature/{f.split(':')[1]}.json"): err(f"{p}: placed feature {f} missing")
    for cat in d["spawners"].values():
        for s in cat:
            if s["type"].split(":")[1] not in ents: err(f"{p}: spawner for unknown entity {s['type']}")

# 7. Java: package matches path, every referenced registry object exists
for p in glob.glob(f"{J}/**/*.java", recursive=True):
    src = open(p).read()
    m = re.search(r"^package ([\w.]+);", src, re.M)
    expected = os.path.dirname(p)[len(J) + 1:].replace("/", ".")
    if not m or m.group(1) != expected: err(f"{p}: package mismatch ({m.group(1) if m else None} vs {expected})")
all_java = "\n".join(open(p).read() for p in glob.glob(f"{J}/**/*.java", recursive=True))
for ref in set(re.findall(r"ModBlocks\.([A-Z_]+)\.get\(", all_java)):
    if ref.lower() not in blocks: err(f"Java uses ModBlocks.{ref} but it is not registered")
for ref in set(re.findall(r"ModItems\.([A-Z_]+)\.get\(", all_java)):
    if ref.lower() not in items: err(f"Java uses ModItems.{ref} but it is not registered")
for ref in set(re.findall(r"ModEntities\.([A-Z_]+)\.get\(", all_java)):
    if ref.lower() not in ents: err(f"Java uses ModEntities.{ref} but it is not registered")
# classes referenced in registries exist
for cls in set(re.findall(r"import com\.hollowexpanse\.([\w.]+);", all_java)):
    if not os.path.exists(f"{J}/com/hollowexpanse/{cls.replace('.', '/')}.java"): err(f"import of missing class {cls}")
# lang keys used in Java exist
for key in set(re.findall(r'"(message\.hollow_expanse\.[a-z_0-9]+)"', all_java)):
    if key.endswith("_") : continue
    if key not in lang: err(f"lang missing {key}")
if re.search(r'"message\.hollow_expanse\.warden_phase_" ', all_java) is None:
    for n in (2, 3):
        if f"message.hollow_expanse.warden_phase_{n}" not in lang: err(f"lang missing warden_phase_{n}")

# 8. mods.toml logo
if not os.path.exists(f"{RES}/logo.png"): err("logo.png missing")

print("\nERRORS:" if errors else "\nALL CHECKS PASSED")
for e in errors: print(" -", e)
sys.exit(1 if errors else 0)

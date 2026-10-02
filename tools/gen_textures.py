"""Procedural smooth, glowing textures for The Hollow Expanse. Run from project root: python3 tools/gen_textures.py"""
import numpy as np, os, math
from PIL import Image, ImageDraw, ImageFilter

RES = "src/main/resources"
T = f"{RES}/assets/hollow_expanse/textures"
for sub in ("block", "item", "entity", "models/armor", "mob_effect"):
    os.makedirs(f"{T}/{sub}", exist_ok=True)

# palette (locked: obsidian black / dark turquoise / dark violet, bright glow accents)
BLACK, BLACK2 = (10, 10, 15), (21, 21, 29)
TD, TM, TB, TG = (13, 51, 51), (26, 107, 107), (74, 204, 204), (124, 232, 232)
VD, VM, VG, VB = (42, 26, 61), (74, 42, 107), (154, 90, 184), (184, 138, 224)
col = lambda c: np.array(c, float)

# ------------------------------------------------------------------ tileable noise helpers
def lowpass(arr, sigma):
    n = arr.shape[0]
    f = np.fft.fft2(arr)
    fx = np.fft.fftfreq(n)[:, None]; fy = np.fft.fftfreq(n)[None, :]
    return np.real(np.fft.ifft2(f * np.exp(-2 * np.pi**2 * sigma**2 * (fx**2 + fy**2))))

def noise(seed, sigma, n=64):
    a = lowpass(np.random.default_rng(seed).standard_normal((n, n)), sigma)
    return (a - a.mean()) / (a.std() + 1e-9)

def lerp(a, b, t): return np.array(a, float) + (np.array(b, float) - np.array(a, float)) * t[..., None]
def smooth(x): x = np.clip(x, 0, 1); return x * x * (3 - 2 * x)

def dots(seed, count, tight, wide, n=64):
    rng = np.random.default_rng(seed)
    p = np.zeros((n, n))
    for _ in range(count): p[rng.integers(n), rng.integers(n)] = 1
    d, h = lowpass(p, tight), lowpass(p, wide)
    return d / d.max(), h / h.max()

def save_rgb(path, arr, alpha=None):
    a = np.clip(arr, 0, 255).astype(np.uint8)
    im = Image.fromarray(a, "RGB").convert("RGBA")
    if alpha is not None:
        im.putalpha(Image.fromarray(np.clip(alpha, 0, 255).astype(np.uint8), "L"))
    im.save(path)

# ------------------------------------------------------------------ BLOCK TEXTURES (64x64, seamless)
def stone_base():
    b = lerp(BLACK2, (32, 30, 50), smooth(noise(1, 5) * 0.35 + 0.5))
    return b + noise(2, 1.3)[..., None] * 1.6

def hollow_stone():
    patch = smooth(noise(5, 7) * 0.9 + 0.55)
    v = np.clip(np.exp(-(noise(3, 4.5) / 0.13) ** 2) + 0.5 * np.exp(-(noise(4, 3.5) / 0.09) ** 2), 0, 1) * patch
    halo = lowpass(v, 2.8) * 2.4
    return stone_base() + halo[..., None] * col((50, 190, 200)) * 0.30 + (v ** 2)[..., None] * col((190, 250, 255)) * 0.75

def hollow_soil():
    base = lerp((26, 18, 40), (50, 35, 70), smooth(noise(31, 4) * 0.4 + 0.5))
    base += noise(32, 1.3)[..., None] * 2.0
    d, h = dots(33, 9, 0.8, 2.6)
    return base + h[..., None] * col(VG) * 0.25 + d[..., None] * col(VB) * 0.6

def voidmoss_top():
    base = lerp(TD, (26, 100, 104), smooth(noise(11, 4) * 0.4 + 0.5))
    base = base + lerp((0, 0, 0), (30, 10, 40), smooth(noise(14, 6) * 0.6 + 0.2))
    base += noise(12, 1.5)[..., None] * 2.0
    d, h = dots(13, 16, 0.9, 3.2)
    return base + h[..., None] * col((60, 220, 200)) * 0.5 + d[..., None] * col((205, 255, 245))

def voidmoss_side():
    soil, moss = hollow_soil(), voidmoss_top()
    y = np.arange(64)[:, None]
    fringe = 11 + 3.0 * noise(22, 5)[0, :][None, :]
    mask = smooth((fringe - y) / 3.0 + 0.5)
    spill = lowpass(mask, 2.5)[..., None] * col((40, 190, 170)) * 0.28
    return soil * (1 - mask[..., None]) + moss * mask[..., None] + spill * (1 - mask[..., None])

def voidsteel_ore():
    base = stone_base()
    blob = smooth((noise(41, 3.0) - 0.95) / 0.35)
    core = smooth((noise(42, 2.2) - 0.4) / 0.8)
    ore = lerp(TM, TG, core) * blob[..., None]
    halo = lowpass(blob, 3.2)[..., None] * col((50, 220, 220)) * 1.6
    return base * (1 - blob[..., None]) + ore + halo + (blob * core ** 2)[..., None] * 60

def reinforced_obsidian():
    x = np.arange(64)
    d = np.minimum(np.abs(((x + 16) % 32) - 16), 99)       # distance to plate seam (period 32)
    seam = np.exp(-(d[None, :] / 1.3) ** 2) + np.exp(-(d[:, None] / 1.3) ** 2)
    seam = np.clip(seam, 0, 1)
    plate = lerp(BLACK, (34, 28, 52), smooth(noise(51, 6) * 0.35 + 0.5))
    plate += noise(52, 1.2)[..., None] * 1.5
    halo = lowpass(seam, 2.2)[..., None] * col(VG) * 1.1
    return plate * (1 - 0.6 * seam[..., None]) + halo + (seam ** 2)[..., None] * col(VB) * 0.7

def anchor_side():
    base = lerp(BLACK2, (36, 30, 58), smooth(noise(61, 5) * 0.3 + 0.5))
    x = np.arange(64)[None, :].repeat(64, 0).astype(float); y = np.arange(64)[:, None].repeat(64, 1).astype(float)
    rune = np.exp(-((x - 32) / 1.6) ** 2) * (0.5 + 0.5 * np.sin(y / 5.0))
    bands = np.exp(-((y - 12) / 1.3) ** 2) + np.exp(-((y - 52) / 1.3) ** 2)
    g = np.clip(rune + 0.8 * bands, 0, 1)
    halo = lowpass(g, 3.0)[..., None] * col(TB) * 1.5
    return base + halo + (g ** 2)[..., None] * col(TG) * 0.9

def anchor_top():
    base = lerp(BLACK2, (36, 30, 58), smooth(noise(62, 5) * 0.3 + 0.5))
    x = np.arange(64)[None, :].repeat(64, 0) - 31.5; y = np.arange(64)[:, None].repeat(64, 1) - 31.5
    r = np.sqrt(x * x + y * y)
    ring = np.exp(-((r - 20) / 1.6) ** 2) + 0.8 * np.exp(-((r - 9) / 1.4) ** 2) + np.exp(-(r / 2.5) ** 2)
    ring = np.clip(ring, 0, 1)
    halo = lowpass(ring, 3.0)[..., None] * col(TB) * 1.5
    return base + halo + (ring ** 2)[..., None] * col(TG) * 0.9

save_rgb(f"{T}/block/hollow_stone.png", hollow_stone())
save_rgb(f"{T}/block/hollow_soil.png", hollow_soil())
save_rgb(f"{T}/block/voidmoss_top.png", voidmoss_top())
save_rgb(f"{T}/block/voidmoss_side.png", voidmoss_side())
save_rgb(f"{T}/block/voidsteel_ore.png", voidsteel_ore())
save_rgb(f"{T}/block/reinforced_obsidian.png", reinforced_obsidian())
save_rgb(f"{T}/block/anchor_stone_side.png", anchor_side())
save_rgb(f"{T}/block/anchor_stone_top.png", anchor_top())

# ------------------------------------------------------------------ animated portal (32 x 32*8 frames, translucent)
F, W = 8, 32
frames = []
yy, xx = np.mgrid[0:W, 0:W].astype(float)
for f in range(F):
    t = f / F
    a = np.sin(2 * np.pi * (xx / W * 2 + yy / W * 1 + t))
    b = np.sin(2 * np.pi * (xx / W * 1 - yy / W * 2 - t * 2))
    c = np.sin(2 * np.pi * (xx / W * 3 + yy / W * 3 + t))
    v = (a + b + 0.6 * c) / 2.6 * 0.5 + 0.5
    rgb = lerp(VD, VG, smooth(v * 1.4)) + lerp((0, 0, 0), TB, smooth((v - 0.62) * 3))
    rim = np.clip((np.abs(xx - 15.5) / 15.5) ** 6 + (np.abs(yy - 15.5) / 15.5) ** 6, 0, 1)[..., None] * col(TG) * 0.4
    alpha = 150 + 80 * v
    im = Image.fromarray(np.clip(rgb + rim, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    im.putalpha(Image.fromarray(np.clip(alpha, 0, 255).astype(np.uint8), "L"))
    frames.append(im)
strip = Image.new("RGBA", (W, W * F))
for i, im in enumerate(frames): strip.paste(im, (0, i * W))
strip.save(f"{T}/block/hollow_portal.png")
open(f"{T}/block/hollow_portal.png.mcmeta", "w").write('{"animation": {"frametime": 3, "interpolate": true}}')

# ------------------------------------------------------------------ SPRITE RENDERER (supersampled, smooth, glowing)
SS, N = 8, 32 * 8
def sc(p): return (p[0] * SS, p[1] * SS)
def poly(d, pts): d.polygon([sc(p) for p in pts], fill=255)
def line(d, a, b, w):
    d.line([sc(a), sc(b)], fill=255, width=int(w * SS))
    r = w * SS / 2
    for p in (sc(a), sc(b)): d.ellipse([p[0] - r, p[1] - r, p[0] + r, p[1] + r], fill=255)
def polyline(d, pts, w):
    for a, b in zip(pts, pts[1:]): line(d, a, b, w)
def circle(d, c, r): d.ellipse([(c[0] - r) * SS, (c[1] - r) * SS, (c[0] + r) * SS, (c[1] + r) * SS], fill=255)

class Sprite:
    def __init__(self, glow=TB, glow_amt=0.7, size=32):
        self.layers, self.glow, self.glow_amt = [], glow, glow_amt
    def shape(self, fn, c1, c2, rim=None, outline=BLACK):
        m = Image.new("L", (N, N), 0); fn(ImageDraw.Draw(m)); self.layers.append((m, c1, c2, rim, outline)); return self
    def render(self):
        canvas = np.zeros((N, N, 4), float)
        union = Image.new("L", (N, N), 0)
        for m, *_ in self.layers: union = Image.fromarray(np.maximum(np.array(union), np.array(m)))
        g = np.array(union.filter(ImageFilter.MaxFilter(2 * SS + 1)).filter(ImageFilter.GaussianBlur(SS * 1.8)), float) / 255
        for k in range(3): canvas[..., k] = col(self.glow)[k]
        canvas[..., 3] = g * 255 * self.glow_amt
        for m, c1, c2, rim, outline in self.layers:
            ma = np.array(m, float) / 255
            dil = np.array(m.filter(ImageFilter.MaxFilter(SS // 2 * 2 + 1)), float) / 255
            ero = np.array(m.filter(ImageFilter.MinFilter(SS // 2 + 3)), float) / 255
            ys = np.arange(N)[:, None, None] / N
            fill = col(c1) + (col(c2) - col(c1)) * ys.repeat(N, 1)
            if rim is not None:
                edge = (ma - ero)[..., None]
                fill = fill * (1 - 0.8 * edge) + col(rim) * 0.8 * edge
            ol = (dil - ma)[..., None]
            for arr, a in ((col(outline) * np.ones((N, N, 3)), ol[..., 0]), (fill, ma)):
                a3 = a[..., None]; a_out = canvas[..., 3:4] / 255
                new_a = a3 + a_out * (1 - a3)
                canvas[..., :3] = np.where(new_a > 0, (arr * a3 + canvas[..., :3] * a_out * (1 - a3)) / np.maximum(new_a, 1e-6), canvas[..., :3])
                canvas[..., 3:4] = new_a * 255
        im = Image.fromarray(np.clip(canvas, 0, 255).astype(np.uint8), "RGBA")
        return im.resize((32, 32), Image.LANCZOS)

def metal(): return TG, TM
def dark(): return (62, 40, 92), (18, 12, 30)

def stick(s, a=(6, 26), b=(20, 12)):
    s.shape(lambda d: line(d, a, b, 2.3), (95, 70, 130), (34, 22, 52), rim=VB)

items = {}
def sprite(name, **kw):
    s = Sprite(**kw); items[name] = s; return s

s = sprite("raw_voidsteel")
s.shape(lambda d: poly(d, [(8, 22), (6, 15), (10, 9), (17, 7), (24, 10), (26, 17), (23, 24), (15, 26)]), (70, 82, 104), (22, 24, 40), rim=TB)
s.shape(lambda d: poly(d, [(12, 12), (15, 9), (17, 13), (14, 16)]), TG, TB, outline=TD)
s.shape(lambda d: poly(d, [(19, 17), (22, 14), (24, 18), (21, 21)]), TG, TB, outline=TD)
s.shape(lambda d: poly(d, [(10, 19), (13, 18), (13, 22), (10, 22)]), TB, TM, outline=TD)

s = sprite("voidsteel_ingot")
s.shape(lambda d: poly(d, [(5, 20), (11, 11), (27, 11), (21, 20)]), TG, TB, rim=(220, 255, 255))
s.shape(lambda d: poly(d, [(5, 20), (21, 20), (21, 26), (5, 26)]), TM, TD, rim=TB)
s.shape(lambda d: poly(d, [(21, 20), (27, 11), (27, 17), (21, 26)]), (20, 80, 84), (8, 30, 34), rim=TB)
s.shape(lambda d: line(d, (9, 17), (22, 17), 0.9), (230, 255, 255), (230, 255, 255), outline=TB)

s = sprite("void_ignition_core", glow=VG, glow_amt=0.9)
s.shape(lambda d: circle(d, (16, 16), 10), (96, 58, 140), (26, 14, 44), rim=VB)
s.shape(lambda d: circle(d, (16, 16), 6.5), (30, 20, 52), (60, 34, 90), rim=VG)
s.shape(lambda d: [poly(d, [(16, 4.5), (17.5, 8), (16, 11), (14.5, 8)]), poly(d, [(16, 27.5), (17.5, 24), (16, 21), (14.5, 24)])], TG, TB, outline=VD)
s.shape(lambda d: circle(d, (16, 16), 3), (230, 255, 255), TB, outline=TM)

s = sprite("void_shard", glow=VG, glow_amt=0.9)
s.shape(lambda d: poly(d, [(16, 2), (23, 12), (20, 30), (11, 26), (9, 11)]), VB, VM, rim=(235, 215, 255), outline=VD)
s.shape(lambda d: poly(d, [(16, 5), (19, 12), (17, 24), (13, 20), (13, 12)]), (150, 235, 240), TM, outline=VM)

s = sprite("hollow_sigil", glow=VG, glow_amt=0.9)
s.shape(lambda d: poly(d, [(16, 2), (29, 16), (16, 30), (3, 16)]), (60, 38, 90), (16, 10, 28), rim=VB)
s.shape(lambda d: (line(d, (16, 8), (16, 24), 1.3), line(d, (9, 16), (23, 16), 1.3), circle(d, (16, 16), 4.2)), TG, TB, outline=VD)
s.shape(lambda d: circle(d, (16, 16), 2.0), (20, 14, 40), (20, 14, 40), outline=TB)

s = sprite("wardens_core", glow=TB, glow_amt=0.9)
s.shape(lambda d: poly(d, [(16, 2), (27, 9), (27, 22), (16, 30), (5, 22), (5, 9)]), (30, 24, 50), BLACK, rim=VG)
s.shape(lambda d: poly(d, [(16, 7), (23, 11), (23, 20), (16, 25), (9, 20), (9, 11)]), TG, TM, rim=(230, 255, 255), outline=VD)
s.shape(lambda d: circle(d, (16, 16), 3.5), (255, 255, 255), TG, outline=TB)

def heart(d):
    pts = []
    for i in range(80):
        t = i / 80 * 2 * math.pi
        x = 16 * math.sin(t) ** 3
        y = 13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t)
        pts.append((16 + x * 0.78, 14 - y * 0.78))
    poly(d, pts)
s = sprite("void_heart", glow=VG, glow_amt=1.0)
s.shape(heart, (130, 70, 170), (28, 12, 46), rim=VB, outline=BLACK)
s.shape(lambda d: poly(d, [(9, 9), (12, 8), (13, 11), (10, 12)]), (240, 220, 255), VB, outline=VM)
s.shape(lambda d: circle(d, (16, 16), 2.4), TG, TB, outline=VM)

def hook(name, big):
    sp = sprite(name, glow=(VG if big else TB), glow_amt=0.8)
    sp.shape(lambda d: polyline(d, [(5, 27), (9, 23), (8, 19), (13, 17), (15, 13)], 1.5), (150, 140, 120), (70, 60, 50), rim=(210, 200, 180), outline=BLACK2)
    sp.shape(lambda d: polyline(d, [(15, 14), (19, 9), (24, 8), (28, 12), (26, 17)], 3.0 if big else 2.6),
             TG if big else (150, 190, 200), TM if big else (60, 100, 110), rim=(240, 255, 255), outline=TD)
    if big:
        sp.shape(lambda d: circle(d, (24, 8.5), 1.6), VB, VG, outline=VD)
hook("tether_hook", False); hook("voidsteel_tether_hook", True)

def sword(name, c1, c2, rim, glow, guard=VM):
    sp = sprite(name, glow=glow, glow_amt=0.75)
    sp.shape(lambda d: line(d, (6, 26), (11, 21), 2.2), (95, 70, 130), (34, 22, 52), rim=VB)
    sp.shape(lambda d: polyline(d, [(9, 16), (16, 23)], 2.6), guard, (24, 14, 40), rim=VB)
    sp.shape(lambda d: poly(d, [(11.5, 19.5), (25.5, 3.5), (28.5, 6.5), (14.5, 22.5)]), c1, c2, rim=rim)
    sp.shape(lambda d: line(d, (14, 18), (26, 6), 0.8), (235, 255, 255), (235, 255, 255), outline=c2)
sword("voidsteel_sword", TG, TM, (225, 255, 255), TB)
sword("hollowforged_sword", (196, 160, 240), (86, 40, 130), (240, 220, 255), VG)

def pickaxe(name, c1, c2, rim, glow):
    sp = sprite(name, glow=glow, glow_amt=0.75)
    stick(sp, (6, 27), (21, 12))
    sp.shape(lambda d: polyline(d, [(8, 12), (12, 7), (19, 5), (25, 8), (28, 14)], 3.3), c1, c2, rim=rim)
pickaxe("voidsteel_pickaxe", TG, TM, (225, 255, 255), TB)
pickaxe("hollowforged_pickaxe", (196, 160, 240), (86, 40, 130), (240, 220, 255), VG)

s = sprite("voidsteel_axe", glow=TB, glow_amt=0.75); stick(s, (6, 27), (21, 11))
s.shape(lambda d: poly(d, [(15, 9), (22, 3), (28, 6), (28, 14), (23, 15), (18, 13)]), TG, TM, rim=(225, 255, 255))
s = sprite("voidsteel_shovel", glow=TB, glow_amt=0.75); stick(s, (5, 27), (20, 12))
s.shape(lambda d: poly(d, [(19, 11), (23, 4), (28, 4), (29, 9), (25, 14)]), TG, TM, rim=(225, 255, 255))
s = sprite("voidsteel_hoe", glow=TB, glow_amt=0.75); stick(s, (6, 27), (20, 11))
s.shape(lambda d: polyline(d, [(13, 8), (20, 6), (27, 9)], 3.0), TG, TM, rim=(225, 255, 255))

s = sprite("void_mace", glow=VG, glow_amt=0.85)
s.shape(lambda d: line(d, (6, 27), (19, 14), 2.4), (95, 70, 130), (34, 22, 52), rim=VB)
spikes = lambda d: [poly(d, [(23 + 8 * math.cos(a), 10 + 8 * math.sin(a)),
                              (23 + 4.5 * math.cos(a + 0.28), 10 + 4.5 * math.sin(a + 0.28)),
                              (23 + 4.5 * math.cos(a - 0.28), 10 + 4.5 * math.sin(a - 0.28))]) for a in [i * math.pi / 4 for i in range(8)]]
s.shape(spikes, (170, 140, 200), (50, 30, 80), rim=VB)
s.shape(lambda d: circle(d, (23, 10), 5.2), (70, 48, 100), (20, 12, 34), rim=VG)
s.shape(lambda d: circle(d, (23, 10), 2.3), TG, TB, outline=VD)

def armor(name, polys, accent):
    sp = sprite(name, glow=TB, glow_amt=0.55)
    sp.shape(lambda d: [poly(d, p) for p in polys], (70, 52, 104), (16, 12, 28), rim=TB)
    sp.shape(accent, TG, TB, outline=VD)
armor("voidsteel_helmet", [[(6, 25), (6, 13), (10, 6), (22, 6), (26, 13), (26, 25), (20, 25), (20, 18), (12, 18), (12, 25)]],
      lambda d: (line(d, (16, 7), (16, 14), 1.4), line(d, (9, 15), (23, 15), 1.0)))
armor("voidsteel_chestplate", [[(3, 9), (10, 5), (13, 8), (19, 8), (22, 5), (29, 9), (29, 16), (25, 16), (25, 28), (7, 28), (7, 16), (3, 16)]],
      lambda d: (circle(d, (16, 16), 3.2), line(d, (16, 10), (16, 24), 1.0)))
armor("voidsteel_leggings", [[(7, 5), (25, 5), (26, 28), (18, 28), (16, 14), (14, 28), (6, 28)]],
      lambda d: (line(d, (9, 8), (23, 8), 1.3), line(d, (10, 12), (9, 25), 0.9), line(d, (22, 12), (23, 25), 0.9)))
armor("voidsteel_boots", [[(4, 15), (13, 15), (13, 22), (15, 25), (15, 28), (3, 28)], [(18, 15), (27, 15), (27, 22), (29, 25), (29, 28), (17, 28)]],
      lambda d: (line(d, (5, 26), (14, 26), 1.1), line(d, (19, 26), (28, 26), 1.1)))

for name, sp in items.items():
    sp.render().save(f"{T}/item/{name}.png")

# ------------------------------------------------------------------ Lumen Bloom (32x32 cutout, glowing)
sb = Sprite(glow=TB, glow_amt=0.9)
sb.shape(lambda d: polyline(d, [(16, 31), (16, 22), (17, 17)], 1.6), (40, 130, 120), (14, 60, 56), rim=TB, outline=TD)
sb.shape(lambda d: [poly(d, [(16, 17), (8, 14), (6, 7), (12, 10)]), poly(d, [(17, 17), (25, 14), (27, 7), (21, 10)]),
                    poly(d, [(16.5, 16), (13, 5), (16.5, 1.5), (20, 5)])], (150, 150, 255), VM, rim=VB, outline=VD)
sb.shape(lambda d: circle(d, (16.5, 12), 3.6), (240, 255, 255), TG, outline=TB)
sb.render().save(f"{T}/block/lumen_bloom.png")

# ------------------------------------------------------------------ Fading effect icon (18x18)
ic = np.zeros((18, 18, 4)); yy2, xx2 = np.mgrid[0:18, 0:18]; r = np.sqrt((xx2 - 8.5) ** 2 + (yy2 - 8.5) ** 2)
for k in range(3): ic[..., k] = lerp(VD, VB, smooth(1 - r / 8.0))[..., k]
ic[..., 3] = np.clip((8.2 - r) * 160, 0, 255) * np.clip(1.2 - 0.12 * np.abs(yy2 - 8.5) * (xx2 > 8) , 0.15, 1)
Image.fromarray(ic.astype(np.uint8), "RGBA").save(f"{T}/mob_effect/fading.png")

# ------------------------------------------------------------------ ARMOR LAYERS (64x32)
def armor_layer(seed, accent_rows):
    base = lerp((20, 14, 34), (52, 36, 84), smooth(noise(seed, 4) * 0.4 + 0.5))
    v = np.clip(np.exp(-(noise(seed + 1, 3.5) / 0.14) ** 2), 0, 1)
    halo = lowpass(v, 2.4) * 2.2
    out = base + halo[..., None] * col(TB) * 0.35 + (v ** 2)[..., None] * col(TG) * 0.8
    return out[:32]
for i, seed in enumerate((71, 81), start=1):
    save_rgb(f"{T}/models/armor/voidsteel_layer_{i}.png", armor_layer(seed, None))

# ------------------------------------------------------------------ ENTITY TEXTURES
def humanoid_mask():
    m = np.zeros((64, 64))
    for x0, y0, x1, y1 in ((0, 0, 32, 16), (16, 16, 40, 32), (0, 16, 16, 32), (40, 16, 56, 32), (32, 48, 48, 64), (16, 48, 32, 64)):
        m[y0:y1, x0:x1] = 255
    return m

def humanoid_glow(seed, eye, chest_core, seams, big):
    g = np.zeros((64, 64, 4))
    def blob(cx, cy, rx, ry, color, strength=1.0, power=1.0):
        yy, xx = np.mgrid[0:64, 0:64]
        a = np.clip(1 - np.sqrt(((xx + 0.5 - cx) / rx) ** 2 + ((yy + 0.5 - cy) / ry) ** 2), 0, 1) ** power * strength
        for k in range(3): g[..., k] = np.where(a > g[..., 3] / 255, color[k], g[..., k])
        g[..., 3] = np.maximum(g[..., 3], a * 255)
    # eyes on head front (8..16, 8..16)
    es = 1.6 if big else 1.2
    blob(10.5, 12.0, es + 0.6, es, eye, 1.0, 0.5); blob(13.5, 12.0, es + 0.6, es, eye, 1.0, 0.5)
    if chest_core:                                          # torso front (20..28, 20..32)
        blob(24, 25.5, 3.4 if big else 2.2, 4.6 if big else 2.8, chest_core, 1.0, 0.45)
    for (cx, y0, y1) in seams:                              # thin vertical seams on limbs
        for y in range(y0, y1): g[y, cx] = (*TB, 190)
    return g

def entity_base(seed, tint_a, tint_b):
    base = lerp(tint_a, tint_b, smooth(noise(seed, 3) * 0.4 + 0.5))
    v = np.clip(np.exp(-(noise(seed + 1, 3.0) / 0.12) ** 2), 0, 1)
    out = base + lowpass(v, 2.0)[..., None] * col(TB) * 0.4 + (v ** 2)[..., None] * col(TG) * 0.55
    return out

def save_entity(name, base, glow, mask=None):
    im = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    if mask is not None: im.putalpha(Image.fromarray(mask.astype(np.uint8), "L"))
    im.save(f"{T}/entity/{name}.png")
    Image.fromarray(glow.astype(np.uint8), "RGBA").save(f"{T}/entity/{name}_glow.png")

mask = humanoid_mask()
save_entity("husk_stalker", entity_base(91, (14, 12, 22), (40, 28, 58)),
            humanoid_glow(91, VB, VG, [(46, 21, 31), (6, 21, 31)], False), mask)
w_base = entity_base(95, (10, 10, 16), (34, 28, 54))
save_entity("hollow_warden", w_base, humanoid_glow(95, TG, TB, [(46, 21, 31), (6, 21, 31), (17, 21, 31)], True), mask)

# Drifter: ghast layout (64x32). Whole sheet is body; glow = faint inner light + bright eyes/mouth.
dbase = lerp(TD, (40, 40, 110), smooth(noise(99, 5)[:32] * 0.5 + 0.5))
dbase = dbase + lerp((0, 0, 0), (40, 0, 60), smooth(noise(98, 8)[:32] * 0.5 + 0.4))
dglow = np.zeros((32, 64, 4))
yy3, xx3 = np.mgrid[0:32, 0:64]
body = ((xx3 >= 0) & (xx3 < 64) & (yy3 >= 0) & (yy3 < 32))
dglow[..., :3] = col(TB); dglow[..., 3] = 55
for cx in (21.5, 26.5):
    a = np.clip(1 - np.sqrt(((xx3 - cx) / 2.6) ** 2 + ((yy3 - 24.5) / 3.6) ** 2), 0, 1) ** 0.5
    for k in range(3): dglow[..., k] = np.where(a > 0.05, TG[k] if k != 2 else 255, dglow[..., k])
    dglow[..., 3] = np.maximum(dglow[..., 3], a * 255)
Image.fromarray(np.clip(dbase, 0, 255).astype(np.uint8), "RGB").convert("RGBA").save(f"{T}/entity/drifter.png")
Image.fromarray(dglow.astype(np.uint8), "RGBA").save(f"{T}/entity/drifter_glow.png")

# ------------------------------------------------------------------ LOGO (512x512) + CurseForge avatar
def make_logo(size=512):
    s = size; img = np.zeros((s, s, 3))
    yy, xx = np.mgrid[0:s, 0:s] / s
    img = lerp((5, 5, 14), (28, 14, 52), smooth(yy * 1.1))
    rng = np.random.default_rng(7)
    star = np.zeros((s, s))
    for _ in range(140): star[rng.integers(0, int(s * 0.8)), rng.integers(0, s)] = rng.random()
    img = img + lowpass_big(star, 0.8)[..., None] * 255 * 2.2
    canvas = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    # floating island silhouette with aura
    layer = Image.new("L", (s, s), 0); d = ImageDraw.Draw(layer)
    d.ellipse([s * 0.16, s * 0.42, s * 0.84, s * 0.54], fill=255)
    d.polygon([(s * 0.18, s * 0.49), (s * 0.82, s * 0.49), (s * 0.66, s * 0.70), (s * 0.58, s * 0.86), (s * 0.5, s * 0.96), (s * 0.42, s * 0.84), (s * 0.34, s * 0.68)], fill=255)
    glow = layer.filter(ImageFilter.MaxFilter(15)).filter(ImageFilter.GaussianBlur(s * 0.045))
    gl = Image.new("RGBA", (s, s), (*TB, 0)); gl.putalpha(glow.point(lambda v: int(v * 0.75)))
    canvas = Image.alpha_composite(canvas, gl)
    fill = Image.new("RGBA", (s, s), (14, 12, 22, 255))
    grad = np.zeros((s, s, 4), np.uint8)
    for k, c in enumerate((22, 18, 40)): grad[..., k] = (c + (np.linspace(0, 1, s)[:, None] * 0)).astype(np.uint8)
    grad[..., 3] = 255
    isle = Image.fromarray(grad, "RGBA"); canvas.paste(isle, (0, 0), layer)
    top = Image.new("L", (s, s), 0); ImageDraw.Draw(top).ellipse([s * 0.16, s * 0.42, s * 0.84, s * 0.54], fill=255)
    moss = Image.new("RGBA", (s, s), (*TM, 255)); canvas.paste(moss, (0, 0), top)
    edge = top.filter(ImageFilter.MaxFilter(5)); rim = Image.new("RGBA", (s, s), (*TG, 255))
    ring = Image.fromarray(np.clip(np.array(edge, int) - np.array(top.filter(ImageFilter.MinFilter(5)), int), 0, 255).astype(np.uint8), "L")
    canvas.paste(rim, (0, 0), ring)
    # portal ring above the island
    ring2 = Image.new("L", (s, s), 0); dd = ImageDraw.Draw(ring2)
    dd.ellipse([s * 0.36, s * 0.10, s * 0.64, s * 0.46], outline=255, width=int(s * 0.022))
    rg = ring2.filter(ImageFilter.GaussianBlur(s * 0.02))
    pg = Image.new("RGBA", (s, s), (*VB, 0)); pg.putalpha(rg.point(lambda v: min(255, int(v * 2.2))))
    canvas = Image.alpha_composite(canvas, pg)
    sharp = Image.new("RGBA", (s, s), (*VB, 255)); canvas.paste(sharp, (0, 0), ring2)
    return canvas.convert("RGB")

def lowpass_big(arr, sigma):
    return lowpass(arr, sigma)

os.makedirs("curseforge", exist_ok=True)
logo = make_logo(512)
logo.save(f"{RES}/logo.png")
logo.resize((256, 256), Image.LANCZOS).save("curseforge/project_avatar_256.png")
print("textures written:", len(items), "item sprites")

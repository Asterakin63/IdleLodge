"""Generates the Masonic pixel art for Idle Lodge.
Usage: python tools/make_art.py app/src/main <preview_dir>   (needs Pillow)
Writes res/drawable/skill_*.png (32x32), assets/sprites/characters/regalia/apron.png,
and preview sheets (upscaled) in <preview_dir>."""
import math
import sys
from pathlib import Path
from PIL import Image, ImageDraw

MAIN, PREVIEW = Path(sys.argv[1]), Path(sys.argv[2])

OUT = (38, 28, 44, 255)
GOLD, GOLD_D, GOLD_L = (226, 184, 72, 255), (168, 120, 38, 255), (250, 226, 140, 255)
SILV, SILV_D, SILV_L = (196, 202, 214, 255), (124, 130, 148, 255), (236, 240, 248, 255)
STONE, STONE_D, STONE_L = (168, 162, 150, 255), (114, 108, 100, 255), (206, 200, 188, 255)
WOOD, WOOD_D, WOOD_L = (150, 98, 58, 255), (98, 62, 38, 255), (190, 134, 82, 255)
WHITE, CREAM = (244, 240, 230, 255), (222, 210, 182, 255)
BLUE, BLUE_D, BLUE_L = (52, 84, 160, 255), (32, 50, 104, 255), (96, 136, 210, 255)
RED, RED_D, RED_L = (192, 52, 52, 255), (124, 30, 38, 255), (232, 100, 90, 255)
GREEN, GREEN_D, GREEN_L = (74, 144, 72, 255), (40, 92, 52, 255), (124, 188, 96, 255)
BLACK = (40, 40, 48, 255)
FLAME, FLAME_L = (240, 140, 40, 255), (255, 220, 110, 255)
GLASS, GLASS_D = (150, 210, 220, 255), (90, 150, 170, 255)


def canvas(size=32):
    im = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    return im, ImageDraw.Draw(im)


def outline(im, color=OUT):
    """Adds a 1px outline around every opaque pixel (4-neighbourhood)."""
    px = im.load()
    w, h = im.size
    add = []
    for y in range(h):
        for x in range(w):
            if px[x, y][3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] and px[nx, ny] != color:
                    add.append((x, y))
                    break
    for p in add:
        px[p] = color
    return im


def line(d, pts, fill, width=1):
    d.line(pts, fill=fill, width=width)


# ---------------------------------------------------------------- skill icons

def icon_mining():  # Quarrying: rough ashlar + pick
    im, d = canvas()
    d.polygon([(5, 14), (11, 9), (21, 10), (26, 15), (25, 25), (17, 28), (7, 26), (4, 20)], fill=STONE)
    d.polygon([(5, 14), (11, 9), (21, 10), (16, 15), (8, 17)], fill=STONE_L)
    d.polygon([(25, 25), (17, 28), (16, 20), (26, 15)], fill=STONE_D)
    line(d, [(10, 21), (13, 23)], STONE_D); line(d, [(19, 13), (21, 16)], STONE_D)
    line(d, [(15, 4), (28, 12)], WOOD, 2)
    d.polygon([(22, 3), (27, 3), (30, 6), (29, 8), (26, 5)], fill=SILV)
    d.point([(27, 4)], fill=SILV_L)
    return outline(im)


def icon_woodcutting():  # Timberwork: cedar of Lebanon
    im, d = canvas()
    d.rectangle([14, 22, 17, 29], fill=WOOD); d.line([(14, 22), (14, 29)], fill=WOOD_D)
    for y, hw in ((18, 12), (12, 10), (7, 7), (3, 4)):
        d.polygon([(15.5 - hw, y + 4), (15.5, y - 1), (15.5 + hw, y + 4)], fill=GREEN_D)
        d.polygon([(15.5 - hw + 1, y + 3), (15.5, y), (15.5 + hw - 1, y + 3)], fill=GREEN)
        d.line([(15.5 - hw + 3, y + 2), (13, y + 1)], fill=GREEN_L)
    return outline(im)


def icon_fishing():  # Provisioning: amphora
    im, d = canvas()
    d.ellipse([8, 9, 23, 26], fill=WOOD)
    d.polygon([(11, 24), (20, 24), (17, 30), (14, 30)], fill=WOOD)
    d.rectangle([13, 4, 18, 10], fill=WOOD)
    d.rectangle([12, 3, 19, 4], fill=WOOD_D)
    d.arc([7, 5, 14, 15], 140, 300, fill=WOOD_D, width=2)
    d.arc([17, 5, 24, 15], 240, 40, fill=WOOD_D, width=2)
    d.ellipse([10, 11, 14, 17], fill=WOOD_L)
    d.line([(9, 18), (22, 18)], fill=GOLD); d.line([(9, 20), (22, 20)], fill=GOLD_D)
    return outline(im)


def icon_farming():  # Cultivation: ear of wheat
    im, d = canvas()
    d.line([(9, 30), (19, 8)], fill=GOLD_D, width=2)
    for i in range(6):
        t = i / 6
        cx, cy = 18 - 9 * t + 0, 8 + 19 * t
        cx, cy = 19 - 9 * (i / 6), 7 + 17 * (i / 6)
        d.ellipse([cx - 6, cy - 2, cx - 1, cy + 2], fill=GOLD)
        d.ellipse([cx + 1, cy - 1, cx + 6, cy + 3], fill=GOLD)
        d.point([(int(cx - 4), int(cy - 1)), (int(cx + 3), int(cy))], fill=GOLD_L)
    d.line([(20, 6), (24, 1)], fill=GOLD_D); d.line([(18, 5), (16, 0)], fill=GOLD_D)
    d.polygon([(10, 26), (4, 22), (6, 21), (11, 24)], fill=GREEN)
    return outline(im)


def icon_thieving():  # Charity: the Widow's alms box
    im, d = canvas()
    d.rectangle([5, 12, 26, 28], fill=WOOD)
    d.rectangle([5, 12, 26, 14], fill=WOOD_L)
    d.rectangle([5, 26, 26, 28], fill=WOOD_D)
    d.rectangle([12, 12, 19, 13], fill=BLACK)
    d.rectangle([8, 17, 23, 24], fill=WOOD_D)
    d.rectangle([9, 18, 22, 23], fill=GOLD_D)
    d.rectangle([14, 19, 17, 22], fill=GOLD)
    d.ellipse([12, 3, 19, 10], fill=GOLD)
    d.ellipse([13, 4, 18, 9], fill=GOLD_L)
    d.line([(15, 5), (15, 8)], fill=GOLD_D)
    return outline(im)


def icon_smithing():  # Founding: the bronze pillar (Jachin) with pomegranates
    im, d = canvas()
    d.rectangle([11, 10, 20, 26], fill=GOLD_D)
    d.rectangle([12, 10, 17, 26], fill=GOLD)
    d.line([(13, 10), (13, 26)], fill=GOLD_L)
    d.rectangle([8, 26, 23, 29], fill=GOLD_D); d.rectangle([9, 26, 22, 27], fill=GOLD)
    d.rectangle([9, 7, 22, 9], fill=GOLD_D); d.rectangle([9, 7, 22, 7], fill=GOLD)
    d.ellipse([8, 1, 15, 8], fill=RED); d.ellipse([16, 1, 23, 8], fill=RED)
    d.ellipse([12, 0, 19, 6], fill=RED_L)
    d.point([(10, 3), (18, 3), (14, 2)], fill=WHITE)
    return outline(im)


def icon_cooking():  # Banqueting: the "canon" (lodge goblet) and bread
    im, d = canvas()
    d.ellipse([3, 19, 18, 29], fill=WOOD_L)
    d.ellipse([4, 19, 17, 25], fill=CREAM)
    d.line([(7, 21), (9, 23)], fill=WOOD); d.line([(11, 20), (13, 22)], fill=WOOD)
    d.polygon([(16, 4), (28, 4), (26, 13), (18, 13)], fill=SILV)
    d.polygon([(17, 5), (27, 5), (26, 8), (18, 8)], fill=RED)
    d.line([(18, 6), (18, 12)], fill=SILV_L)
    d.rectangle([21, 13, 23, 23], fill=SILV_D)
    d.rectangle([17, 24, 27, 26], fill=SILV)
    return outline(im)


def icon_fletching():  # Writing: quill and inkwell
    im, d = canvas()
    d.rectangle([5, 21, 16, 29], fill=BLACK); d.rectangle([6, 22, 15, 23], fill=BLUE_D)
    d.rectangle([8, 18, 13, 21], fill=BLACK)
    d.polygon([(11, 20), (13, 18), (28, 2), (26, 9), (19, 14)], fill=WHITE)
    d.polygon([(13, 18), (28, 2), (22, 6)], fill=CREAM)
    d.line([(11, 20), (27, 3)], fill=SILV_D)
    return outline(im)


def icon_crafting():  # Jewelcraft: officer's jewel on a collar ribbon
    im, d = canvas()
    d.polygon([(4, 1), (10, 1), (16, 12), (22, 1), (28, 1), (18, 17), (14, 17)], fill=BLUE)
    d.line([(6, 2), (15, 14)], fill=BLUE_L); d.line([(25, 2), (17, 14)], fill=BLUE_D)
    d.ellipse([9, 15, 23, 29], fill=GOLD)
    d.ellipse([11, 17, 21, 27], fill=GOLD_D)
    d.polygon([(16, 18), (20, 22), (16, 26), (12, 22)], fill=GLASS)
    d.polygon([(16, 18), (20, 22), (16, 22)], fill=WHITE)
    d.point([(11, 17)], fill=GOLD_L)
    return outline(im)


def icon_firemaking():  # Illumination: three candles
    im, d = canvas()
    for x, top in ((5, 14), (13, 9), (21, 14)):
        d.rectangle([x + 1, top, x + 5, 26], fill=WHITE)
        d.line([(x + 1, top), (x + 1, 26)], fill=CREAM)
        d.ellipse([x + 1, top - 7, x + 5, top - 1], fill=FLAME)
        d.ellipse([x + 2, top - 5, x + 4, top - 2], fill=FLAME_L)
        d.point([(x + 3, top)], fill=BLACK)
    d.rectangle([3, 26, 28, 29], fill=GOLD); d.line([(3, 29), (28, 29)], fill=GOLD_D)
    return outline(im)


def icon_runecrafting():  # Symbolism: radiant delta
    im, d = canvas()
    for a in range(0, 360, 30):
        r = math.radians(a)
        d.line([(16, 18), (16 + 15 * math.cos(r), 18 + 15 * math.sin(r))], fill=GOLD_L)
    d.polygon([(16, 4), (29, 27), (3, 27)], fill=GOLD)
    d.polygon([(16, 9), (25, 25), (7, 25)], fill=GOLD_D)
    d.polygon([(16, 13), (21, 23), (11, 23)], fill=GOLD_L)
    d.rectangle([15, 17, 16, 20], fill=BLUE_D)
    return outline(im)


def icon_herblore():  # Alchemy: flask
    im, d = canvas()
    d.rectangle([13, 3, 18, 12], fill=GLASS)
    d.rectangle([12, 2, 19, 3], fill=WOOD)
    d.ellipse([5, 11, 26, 30], fill=GLASS)
    d.chord([5, 11, 26, 30], 10, 170, fill=GREEN)
    d.chord([7, 13, 24, 28], 20, 160, fill=GREEN_L)
    d.ellipse([9, 14, 12, 18], fill=WHITE)
    d.point([(14, 22), (18, 20), (16, 25)], fill=WHITE)
    return outline(im)


def icon_construction():  # Architecture: arch with keystone
    im, d = canvas()
    d.rectangle([3, 14, 9, 29], fill=STONE); d.rectangle([22, 14, 28, 29], fill=STONE)
    d.pieslice([3, 3, 28, 26], 180, 360, fill=STONE)
    d.pieslice([9, 9, 22, 22], 180, 360, fill=(0, 0, 0, 0))
    d.rectangle([10, 15, 21, 29], fill=(0, 0, 0, 0))
    d.polygon([(13, 3), (18, 3), (17, 9), (14, 9)], fill=GOLD)
    for y in (18, 23):
        d.line([(3, y), (9, y)], fill=STONE_D); d.line([(22, y), (28, y)], fill=STONE_D)
    d.line([(5, 9), (10, 12)], fill=STONE_D); d.line([(26, 9), (21, 12)], fill=STONE_D)
    d.line([(3, 14), (3, 29)], fill=STONE_L); d.line([(22, 14), (22, 29)], fill=STONE_L)
    return outline(im)


def icon_prayer():  # Reflection: memento mori skull
    im, d = canvas()
    d.ellipse([6, 3, 25, 21], fill=WHITE)
    d.rectangle([10, 17, 21, 25], fill=WHITE)
    d.rectangle([10, 23, 21, 25], fill=CREAM)
    d.ellipse([9, 11, 14, 16], fill=BLACK); d.ellipse([17, 11, 22, 16], fill=BLACK)
    d.polygon([(15, 17), (16, 17), (17, 20), (14, 20)], fill=BLACK)
    for x in (12, 15, 18):
        d.line([(x, 23), (x, 25)], fill=STONE_D)
    d.line([(8, 7), (11, 5)], fill=SILV_L)
    return outline(im)


def icon_mercantile():  # Commerce: balance
    im, d = canvas()
    d.rectangle([15, 4, 16, 26], fill=GOLD_D)
    d.rectangle([10, 26, 21, 28], fill=GOLD)
    d.line([(4, 8), (27, 8)], fill=GOLD, width=2)
    d.ellipse([14, 2, 17, 5], fill=GOLD_L)
    for cx in (6, 25):
        d.line([(cx, 9), (cx - 4, 18)], fill=GOLD_D); d.line([(cx, 9), (cx + 4, 18)], fill=GOLD_D)
        d.chord([cx - 5, 14, cx + 5, 22], 0, 180, fill=GOLD)
    return outline(im)


def icon_agility():  # Ritual: mosaic pavement
    im, d = canvas()
    for y in range(32):
        for x in range(32):
            dx, dy = x - 15.5, y - 15.5
            if abs(dx) + abs(dy) <= 13:
                u, v = math.floor((dx + dy + 14) / 7), math.floor((dx - dy + 14) / 7)
                im.putpixel((x, y), WHITE if (u + v) % 2 == 0 else BLACK)
            elif abs(dx) + abs(dy) <= 14.5:
                im.putpixel((x, y), GOLD)
    return outline(im)


def icon_slayer():  # Vigilance: the Tyler's flaming sword
    im, d = canvas()
    pts = [(16, 1)]
    for i, y in enumerate(range(4, 21, 3)):
        pts.append((18 if i % 2 == 0 else 19, y))
    pts += [(17, 21), (14, 21)]
    for i, y in reversed(list(enumerate(range(4, 21, 3)))):
        pts.append((13 if i % 2 == 0 else 12, y))
    d.polygon(pts, fill=SILV)
    d.line([(15, 3), (15, 20)], fill=SILV_L)
    d.rectangle([9, 21, 22, 23], fill=GOLD)
    d.rectangle([14, 24, 17, 28], fill=WOOD)
    d.ellipse([13, 28, 18, 31], fill=GOLD)
    return outline(im)


def icon_attack():  # Rigor: the 24-inch gauge
    im, d = canvas()
    d.polygon([(3, 24), (24, 3), (29, 8), (8, 29)], fill=WOOD_L)
    d.line([(3, 24), (24, 3)], fill=CREAM)
    d.line([(8, 29), (29, 8)], fill=WOOD)
    for i in range(1, 12):
        x, y = 3 + i * 21 / 12, 24 - i * 21 / 12
        n = 3 if i % 3 == 0 else 2
        d.line([(x + 1, y + 1), (x + n, y + n)], fill=WOOD_D)
    return outline(im)


def icon_strength():  # Strength: the gavel
    im, d = canvas()
    d.line([(8, 29), (20, 13)], fill=WOOD, width=3)
    d.line([(8, 28), (19, 13)], fill=WOOD_L)
    d.polygon([(12, 9), (22, 1), (30, 11), (20, 19)], fill=WOOD_D)
    d.polygon([(13, 9), (22, 2), (25, 6), (16, 13)], fill=WOOD)
    d.line([(14, 9), (22, 3)], fill=WOOD_L)
    return outline(im)


def icon_defense():  # Temperance: the level
    im, d = canvas()
    d.rectangle([3, 25, 28, 28], fill=WOOD); d.line([(3, 25), (28, 25)], fill=WOOD_L)
    d.line([(16, 4), (5, 25)], fill=WOOD_D, width=3)
    d.line([(16, 4), (27, 25)], fill=WOOD_D, width=3)
    d.line([(16, 5), (6, 24)], fill=WOOD_L)
    d.line([(16, 6), (16, 20)], fill=SILV_D)
    d.ellipse([14, 19, 18, 23], fill=GOLD)
    d.ellipse([14, 2, 18, 6], fill=GOLD)
    return outline(im)


def icon_hitpoints():  # Vitality: flaming heart
    im, d = canvas()
    d.ellipse([4, 9, 16, 21], fill=RED); d.ellipse([15, 9, 27, 21], fill=RED)
    d.polygon([(5, 17), (26, 17), (16, 29)], fill=RED)
    d.ellipse([7, 11, 11, 15], fill=RED_L)
    d.polygon([(19, 26), (26, 17), (22, 22)], fill=RED_D)
    d.polygon([(12, 10), (16, 1), (20, 10), (16, 13)], fill=FLAME)
    d.polygon([(14, 10), (16, 5), (18, 10)], fill=FLAME_L)
    return outline(im)


def icon_ranged():  # Eloquence: the scroll (a "planche")
    im, d = canvas()
    d.polygon([(6, 7), (25, 7), (25, 25), (6, 25)], fill=CREAM)
    d.rectangle([4, 4, 27, 8], fill=WOOD_L); d.line([(4, 8), (27, 8)], fill=WOOD)
    d.rectangle([4, 24, 27, 28], fill=WOOD_L); d.line([(4, 28), (27, 28)], fill=WOOD)
    for y in (11, 14, 17, 20):
        d.line([(9, y), (22 - (y % 3) * 2, y)], fill=STONE_D)
    d.ellipse([18, 18, 23, 23], fill=RED)
    return outline(im)


def icon_magic():  # Wisdom: the blazing star
    im, d = canvas()
    pts = []
    for i in range(10):
        r = 14 if i % 2 == 0 else 6
        a = math.radians(-90 + i * 36)
        pts.append((15.5 + r * math.cos(a), 16.5 + r * math.sin(a)))
    d.polygon(pts, fill=GOLD)
    inner = [(15.5 + (p[0] - 15.5) * .55, 16.5 + (p[1] - 16.5) * .55) for p in pts]
    d.polygon(inner, fill=GOLD_L)
    d.text((13, 10), 'G', fill=BLUE_D)
    return outline(im)


def icon_combat():  # Trials: crossed gavel and chisel
    im, d = canvas()
    d.line([(5, 28), (24, 9)], fill=SILV, width=3)
    d.line([(5, 27), (23, 9)], fill=SILV_L)
    d.polygon([(24, 9), (29, 3), (28, 9)], fill=SILV_D)
    d.rectangle([3, 26, 7, 30], fill=WOOD)
    d.line([(27, 28), (12, 13)], fill=WOOD, width=3)
    d.polygon([(4, 12), (10, 4), (19, 12), (12, 20)], fill=WOOD_D)
    d.polygon([(5, 12), (10, 6), (14, 10), (9, 15)], fill=WOOD_L)
    return outline(im)


def icon_expedition():  # Pilgrimage: staff, gourd and scallop
    im, d = canvas()
    d.line([(9, 30), (20, 2)], fill=WOOD, width=2)
    d.ellipse([16, 1, 22, 6], fill=WOOD_D)
    d.ellipse([19, 9, 26, 16], fill=GOLD_D); d.ellipse([20, 14, 28, 23], fill=GOLD_D)
    d.ellipse([21, 15, 25, 19], fill=GOLD)
    d.pieslice([3, 13, 17, 27], 180, 360, fill=CREAM)
    d.rectangle([8, 20, 12, 23], fill=CREAM)
    for x in (6, 10, 14):
        d.line([(x, 15), (10, 21)], fill=WOOD_L)
    return outline(im)


ICONS = {
    'mining': icon_mining, 'woodcutting': icon_woodcutting, 'fishing': icon_fishing,
    'farming': icon_farming, 'thieving': icon_thieving, 'smithing': icon_smithing,
    'cooking': icon_cooking, 'fletching': icon_fletching, 'crafting': icon_crafting,
    'firemaking': icon_firemaking, 'runecrafting': icon_runecrafting, 'herblore': icon_herblore,
    'construction': icon_construction, 'prayer': icon_prayer, 'mercantile': icon_mercantile,
    'agility': icon_agility, 'slayer': icon_slayer, 'attack': icon_attack,
    'strength': icon_strength, 'defense': icon_defense, 'hitpoints': icon_hitpoints,
    'ranged': icon_ranged, 'magic': icon_magic, 'combat': icon_combat, 'expedition': icon_expedition,
}


# ---------------------------------------------------------------- seasonal banners

def emblem_sun():  # Summer St John
    im, d = canvas()
    for a in range(0, 360, 30):
        r = math.radians(a)
        d.polygon([(15.5 + 8 * math.cos(r - .25), 15.5 + 8 * math.sin(r - .25)),
                   (15.5 + 14 * math.cos(r), 15.5 + 14 * math.sin(r)),
                   (15.5 + 8 * math.cos(r + .25), 15.5 + 8 * math.sin(r + .25))], fill=GOLD)
    d.ellipse([7, 7, 24, 24], fill=GOLD)
    d.ellipse([10, 10, 21, 21], fill=GOLD_L)
    return outline(im)


def emblem_moon():  # Winter St John
    im, d = canvas()
    d.ellipse([4, 5, 25, 26], fill=SILV_L)
    d.ellipse([10, 2, 30, 23], fill=(0, 0, 0, 0))
    for x, y in ((22, 20), (26, 12), (19, 27)):
        d.polygon([(x, y - 3), (x + 1, y), (x, y + 3), (x - 1, y)], fill=GOLD_L)
        d.polygon([(x - 3, y), (x, y - 1), (x + 3, y), (x, y + 1)], fill=GOLD_L)
    return outline(im)


def emblem_acacia():  # Spring equinox
    im, d = canvas()
    d.line([(15, 31), (15, 3)], fill=GOLD_D)
    for y in range(6, 28, 5):
        d.ellipse([5, y, 13, y + 3], fill=GOLD)
        d.ellipse([17, y, 25, y + 3], fill=GOLD)
        d.point([(7, y + 1), (19, y + 1)], fill=GOLD_L)
    d.ellipse([13, 0, 17, 5], fill=GOLD_L)
    return outline(im)


BANNERS = {'spring': emblem_acacia, 'summer': emblem_sun, 'fall': icon_mercantile, 'winter': emblem_moon}


def banners(orig_dir):
    out = []
    for season, fn in BANNERS.items():
        im = Image.open(Path(orig_dir) / f'banner_{season}.png').convert('RGBA')
        im.alpha_composite(fn(), (11, 34))
        im.save(MAIN / 'res' / 'drawable' / f'banner_{season}.png')
        out.append(im)
    return out


# ---------------------------------------------------------------- character apron

def apron():
    """White apron with blue border, sized for the 64x36 character frame (torso x25..32)."""
    im = Image.new('RGBA', (64, 36), (0, 0, 0, 0))
    rows = {
        24: 'BBBBBBBB',
        25: 'BWWBBWWB',
        26: 'BWWWWWWB',
        27: 'BWWYYWWB',
        28: 'BBBBBBBB',
    }
    cols = {'B': BLUE, 'W': WHITE, 'Y': GOLD}
    for y, row in rows.items():
        for i, c in enumerate(row):
            im.putpixel((25 + i, y), cols[c])
    return im


def sheet(images, scale, path, cols=8):
    w, h = images[0].size
    rows = (len(images) + cols - 1) // cols
    out = Image.new('RGBA', (cols * (w * scale + 6), rows * (h * scale + 6)), (60, 64, 80, 255))
    for i, im in enumerate(images):
        out.alpha_composite(im.resize((w * scale, h * scale), Image.NEAREST),
                            ((i % cols) * (w * scale + 6), (i // cols) * (h * scale + 6)))
    out.save(path)


if __name__ == '__main__':
    PREVIEW.mkdir(parents=True, exist_ok=True)
    icons = []
    for key, fn in ICONS.items():
        im = fn()
        im.save(MAIN / 'res' / 'drawable' / f'skill_{key}.png')
        icons.append(im)
    sheet(icons, 4, PREVIEW / 'icons.png')
    reg = MAIN / 'assets' / 'sprites' / 'characters' / 'regalia'
    reg.mkdir(parents=True, exist_ok=True)
    apron().save(reg / 'apron.png')
    sheet(banners(Path(__file__).parent / 'art_src'), 4, PREVIEW / 'banners_new.png', cols=4)
    print('ok', len(icons))


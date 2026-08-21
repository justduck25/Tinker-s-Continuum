from __future__ import annotations

import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[2]
TEXTURES = ROOT / "src/main/resources/assets/tconstruct/textures/item/tool"
OUTPUT = Path(__file__).parent / "tool-overlays"

TOOLS: dict[str, tuple[str, tuple[str, ...]]] = {
    "pickaxe": ("pickaxe", ("head", "handle", "binding")),
    "sledge_hammer": ("sledge_hammer", ("head", "front", "back", "handle")),
    "vein_hammer": ("vein_hammer", ("head", "front", "grip", "handle")),
    "mattock": ("mattock", ("axe", "pick")),
    "pickadze": ("pickadze", ("pick", "adze")),
    "excavator": ("excavator", ("head", "handle", "grip", "binding")),
    "hand_axe": ("hand_axe", ("head", "binding")),
    "broad_axe": ("broad_axe", ("blade", "back", "handle", "binding")),
    "kama": ("kama", ("head", "binding")),
    "scythe": ("scythe", ("head", "handle", "binding", "accessory")),
    "dagger": ("dagger", ("blade", "handle", "guard", "crossguard")),
    "sword": ("sword", ("blade", "handle", "guard")),
    "cleaver": ("cleaver", ("head", "handle", "guard", "shield")),
    "crossbow": ("crossbow", ("body", "limb", "binding", "bowstring")),
    "longbow": ("longbow", ("limb_top", "limb_bottom", "grip", "bowstring")),
    "fishing_rod": ("fishing_rod", ("rod", "hook")),
    "javelin": ("javelin", ("head", "handle", "grip", "guard")),
    "flint_and_brick": ("flint_and_brick", ("tool",)),
    "sky_staff": ("staff", ("sky",)),
    "earth_staff": ("staff", ("earth",)),
    "ichor_staff": ("staff", ("ichor",)),
    "ender_staff": ("staff", ("ender",)),
    "melting_pan": ("melting_pan", ("head", "handle")),
    "battlesign": ("battlesign", ("head", "handle", "binding")),
    "swasher": ("swasher", ("barrel", "blade", "handle", "tank")),
    "minotaur_axe": ("minotaur_axe", ("front", "back")),
    "travelers_shield": ("armor/travelers/shield", ("wood", "cuirass", "trim")),
    "plate_shield": ("armor/plate/shield", ("core", "plating", "trim")),
}

PALETTES = {
    1: ((47, 75, 9, 255), (72, 108, 13, 255), (101, 139, 22, 255)),
    2: ((42, 69, 8, 255), (78, 116, 14, 255), (120, 166, 24, 255), (151, 194, 37, 255)),
    3: ((35, 62, 9, 255), (66, 105, 17, 255), (104, 151, 32, 255), (137, 176, 66, 255), (37, 157, 142, 255)),
}


def stable_value(name: str, x: int, y: int, salt: int = 0) -> float:
    digest = hashlib.blake2b(f"{name}:{x}:{y}:{salt}".encode(), digest_size=4).digest()
    return int.from_bytes(digest, "big") / 0xFFFFFFFF


def load_tool(tool: str, folder: str, layers: tuple[str, ...]) -> tuple[Image.Image, set[tuple[int, int]]]:
    images = []
    for layer in layers:
        path = TEXTURES / folder / f"{layer}.png"
        if not path.exists():
            raise FileNotFoundError(f"Missing source layer for {tool}: {path}")
        images.append(Image.open(path).convert("RGBA"))

    size = images[0].size
    if any(image.size != size for image in images):
        raise ValueError(f"Mismatched source sizes for {tool}")

    composite = Image.new("RGBA", size, (0, 0, 0, 0))
    mask: set[tuple[int, int]] = set()
    for image in images:
        composite.alpha_composite(image)
        alpha = image.getchannel("A")
        for y in range(size[1]):
            for x in range(size[0]):
                if alpha.getpixel((x, y)) > 24:
                    mask.add((x, y))
    return composite, mask


def neighbours(x: int, y: int) -> tuple[tuple[int, int], ...]:
    return ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1))


def build_overlay(name: str, size: tuple[int, int], mask: set[tuple[int, int]], level: int) -> Image.Image:
    width, height = size
    top_edge = {(x, y) for x, y in mask if (x, y - 1) not in mask}
    side_edge = {(x, y) for x, y in mask if any(point not in mask for point in neighbours(x, y))}

    top_chance = (0.45, 0.72, 0.92)[level - 1]
    side_chance = (0.12, 0.34, 0.62)[level - 1]
    inner_chance = (0.015, 0.07, 0.19)[level - 1]
    seeds: set[tuple[int, int]] = set()
    for x, y in mask:
        chance = top_chance if (x, y) in top_edge else side_chance if (x, y) in side_edge else inner_chance
        if stable_value(name, x, y, level) < chance:
            seeds.add((x, y))

    selected = set(seeds)
    grow_steps = level - 1
    for step in range(grow_steps):
        grown = set(selected)
        for x, y in selected:
            for nx, ny in neighbours(x, y):
                if (nx, ny) in mask and stable_value(name, nx, ny, 20 + step) < (0.55 + level * 0.08):
                    grown.add((nx, ny))
        selected = grown

    # Keep Moss I sparse as-is; trim later levels without changing their growth language.
    if level >= 2:
        selected = {
            (x, y) for x, y in selected
            if stable_value(name, x, y, 130 + level) >= 0.30
        }

    overlay = Image.new("RGBA", size, (0, 0, 0, 0))
    pixels = overlay.load()
    palette = PALETTES[level]
    for x, y in selected:
        color_index = min(len(palette) - 1, int(stable_value(name, x, y, 40 + level) * len(palette)))
        if level == 3 and color_index == len(palette) - 1 and stable_value(name, x, y, 75) > 0.22:
            color_index -= 1
        pixels[x, y] = palette[color_index]

    if level >= 2:
        lower_edges = [(x, y) for x, y in selected if (x, y + 1) not in mask]
        vine_chance = 0.12 if level == 2 else 0.34
        max_length = 1 if level == 2 else 3
        for x, y in lower_edges:
            if stable_value(name, x, y, 90 + level) >= vine_chance:
                continue
            length = 1 + int(stable_value(name, x, y, 100 + level) * max_length)
            for offset in range(1, length + 1):
                vy = y + offset
                if vy >= height or (x, vy) in mask:
                    break
                color = palette[-1] if level == 3 and stable_value(name, x, vy, 110) < 0.22 else palette[1]
                pixels[x, vy] = color
    return overlay


def build_animation(name: str, overlay: Image.Image, level: int) -> tuple[Image.Image, Image.Image, Image.Image]:
    width, height = overlay.size
    base = overlay.copy()
    first = overlay.copy()
    second = overlay.copy()
    occupied = {
        (x, y)
        for y in range(height)
        for x in range(width)
        if overlay.getpixel((x, y))[3] > 0
    }
    chance = (0.06, 0.10, 0.14)[level - 1]
    palette = PALETTES[level]

    candidates = []
    for x, y in sorted(occupied):
        empty = [
            (nx, ny)
            for nx, ny in ((x, y + 1), (x + 1, y), (x - 1, y), (x, y - 1))
            if 0 <= nx < width and 0 <= ny < height and (nx, ny) not in occupied
        ]
        if empty:
            candidates.append((x, y, empty))

    selected_candidates = [
        candidate for candidate in candidates
        if stable_value(name, candidate[0], candidate[1], 170 + level) < chance
    ]
    if not selected_candidates and candidates:
        selected_candidates.append(min(candidates, key=lambda point: stable_value(name, point[0], point[1], 171 + level)))

    tips = []
    extended = False
    for x, y, empty in selected_candidates:
        tip = empty[int(stable_value(name, x, y, 180 + level) * len(empty)) % len(empty)]
        tips.append(tip)
        color = palette[-1] if level == 3 and stable_value(name, *tip, 190) < 0.20 else palette[1]
        first.putpixel(tip, color)
        second.putpixel(tip, color)

        dx, dy = tip[0] - x, tip[1] - y
        extension = (tip[0] + dx, tip[1] + dy)
        if (
            0 <= extension[0] < width
            and 0 <= extension[1] < height
            and extension not in occupied
            and stable_value(name, *tip, 200 + level) < (0.35 if level == 1 else 0.55)
        ):
            second.putpixel(extension, palette[-1] if level == 3 else palette[2])
            extended = True
    if tips and (not extended or first.tobytes() == second.tobytes()):
        current = first.getpixel(tips[0])
        second.putpixel(tips[0], palette[0] if current != palette[0] else palette[-1])
    return base, first, second


def save_animation(tool_output: Path, level: int, frames: tuple[Image.Image, Image.Image, Image.Image]) -> None:
    width, height = frames[0].size
    strip = Image.new("RGBA", (width, height * len(frames)), (0, 0, 0, 0))
    for index, frame in enumerate(frames):
        strip.alpha_composite(frame, (0, index * height))
    path = tool_output / f"moss_{level}_animated.png"
    strip.save(path)
    metadata = {
        "animation": {
            "interpolate": False,
            "frames": [
                {"index": 0, "time": 45},
                {"index": 1, "time": 3},
                {"index": 2, "time": 3},
                {"index": 1, "time": 3},
            ],
        }
    }
    path.with_suffix(path.suffix + ".mcmeta").write_text(json.dumps(metadata, indent=2) + "\n", encoding="ascii")


def muted_base(source: Image.Image) -> Image.Image:
    result = Image.new("RGBA", source.size, (0, 0, 0, 0))
    src = source.load()
    dst = result.load()
    for y in range(source.height):
        for x in range(source.width):
            red, green, blue, alpha = src[x, y]
            if alpha:
                light = max(55, min(205, int(red * 0.22 + green * 0.55 + blue * 0.23)))
                dst[x, y] = (light, light, light, alpha)
    return result


def save_preview(entries: list[tuple[str, Image.Image, tuple[Image.Image, ...]]], page: int) -> None:
    scale = 8
    label_width = 128
    cell = 16 * scale
    header = 30
    row_height = cell + 14
    sheet = Image.new("RGB", (label_width + cell * 4, header + row_height * len(entries)), (20, 22, 19))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    headings = ("BASE", "MOSS I", "MOSS II", "MOSS III")
    for index, heading in enumerate(headings):
        draw.text((label_width + index * cell + 8, 10), heading, fill=(192, 220, 170), font=font)

    for row, (name, base, overlays) in enumerate(entries):
        top = header + row * row_height
        draw.text((8, top + cell // 2 - 4), name, fill=(225, 228, 220), font=font)
        cells = [base]
        for overlay in overlays:
            combined = base.copy()
            combined.alpha_composite(overlay)
            cells.append(combined)
        for column, image in enumerate(cells):
            enlarged = image.resize((cell, cell), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (label_width + column * cell, top), enlarged)
        draw.line((0, top + row_height - 1, sheet.width, top + row_height - 1), fill=(48, 52, 45))
    sheet.save(OUTPUT / f"moss_tool_overlays_preview_{page}.png")


def render_animation_sheet(
    entries: list[tuple[str, Image.Image, tuple[tuple[Image.Image, Image.Image, Image.Image], ...]]],
    animation_frame: int,
) -> Image.Image:
    scale = 8
    label_width = 128
    cell = 16 * scale
    header = 30
    row_height = cell + 14
    sheet = Image.new("RGB", (label_width + cell * 4, header + row_height * len(entries)), (20, 22, 19))
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for index, heading in enumerate(("BASE", "MOSS I", "MOSS II", "MOSS III")):
        draw.text((label_width + index * cell + 8, 10), heading, fill=(192, 220, 170), font=font)
    for row, (name, base, animations) in enumerate(entries):
        top = header + row * row_height
        draw.text((8, top + cell // 2 - 4), name, fill=(225, 228, 220), font=font)
        cells = [base]
        for animation in animations:
            combined = base.copy()
            combined.alpha_composite(animation[animation_frame])
            cells.append(combined)
        for column, image in enumerate(cells):
            enlarged = image.resize((cell, cell), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (label_width + column * cell, top), enlarged)
        draw.line((0, top + row_height - 1, sheet.width, top + row_height - 1), fill=(48, 52, 45))
    return sheet


def save_animation_preview(
    entries: list[tuple[str, Image.Image, tuple[tuple[Image.Image, Image.Image, Image.Image], ...]]],
    page: int,
) -> None:
    sequence = (0, 1, 2, 1)
    frames = [render_animation_sheet(entries, index) for index in sequence]
    frames[0].save(
        OUTPUT / f"moss_tool_animation_preview_{page}.gif",
        save_all=True,
        append_images=frames[1:],
        duration=(2250, 150, 150, 150),
        loop=0,
        disposal=2,
    )


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    preview_entries = []
    animation_entries = []
    for tool, (folder, layers) in TOOLS.items():
        source, mask = load_tool(tool, folder, layers)
        tool_output = OUTPUT / tool
        tool_output.mkdir(parents=True, exist_ok=True)
        overlays = tuple(build_overlay(tool, source.size, mask, level) for level in (1, 2, 3))
        animations = tuple(build_animation(tool, overlay, level) for level, overlay in enumerate(overlays, 1))
        for level, animation in enumerate(animations, 1):
            if animation[0].tobytes() == animation[1].tobytes():
                raise ValueError(f"Moss animation does not sprout for {tool} level {level}")
            if animation[1].tobytes() == animation[2].tobytes():
                raise ValueError(f"Moss animation has no second motion for {tool} level {level}")
        for level, overlay in enumerate(overlays, 1):
            overlay.save(tool_output / f"moss_{level}.png")
        for level, animation in enumerate(animations, 1):
            save_animation(tool_output, level, animation)
        base = muted_base(source)
        base.save(tool_output / "source_preview.png")
        preview_entries.append((tool, base, overlays))
        animation_entries.append((tool, base, animations))

    midpoint = (len(preview_entries) + 1) // 2
    save_preview(preview_entries[:midpoint], 1)
    save_preview(preview_entries[midpoint:], 2)
    save_animation_preview(animation_entries[:midpoint], 1)
    save_animation_preview(animation_entries[midpoint:], 2)


if __name__ == "__main__":
    main()

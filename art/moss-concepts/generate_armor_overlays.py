from __future__ import annotations

import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from generate_tool_overlays import build_animation, build_overlay, muted_base


ROOT = Path(__file__).resolve().parents[2]
ITEM_ROOT = ROOT / "src/main/resources/assets/tconstruct/textures/item/tool/armor"
WORN_ROOT = ROOT / "src/main/resources/assets/tconstruct/textures/tinker_armor"
OUTPUT = Path(__file__).parent / "armor-overlays"

ITEMS: dict[str, tuple[str, tuple[str, ...]]] = {
    "travelers_helmet": ("travelers/goggles", ("base", "cuirass", "metal")),
    "travelers_chestplate": ("travelers/vest", ("cuirass", "metal")),
    "travelers_leggings": ("travelers/pants", ("cuirass", "metal")),
    "travelers_boots": ("travelers/boots", ("cuirass", "metal")),
    "plate_helmet": ("plate/helmet", ("maille", "plating")),
    "plate_chestplate": ("plate/chestplate", ("maille", "plating")),
    "plate_leggings": ("plate/leggings", ("maille", "plating")),
    "plate_boots": ("plate/boots", ("maille", "plating")),
    "slime_helmet": ("slime/helmet", ("slime", "skull")),
    "slimy_chestplate": ("slime/chestplate", ("slime", "ribcage")),
    "slime_leggings": ("slime/leggings", ("slime", "shell")),
    "slime_boots": ("slime/boots", ("slime", "laces")),
    "slime_wings": ("slime/wings", ("slime", "trim")),
}

WORN: dict[str, tuple[str, ...]] = {
    "travelers_armor": (
        "travelers/base_armor.png",
        "travelers/cuirass_armor.png",
        "travelers/metal_armor.png",
    ),
    "travelers_leggings": (
        "travelers/cuirass_leggings.png",
        "travelers/metal_leggings.png",
    ),
    "plate_armor": (
        "plate/maille_armor.png",
        "plate/plating_armor.png",
    ),
    "plate_leggings": (
        "plate/maille_leggings.png",
        "plate/plating_leggings.png",
    ),
    "slime_armor": ("slime/armor.png",),
    "slime_leggings": ("slime/leggings.png",),
    "slime_wings": ("slime/wings.png",),
}


def load_layers(root: Path, paths: tuple[str, ...]) -> tuple[Image.Image, set[tuple[int, int]]]:
    images = []
    for relative in paths:
        path = root / relative
        if not path.exists():
            raise FileNotFoundError(path)
        images.append(Image.open(path).convert("RGBA"))
    size = images[0].size
    if any(image.size != size for image in images):
        raise ValueError(f"Mismatched source sizes: {paths}")

    composite = Image.new("RGBA", size, (0, 0, 0, 0))
    mask: set[tuple[int, int]] = set()
    for image in images:
        composite.alpha_composite(image)
        for y in range(size[1]):
            for x in range(size[0]):
                if image.getpixel((x, y))[3] > 24:
                    mask.add((x, y))
    if not mask:
        raise ValueError(f"Empty armor mask: {paths}")
    return composite, mask


def keep_animation_on_atlas(
    frames: tuple[Image.Image, Image.Image, Image.Image], mask: set[tuple[int, int]]
) -> tuple[Image.Image, Image.Image, Image.Image]:
    result = []
    for frame in frames:
        cleaned = frame.copy()
        for y in range(frame.height):
            for x in range(frame.width):
                if (x, y) not in mask:
                    cleaned.putpixel((x, y), (0, 0, 0, 0))
        result.append(cleaned)
    return tuple(result)  # type: ignore[return-value]


def save_animation(folder: Path, level: int, frames: tuple[Image.Image, Image.Image, Image.Image]) -> None:
    width, height = frames[0].size
    strip = Image.new("RGBA", (width, height * 3), (0, 0, 0, 0))
    for index, frame in enumerate(frames):
        strip.alpha_composite(frame, (0, index * height))
    path = folder / f"moss_{level}_animated.png"
    strip.save(path)
    metadata = {
        "animation": {
            "interpolate": False,
            "frames": [
                {"index": 0, "time": 43},
                {"index": 1, "time": 4},
                {"index": 2, "time": 4},
                {"index": 1, "time": 4},
            ],
        }
    }
    path.with_suffix(path.suffix + ".mcmeta").write_text(
        json.dumps(metadata, indent=2) + "\n", encoding="ascii"
    )


def render_sheet(entries, frame_index: int | None, atlas: bool) -> Image.Image:
    scale = 4 if atlas else 8
    source_width, source_height = entries[0][1].size
    cell_width, cell_height = source_width * scale, source_height * scale
    label_width = 150
    header = 28
    row_height = cell_height + 8
    sheet = Image.new(
        "RGB",
        (label_width + cell_width * 4, header + row_height * len(entries)),
        (20, 22, 19),
    )
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.load_default()
    for column, label in enumerate(("BASE", "MOSS I", "MOSS II", "MOSS III")):
        draw.text((label_width + column * cell_width + 6, 9), label, fill=(192, 220, 170), font=font)
    for row, (name, base, overlays, animations) in enumerate(entries):
        top = header + row * row_height
        draw.text((8, top + cell_height // 2 - 4), name, fill=(225, 228, 220), font=font)
        cells = [base]
        for index in range(3):
            combined = base.copy()
            layer = overlays[index] if frame_index is None else animations[index][frame_index]
            combined.alpha_composite(layer)
            cells.append(combined)
        for column, image in enumerate(cells):
            enlarged = image.resize((cell_width, cell_height), Image.Resampling.NEAREST)
            sheet.paste(enlarged, (label_width + column * cell_width, top), enlarged)
        draw.line((0, top + row_height - 1, sheet.width, top + row_height - 1), fill=(48, 52, 45))
    return sheet


def save_previews(entries, prefix: str, atlas: bool) -> None:
    render_sheet(entries, None, atlas).save(OUTPUT / f"{prefix}_preview.png")
    sequence = (0, 1, 2, 1)
    frames = [render_sheet(entries, index, atlas) for index in sequence]
    frames[0].save(
        OUTPUT / f"{prefix}_animation_preview.gif",
        save_all=True,
        append_images=frames[1:],
        duration=(2150, 200, 200, 200),
        loop=0,
        disposal=2,
    )


def generate_group(group: str, definitions, loader, atlas: bool):
    entries = []
    for name, data in definitions.items():
        source, mask = loader(data)
        folder = OUTPUT / group / name
        folder.mkdir(parents=True, exist_ok=True)
        overlays = tuple(build_overlay(f"armor:{name}", source.size, mask, level) for level in (1, 2, 3))
        animations = []
        for level, overlay in enumerate(overlays, 1):
            animation = build_animation(f"armor:{name}", overlay, level)
            if atlas:
                animation = keep_animation_on_atlas(animation, mask)
            if animation[0].tobytes() == animation[1].tobytes():
                # Atlas edges may remove the sprout; retain a visible pulse on one moss pixel.
                pulse = animation[1].copy()
                point = next(iter(mask & {(x, y) for y in range(source.height) for x in range(source.width) if overlay.getpixel((x, y))[3]}))
                red, green, blue, alpha = pulse.getpixel(point)
                pulse.putpixel(point, (min(180, red + 25), min(230, green + 35), min(120, blue + 12), alpha))
                animation = (animation[0], pulse, animation[2])
            if animation[1].tobytes() == animation[2].tobytes():
                settle = animation[2].copy()
                point = next((p for p in mask if settle.getpixel(p)[3]), next(iter(mask)))
                red, green, blue, alpha = settle.getpixel(point)
                settle.putpixel(point, (max(20, red - 12), max(35, green - 15), blue, alpha))
                animation = (animation[0], animation[1], settle)
            animations.append(animation)
            save_animation(folder, level, animation)
        for level, overlay in enumerate(overlays, 1):
            overlay.save(folder / f"moss_{level}.png")
        base = muted_base(source)
        base.save(folder / "source_preview.png")
        entries.append((name, base, overlays, tuple(animations)))
    return entries


def main() -> None:
    OUTPUT.mkdir(parents=True, exist_ok=True)
    item_entries = generate_group(
        "items",
        ITEMS,
        lambda data: load_layers(ITEM_ROOT / data[0], tuple(f"{layer}.png" for layer in data[1])),
        False,
    )
    worn_entries = generate_group(
        "worn",
        WORN,
        lambda paths: load_layers(WORN_ROOT, paths),
        True,
    )
    save_previews(item_entries, "moss_armor_items", False)
    save_previews(worn_entries, "moss_armor_worn_atlases", True)


if __name__ == "__main__":
    main()

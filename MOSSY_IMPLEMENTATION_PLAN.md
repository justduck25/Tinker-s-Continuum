# Mossy Modifier Integration Plan

## 1. Muc tieu

Tich hop modifier `tconstruct:mossy`, lay cam hung tu Moss cua Tinkers' Construct 1.

- Tu dong hoi durability theo thoi gian.
- Ho tro tool va armor co durability.
- Khong ho tro ammo khong co durability nhu arrow va shuriken.
- Co 3 level voi overlay va animation rieng.
- Bao toan material color, armor trim va cac modifier model khac.
- Moi giai doan chi duoc danh dau hoan thanh sau khi nguoi dung test va noi `OK`.

## 2. Dac ta gameplay de xuat

### Modifier

- ID: `tconstruct:mossy`
- Ten hien thi: `Mossy`
- Level toi da: 3
- Mossy III co the hien subtitle/flavor `Ancient Moss`.
- Moi level dung 1 upgrade slot.

### Toc do repair da chot

- Mossy I: trung binh 1 durability moi 20 giay.
- Mossy II: trung binh 2 durability moi 15 giay.
- Mossy III: trung binh 2 durability moi 5 giay.
- Neu player/item dang dung duoi anh sang: toc do repair x2.
- Neu khong co anh sang: toc do repair binh thuong.

Gia tri phai duoc luu trong modifier data/module config de co the can bang lai ma khong viet lai logic Java.

### Quy tac repair

- Chi xu ly phia server.
- Chi repair khi damage lon hon 0.
- Tool da broken van co the hoi durability va hoat dong lai.
- Khong repair trong luc item dang duoc dung lien tuc, vi du keo cung hoac dung khien.
- Xu ly main hand, offhand, inventory va armor dang mac.
- Tick theo khoang thoi gian co dinh, khong sync moi game tick.
- Repair durability that, khong hoi Overslime.
- He so anh sang tinh theo vi tri entity dang giu/mang item.
- Can chot threshold trong code sau khi doi chieu TCon/Minecraft API, de xuat: co block light hoac sky light lon hon 0 thi duoc tinh la co anh sang.

### Item duoc ho tro

- Durable melee tools.
- Harvest tools.
- Bows, crossbows va fishing rods co durability.
- Shields.
- Traveler, Plate va Slimesuit armor.

### Item bi loai

- Vanilla arrow.
- Tinkers' arrow.
- Shuriken.
- Ammo hoac single-use item khong co durability.
- Bat ky item nao chi dung stack count lam ammo.

## 3. Giai doan A: Dang ky du lieu

### Ball of Moss

- Dang ky item `ball_of_moss`.
- Gan icon dong da tao trong `art/moss-concepts/icons`.
- Them vao creative tab phu hop.
- Them ten va tooltip.
- Them JEI visibility.

### Recipe Ball of Moss

Cong thuc:

```text
M M M
M M M
M M M
```

Trong do `M` la bat ky block/item vanilla da co mossy variant hop ly, gom it nhat:

- Mossy Stone Bricks.
- Mossy Cobblestone.
- Cac loai mossy da ton tai trong registry/tag local neu co.

Ket qua: 1 Ball of Moss.

Nen tao item tag `tconstruct:ball_of_moss_ingredients` de recipe va JEI de bao tri.

### Modifier registration

- Them `ModifierIds.mossy`.
- Dang ky module loader trong `TinkerModifiers`.
- Them modifier trong `ModifierProvider`.
- Dat max level 3.
- Them lang va tooltip.

### Tieu chi hoan thanh

- Datagen tao modifier JSON hop le.
- Ball of Moss hien dung ten, icon va recipe.
- Modifier hien trong JEI/Tinker Station.
- Chua danh dau hoan thanh cho toi khi nguoi dung noi `OK`.

## 4. Giai doan B: Gameplay module

### Kien truc

Tao `MossyRepairModule` implement `InventoryTickModifierHook`.

Tham khao pipeline hien co:

- `OvergrowthModule` cho inventory tick va interval.
- `ModifiableItem` cho tool inventory tick.
- `ModifiableLauncherItem` cho launcher.
- Equipment slot handling cua cac armor module.

Khong tai su dung truc tiep `OvergrowthModule`, vi module do hoi Overslime thay vi durability.

### Logic

1. Bo qua client side.
2. Bo qua item khong damage.
3. Bo qua item dang duoc su dung.
4. Lay light multiplier tu entity position: co anh sang = x2, khong co anh sang = x1.
5. Mossy I: sau 20 giay repair 1, duoi anh sang sau 10 giay repair 1.
6. Mossy II: sau 15 giay repair 2, duoi anh sang sau 7.5 giay repair 2.
7. Mossy III: sau 5 giay repair 2, duoi anh sang sau 2.5 giay repair 2.
8. Giam damage cua tool theo amount cua level.
9. Sync stack chi khi durability that su thay doi.

### Tuong tac modifier

- Reinforced: duoc phep ket hop.
- Overslime: duoc phep ket hop, hai he thong doc lap.
- Unbreakable: Mossy du thua nhung khong duoc gay loi.
- Vanilla Mending: kiem tra de tranh double repair bat thuong.
- Broken state: phai co the thoat broken sau khi du durability.

### Tieu chi hoan thanh

- Level I, II, III cho toc do khac nhau.
- Tool broken hoi lai va su dung duoc.
- Armor dang mac tu repair.
- Khong repair ammo.
- Khong co client/server desync.
- Chua danh dau hoan thanh cho toi khi nguoi dung noi `OK`.

## 5. Giai doan C: Modifier recipe

- Mossy I: bo 2 Ball of Moss vao tool station de apply.
- Mossy II: bo 4 Ball of Moss vao tool station de nang tu Mossy I len Mossy II.
- Mossy III: bo 4 Ball of Moss + 2 Netherite Ingot vao tool station de nang tu Mossy II len Mossy III.
- Max level 3.
- Moi level ton 1 upgrade slot.
- Tool predicate: durability tool hoac worn armor, tru single-use/ammo.
- Them salvage recipe neu pipeline hien tai ho tro dung.
- Kiem tra recipe trong Tinker Station va JEI.

### Tieu chi hoan thanh

- Apply duoc cho tat ca durable tool du kien.
- Apply duoc cho armor.
- Khong apply duoc cho arrow/shuriken.
- Mossy I dung dung 2 Ball of Moss.
- Mossy II dung dung 4 Ball of Moss.
- Mossy III dung dung 4 Ball of Moss + 2 Netherite Ingot.
- Khong vuot level 3.
- Slot bi tru dung.
- Chua danh dau hoan thanh cho toi khi nguoi dung noi `OK`.

## 6. Giai doan D: Tool overlay

### Asset nguon

- `art/moss-concepts/tool-overlays`
- 28 durable tool folders.
- Moi tool co Moss I, Moss II va Moss III.
- Moi level co static texture, animated strip va `.mcmeta`.

### Model pipeline

`NormalModifierModel` chi ve mot texture co dinh. Mossy can model moi co kha nang chon texture theo modifier level.

De xuat:

- Tao `MossyModifierModel`.
- Dang ky loader trong `ToolClientEvents.registerModifierModels`.
- Them mapping trong `ModifierModelMapProvider`.
- Chon Moss I/II/III dua tren `ModifierEntry.level()`.
- Dung animated texture strip khi resource duoc load.

### Noi can test render

- Creative inventory.
- Player inventory.
- First person.
- Third person.
- Ground item.
- Item frame.
- Casting table.
- Tinker Station preview.

### Tieu chi hoan thanh

- Du 28 durable tool.
- Dung overlay theo level.
- Animation chay dung.
- Khong texture tim.
- Khong chay muc/pixel extrusion.
- Khong sai rotation tren casting table.
- Chua danh dau hoan thanh cho toi khi nguoi dung noi `OK`.

## 7. Giai doan E: Armor overlay

### Asset nguon

- `art/moss-concepts/armor-overlays`
- 13 armor item icons.
- 7 worn armor atlas masks.

### Item model

- Them level-aware Mossy overlay vao armor item model.
- Dung animation rieng cho icon armor.
- Khong thay the material base model.

### Worn model

- Them Mossy layer vao armor rendering pipeline.
- Ho tro Traveler, Plate, Slimesuit va Slime Wings.
- Bao toan material tint.
- Bao toan armor trim.
- Bao toan cac modifier layer khac.
- Worn animation doc lap voi item icon animation.

### Tieu chi hoan thanh

- Armor item hien dung overlay.
- Armor dang mac hien dung overlay.
- Trim van hien.
- Khong thay sai armor material.
- First/third person khong crash.
- Chua danh dau hoan thanh cho toi khi nguoi dung noi `OK`.

## 8. Giai doan F: Hoan thien du lieu va UI

- `en_us` lang.
- Tooltip mo ta auto-repair.
- Ten Ball of Moss.
- Ten modifier va level.
- Modifier icon animation rieng.
- Book entry neu phu hop voi structure book hien tai.
- JEI recipe va usage.
- Creative tab.
- Modifier tags/category.
- Salvage data.

## 9. Kiem thu

### Build

```powershell
cd D:\Game\Tcon3\Tcon4
.\gradlew.bat compileJava *> compile-latest.log
.\gradlew.bat runData
```

### Gameplay matrix

- Mossy I, II, III tren cung mot tool type.
- Tool damage nhe.
- Tool gan broken.
- Tool broken hoan toan.
- Tool trong hotbar.
- Tool trong inventory.
- Tool o main hand va offhand.
- Armor trong inventory.
- Armor dang mac.
- Repair trong bong toi.
- Repair duoi anh sang.
- So sanh toc do repair co anh sang x2 voi khong co anh sang.
- Logout/rejoin.
- Doi dimension.
- Dedicated server.

### Compatibility matrix

- Reinforced.
- Overslime.
- Unbreakable.
- Armor trim.
- Modifier model khac.
- Broken model.
- Ball of Moss va modifier icon animation.

### Render matrix

- Tat ca 28 tool.
- Tat ca 13 armor item.
- Tat ca 7 worn armor atlas.
- Moss I, II, III.
- First person va third person.
- GUI, ground, item frame va casting table.

## 10. Thu tu thuc hien

1. Chot gameplay va recipe.
2. Dang ky Ball of Moss va modifier data.
3. Implement `MossyRepairModule`.
4. Implement modifier recipe.
5. Tich hop tool overlays.
6. Test va cho nguoi dung xac nhan tool.
7. Tich hop armor item overlays.
8. Tich hop worn armor overlays.
9. Them UI, lang, JEI va book.
10. Chay full build, datagen va runtime matrix.

## 11. Trang thai

- [x] Gameplay specification da chot
- [ ] Ball of Moss da dang ky
- [ ] Ball of Moss recipe hoat dong
- [ ] Mossy modifier da dang ky
- [ ] Mossy repair module hoat dong
- [ ] Modifier recipe hoat dong
- [ ] Tool overlay Moss I/II/III hoat dong
- [ ] Tool animation hoat dong
- [ ] Armor item overlay hoat dong
- [ ] Worn armor overlay hoat dong
- [ ] Armor animation hoat dong
- [ ] Lang, tooltip, JEI va book hoan thanh
- [ ] Compile pass
- [ ] Datagen pass
- [ ] RunClient test pass
- [ ] Dedicated server test pass
- [ ] Nguoi dung xac nhan `OK`

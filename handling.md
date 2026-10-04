# TCon4 – Bàn giao nghiên cứu và kế hoạch Ability Apotheosis

**Mục đích:** File này dành cho agent khác tiếp quản việc nghiên cứu và port cơ chế ability kiểu Apotheosis cho Tinkers’ Construct 4 trên NeoForge 26.1.2. Đọc file này trước khi sửa code. Đây là **handoff đang thực hiện**, chưa phải thông báo hoàn thành task.

**Ngôn ngữ làm việc:** Tiếng Việt, ngắn, trực tiếp, kỹ thuật.

**Ngày cập nhật:** 2026-08-23 GMT+7.

---

## 1. Quy tắc bắt buộc trước khi làm việc

Luôn đọc hai file sau trước khi chỉnh sửa hoặc build:

1. `D:\Game\Tcon3\AGENT.md`
2. `D:\Game\Tcon3\PORTING_RULEBOOK.md`

Quy tắc chính trong workspace là source-first:

> So sánh hành vi gốc trong `D:\Game\Tcon3\TinkersConstruct`, sau đó kiểm tra TCon4/Mantle hiện tại, source jar NeoForge/Minecraft 26.1.2, tài liệu `Must Read`, rồi mới sửa code. Không đoán API hoặc schema.

Thông tin nền tảng:

| Thành phần | Giá trị |
|---|---|
| Workspace chính | `D:\Game\Tcon3` |
| Project đang sửa | `D:\Game\Tcon3\Tcon4` |
| Project gốc để so sánh | `D:\Game\Tcon3\TinkersConstruct` |
| Branch mục tiêu | `neoforge-26.1.2` |
| Minecraft | 26.1.2 |
| NeoForge | 26.1.2.78 |
| Java | 25 |
| TCon remote | `https://github.com/justduck25/Tinker-Construct-3-NeoForge.git` |
| Author/user | `justduck` / `justduck25` |

Không sửa, revert hoặc stage các file ngoài phạm vi. Workspace có nhiều log, script, file tạm và thay đổi cũ. Trước khi commit phải stage từng file có chủ đích.

Không tạo lại `todo.md` hoặc `todonext.md`; user đã yêu cầu xóa chúng. Không rebuild Mantle nếu Mantle không thay đổi. Sau mỗi compile đáng kể phải cập nhật `Tcon4\compile-latest.log`.

---

## 2. Ý định mới nhất của user

User muốn quay lại nghiên cứu Apotheosis/Apothic Enchanting vì Apotheosis có mô hình cường hóa enchantment vượt level vanilla. Ý tưởng mong muốn:

> Có một công thức ability mới, kiểu `apotheosis`. Khi add ability này vào TCon tool thì tool unlock được level mới cho các modifier.

Mục tiêu thật là **mở level modifier TCon cao hơn cap bình thường bằng một ability có dữ liệu lưu thật**, không chỉ cộng một effective level tạm thời ở một accessor.

Không được bật vanilla enchanting cho TCon tool. Không ghi vanilla enchantment component. Không sửa trực tiếp Apotheosis affix component. Không ghi fake modifier hoặc fake NBT.

---

## 3. Trạng thái công việc trước task ability

Phần port TCon4 trước đó phần lớn đã hoàn tất. Một số task compat đã xong và user đã test/đồng ý:

| Hạng mục | Trạng thái |
|---|---|
| Tool Leveling addon | Đã port thành project riêng `tool-leveling-26.1.2`, đã push trước đó |
| Crafting Tweaks compat | Đã qua |
| JsonThings compat | Đã qua |
| Javelin projectile 3D | User đã hủy, không tiếp tục |
| Nhiều bug foundry/smeltery/render/tool/armor | Đã xử lý qua các phiên trước |
| Apotheosis compat slice A | Đã làm và user test/đồng ý |
| Apotheosis direct Prosperous bridge | Đã code, compile, build và smoke-load thành công; chưa commit/push sau thay đổi này |

### 3.1. Apotheosis slice A đã làm

Runtime đã kiểm tra với stack:

- Apotheosis `9.0.3`
- Placebo `10.0.2`
- Apothic Attributes `3.0.1`
- Apothic Enchanting `2.0.0`
- Apothic Spawners `2.0.0`

TCon4 có data map:

`src/main/resources/data/apotheosis/data_maps/item/loot_category_overrides.json`

Data map này đưa một số TCon breaker/melee/trident/shield tools vào category loot của Apotheosis. Đây là compat category/affix, không phải vanilla enchanting.

Build metadata hiện tại có optional Apotheosis dependency và compileOnly Apotheosis/Placebo. Các thay đổi chính nằm trong:

- `build.gradle`
- `gradle.properties`
- `src/main/resources/META-INF/neoforge.mods.toml`

### 3.2. Bridge Prosperous hiện tại

File:

`src/main/java/slimeknights/tconstruct/plugin/apotheosis/ApotheosisModifierBridge.java`

Bridge hiện tại:

- Chỉ hoạt động khi mod list có `apotheosis`.
- Đọc affix bằng `AffixHelper.streamAffixes(ItemStack)`.
- Map Prosperous affix đã xác minh sang effective `luck` level.
- Không ghi `Components.AFFIXES`.
- Không ghi vanilla enchantment.
- Không sửa `tic_modifiers` hoặc `ToolStack` modifier NBT.
- Không tạo fake modifier.

File accessor liên quan:

`src/main/java/slimeknights/tconstruct/library/tools/helper/ModifierUtil.java`

Accessor này gọi bridge cho effective `luck` level. Đây là bridge read-only; nó **không đủ** để trở thành cơ chế unlock level cho mọi modifier vì nhiều TCon hook khác đọc trực tiếp `ModifierEntry.getEffectiveLevel()` hoặc lặp dữ liệu modifier riêng.

Compile và build hiện tại đã pass trước khi mở rộng task:

- `compileJava`: pass
- `build -x test`: pass
- Runtime có thể load đầy đủ stack Apotheosis: pass, không có NoClassDefFoundError/NoSuchMethodError do bridge

Có một lỗi runtime upstream/data riêng:

`apotheosis:generic/attribute/lucky` tham chiếu loot category `apotheosis:charm` không có trong registry. Lỗi này không xuất phát từ bridge TCon. Không được nhầm nó với lỗi ability mới.

---

## 4. Research đã xác minh về Apotheosis và enchantment level

### 4.1. Apotheosis affix không phải generic TCon modifier system

Apotheosis dùng hệ thống riêng:

- `Affix`
- `AffixInstance`
- `AffixHelper`
- `AffixManager`
- ItemStack component `Components.AFFIXES`
- Rarity/category riêng

Apotheosis không có TCon integration sẵn trong upstream Java source. TCon4 phải tự định nghĩa compat.

Trong generated Apotheosis 26.1 đã xác minh có đúng hai built-in affix dạng `type: apotheosis:enchantment`:

| Affix | Enchantment đích | Mode | Giá trị đã thấy |
|---|---|---|---|
| `apotheosis:breaker/enchantment/prosperous` | `minecraft:fortune` | `existing` | Uncommon 1; Rare 1–2; Epic 1–3; Mythic 2–4 |
| `apotheosis:ranged/enchantment/prosperous` | `minecraft:looting` | `single` | Epic 6–8; Mythic 8–10 |

`EnchantmentAffix` áp dụng bonus vào event enchantment level. Bridge TCon hiện tại đọc affix để ảnh hưởng effective `luck`, nhưng đây là bridge riêng, không phải cơ chế unlock recipe.

### 4.2. Mô hình level của Apothic Enchanting

Mô hình “enchantment vượt level” chủ yếu nằm trong **Apothic Enchanting 2.0.0**.

`EnchantmentInfo` có các giới hạn tách riêng:

- `maxLevel`
- `maxLootLevel`
- `maxAnvilCombineLevel`
- `levelCap`

`EnchHooks.getMaxLevel(Enchantment)` lấy max level configured/fallback. Event handler `ApothEnchEvents.clamp(GetEnchantmentLevelEvent)` duyệt mutable enchantment map và clamp effective value theo `EnchantmentInfo.levelCap()` nếu cap khác `-1`.

Kết luận quan trọng:

> Apothic Enchanting tách level được lưu/tính toán khỏi effective cap. TCon nên mượn mô hình này về mặt ý tưởng, nhưng phải lưu level TCon thật bằng recipe và requirement; không nên chỉ patch một getter.

Các source/class đã kiểm tra:

- `ApothEnchEvents`
- `asm.EnchHooks`
- `EnchantmentInfo`
- `mixin.EnchantmentMixin`
- `mixin.ItemStackMixin`
- `EnchantmentAffix`

File research tạm cũ:

`Tcon4\tmp-apothic-enchant-javap.txt`

Đây là file nghiên cứu, không phải product source. Không cần stage khi commit.

---

## 5. Research đã xác minh về cap modifier TCon

### 5.1. TCon không có global modifier cap

Cap chủ yếu nằm trong từng recipe. `AbstractModifierRecipe` dùng `IntRange level` của recipe:

- `getNewLevel(tool)` lấy raw level hiện có rồi cộng 1.
- `validatePrerequisites(tool, resultLevel)` gọi `validateLevel(resultLevel)`.
- Nếu result level lớn hơn `level.max()`, recipe bị từ chối.
- Sau khi pass các check chung, recipe copy tool và `tool.addModifier(...)`.
- Sau đó `tool.tryValidate()` chạy validation của modifier.

Các dòng quan trọng đã kiểm tra:

- `AbstractModifierRecipe.java` quanh 333–395
- `ModifierRecipe.java` quanh 145–170
- `IncrementalModifierRecipe.java`
- `MultilevelModifierRecipe.java`
- `MultilevelIncrementalModifierRecipe.java`

Điều này xác nhận chiến lược đúng:

1. Nới recipe range cho level post-cap.
2. Gắn `ModifierRequirementsModule` vào modifier ở range post-cap.
3. Recipe add modifier thật.
4. `tool.tryValidate()` gọi `ModifierHooks.VALIDATE_UPGRADE` và requirement module chặn nếu tool chưa có ability `apotheosis`.

### 5.2. ModifierRequirementsModule phù hợp

File:

`src/main/java/slimeknights/tconstruct/library/modifiers/modules/build/ModifierRequirementsModule.java`

Module này có:

- `requireModifier(ModifierId id, int level)` để yêu cầu modifier/ability khác.
- `minLevel(...)` và `maxLevel(...)` để requirement chỉ có hiệu lực trong một range.
- `validate(IToolStackView tool, ModifierEntry modifier)` kiểm tra level post-add.
- `requirementsError(...)` để hiển thị lỗi/requirement trong UI.
- `displayModifier(...)` để hiển thị requirement.

Ví dụ TCon hiện tại đã dùng pattern requirement có level, như `unbreakable` yêu cầu netherite và reinforced level 5.

Requirement đề xuất cho modifier mục tiêu:

```java
ModifierRequirementsModule.builder()
  .requireModifier(ModifierIds.apotheosis, 1)
  .minLevel(NORMAL_CAP + 1)
  .maxLevel(EXTENDED_CAP)
  .modifierKey(ModifierIds.TARGET)
  .build()
```

Tên method/builder phải kiểm tra lại trực tiếp nếu code hiện tại khác; không đoán API. `ModifierRequirementsModule` hiện có builder `.minLevel(...)`/`.maxLevel(...)` qua Lombok fluent setter, nhưng compile phải là nguồn xác nhận cuối cùng.

### 5.3. Cap hiện tại của một số modifier

Các cap đã đọc trong `ModifierRecipeProvider.java`:

| Modifier | Cap recipe hiện tại | Công thức/ghi chú |
|---|---:|---|
| `reinforced` | 5 | Có công thức riêng level 0–5 và module scaling tiếp từ level 6 |
| `haste` | 5 | Mining speed +4/level, attribute multiplier +0.1/level |
| `sharpness` | 5 | Attack damage +0.75/level |
| `smite` | 5 | Conditional undead damage +2/level |
| `bane_of_sssss` | 5 | Conditional arthropod/creeper damage +2/level, thêm slow effect |
| `knockback` | 3 | Knockback module/attribute/formula |
| `fiery` | 5 | Fiery attack +5/level; armor fire counter riêng |
| `power` | 5 | Projectile damage +0.5/level |
| `punch` | 3 | Punch +1/level |
| `quick_charge` | 4 | Draw speed multiplier +0.25/level |
| `arrow_pierce` | Cần lấy exact region/name khi code | Arrow pierce +1/level |
| `luck` | 3 | Fortune/Looting/Sea Luck/armor Luck; `UniqueForLevels(3)` |
| `fortune`/`looting` | Có module projection | Cần xem exact recipe/cap trước khi mở |
| `multishot` | Cần audit | Tăng số projectile/ammo behavior |
| `sweeping` | 3 | Sweeping +0.25/level |
| `swiftstrike` | 5 | Attack speed +0.05/level |

`ModifierProvider.java` đã cho thấy nhiều công thức level cao vẫn có ý nghĩa. Riêng modifier có probability, inventory/ammo, slotless, unique display, hoặc side effect phức tạp không được blanket unlock.

### 5.4. Mapping vanilla enchantment → TCon modifier

`src/main/java/slimeknights/tconstruct/tools/data/EnchantmentToModifierProvider.java` đã xác minh các mapping chính:

| Vanilla enchantment | TCon modifier |
|---|---|
| Fortune/Looting/Luck of the Sea | `luck` |
| Efficiency | `haste` |
| Unbreaking | `reinforced` |
| Sharpness | `sharpness` |
| Fire Aspect/Flame | `fiery` |
| Power | `power` |
| Punch | `punch` |
| Multishot | `multishot` |
| Quick Charge | `quick_charge` |
| Piercing | `impaling`/`arrow_pierce` theo ID thực tế phải kiểm tra lại |
| Lure | `lure` |
| Loyalty | `returning` |
| Channeling | `channeling` |
| Riptide | `drill_attack` |
| Armor enchantments | Các armor modifiers tương ứng |

Không được hiểu bảng này là danh sách phải mở ngay. Đây chỉ là mapping để chọn subset an toàn.

---

## 6. Recipe condition đã xác minh

TCon4/Mantle đã có helper condition cho recipe:

`Mantle/src/main/java/slimeknights/mantle/recipe/data/IRecipeHelper.java`

```java
default RecipeOutput withCondition(RecipeOutput consumer, ICondition... conditions) {
  return consumer.withConditions(conditions);
}
```

TCon4 đã dùng pattern này ở nhiều compat recipe, ví dụ `ModLoadedCondition("create")`, `ModLoadedCondition("theoneprobe")`, `ModLoadedCondition("headlight")`, `ModLoadedCondition("twilightforest")`.

Vì `BaseRecipeProvider` implement `IRecipeHelper`, `ModifierRecipeProvider` có thể dùng:

```java
RecipeOutput apotheosisConsumer = withCondition(
  consumer,
  new ModLoadedCondition("apotheosis")
);
```

Sau đó `.save(apotheosisConsumer, ...)`.

Điểm quan trọng:

- Không tạo recipe unconditional tham chiếu `apotheosis:sigil_of_enhancement` khi Apotheosis không có.
- Không tự đoán JSON field `conditions` nếu builder/wrapper đã có API thật.
- `AbstractModifierRecipeBuilder` và `ModifierRecipeBuilder` hiện save trực tiếp vào `RecipeOutput`; không có method condition riêng. Dùng `RecipeOutput.withConditions(...)` ở provider layer là pattern đã tồn tại.
- Ingredient item external cần xác minh overload/API tại compile. Có thể tạo `ItemStack`/ingredient từ `Identifier` hoặc dùng registry-safe helper tùy source 26.1; không đoán.

---

## 7. Thiết kế đang đề xuất, chưa được user chốt

### 7.1. Ability marker

Đề xuất tạo dynamic modifier:

```text
tconstruct:apotheosis
```

Đặc điểm dự kiến:

| Thuộc tính | Đề xuất |
|---|---|
| Loại | General ability |
| Level | 1 level duy nhất |
| Slot | Tốn 1 ability slot |
| Behavior | Marker/data gate, không có effect trực tiếp |
| Storage | Modifier thật trong `tic_modifiers` |
| Vanilla enchantment | Không dùng |
| Apoth affix | Không ghi/sửa |
| Unlock effect | Cho phép target modifiers ở level post-cap |

Marker này không được biến thành fake data. Nó phải được add bằng TCon modifier recipe bình thường để `tool.tryValidate()`, slot accounting, save/load và UI chạy theo hệ thống TCon.

### 7.2. Recipe ability

Recipe phải chỉ xuất hiện khi Apotheosis loaded bằng `ModLoadedCondition("apotheosis")`.

Nguyên liệu đề xuất ban đầu là item Apotheosis late-game, ưu tiên:

- `apotheosis:sigil_of_enhancement`
- cộng thêm một vật liệu late-game như `apotheosis:godforged_pearl`, nếu source item/recipe và cân bằng được xác minh

Không chốt ingredient/cost cuối cùng cho đến khi kiểm tra source Apotheosis và ingredient API thật. Không dùng tên item chỉ dựa vào trí nhớ.

### 7.3. Post-cap recipe

Mỗi target modifier cần recipe level band rõ ràng, ví dụ:

- Recipe normal: level 1 đến cap cũ.
- Recipe Apotheosis: level cap cũ + 1 đến extended cap.
- Recipe post-cap vẫn add raw modifier thật.
- Recipe post-cap có nguyên liệu đắt hơn hoặc số lượng lớn hơn để thể hiện progression.
- Recipe post-cap optional theo Apotheosis nếu có reference item Apotheosis.

Không mở vô hạn ở batch đầu. Dùng extended cap hữu hạn tương tự `levelCap` của Apothic Enchanting. Khuyến nghị ban đầu: **cap cũ + 2 level**.

### 7.4. Scope khuyến nghị cho batch đầu

Subset an toàn, gần vanilla enchantment và có scaling tuyến tính:

```text
reinforced
haste
sharpness
smite
bane_of_sssss
knockback
fiery
power
punch
quick_charge
arrow_pierce / impaling – phải xác minh ID chính xác trước
```

Chưa mở ở batch đầu:

```text
luck / fortune / looting
multishot
sweeping
swiftstrike
lure
returning / loyalty
channeling
drill_attack / riptide
slotless modifiers
ability đặc biệt
ammo/inventory modifiers
modifier có probability hoặc phụ thuộc nhiều hook
```

Lý do tạm hoãn `luck`: `luck` có nhiều projection khác nhau cho Fortune, Looting, Sea Luck và armor Luck; `ModifierProvider` dùng `UniqueForLevels(3)`, đồng thời bridge Prosperous hiện tại cũng tác động effective luck. Mở level 4+ phải audit tooltip, formula, event projection và tương tác bridge để không double-count.

Lý do tạm hoãn `multishot`: level trực tiếp thay đổi số projectile/ammo; cần kiểm tra giới hạn gameplay và hiệu năng.

**Trạng thái chốt:** User chưa trả lời xác nhận bảng subset/cap. Không tự tuyên bố đây là lựa chọn cuối nếu user chưa đồng ý.

---

## 8. Các file có khả năng cần sửa

Chỉ sửa sau khi đã so sánh canonical source và kiểm tra API:

| File | Mục đích |
|---|---|
| `src/main/java/slimeknights/tconstruct/tools/data/ModifierIds.java` | Thêm `ModifierIds.apotheosis` nếu dynamic marker cần ID trung tâm |
| `src/main/java/slimeknights/tconstruct/tools/data/ModifierProvider.java` | Đăng ký marker và thêm `ModifierRequirementsModule` cho range post-cap của target modifiers |
| `src/main/java/slimeknights/tconstruct/tools/data/ModifierRecipeProvider.java` | Recipe ability optional và recipe post-cap từng target |
| `src/main/java/slimeknights/tconstruct/common/data/tags/ModifierTagProvider.java` | Đưa marker vào đúng general ability tag; quyết định extraction/salvage behavior |
| `src/main/resources/assets/tconstruct/lang/en_us.json` | Tên/description/requirement của marker và thông báo lỗi nếu cần |
| `src/main/java/slimeknights/tconstruct/tools/data/client/ModifierModelMapProvider.java` | Chỉ sửa nếu marker cần overlay/icon tool; phải kiểm tra model pattern trước |
| `src/main/java/slimeknights/tconstruct/plugin/apotheosis/ApotheosisModifierBridge.java` | Chỉ refactor nếu cần gate bridge cũ; giữ read-only và không regression |
| `src/main/java/slimeknights/tconstruct/library/tools/helper/ModifierUtil.java` | Không mở rộng getter bridge blanket; chỉ sửa khi có bằng chứng cần thiết |

Không hand-edit generated data nếu provider có thể sinh lại. Không stage các file nghiên cứu/log như:

- `tmp-apothic-enchant-javap.txt`
- `tmp-validate-hook-search.txt`
- `tmp-modifier-call-search.txt`
- `tmp-recipe-condition-search.txt`
- `tmp-condition-helper-path.txt`
- `tmp-recipe-helper-search.txt`
- `tmp-optional-ingredient-search.txt`
- các log runtime/build ngoài file log bắt buộc

---

## 9. Vấn đề còn mở cần giải quyết trước khi code

### 9.1. User approval về phạm vi

Câu hỏi đã gửi user:

> Làm bảng khuyến nghị gồm 11 modifier với cap cũ + 2, hay mở rộng thêm `luck/fortune/looting` ngay?

Nếu user chưa trả lời, không tự mở toàn bộ modifier. Phương án mặc định an toàn là subset 11 modifier và cap +2, nhưng vẫn nên báo rõ đây là default đề xuất.

### 9.2. Ingredient/cost chính xác

Phải kiểm tra:

1. Apotheosis source/item IDs ở version đang dùng.
2. Item `sigil_of_enhancement` có phải item dùng cho mục đích này hay chỉ là item khác trong hệ thống Apotheosis.
3. Recipe ingredient API NeoForge 26.1/TCon4.
4. Cân bằng số lượng và leftover.

Không khẳng định Sigil tự động mở generic enchant cap nếu chưa đọc exact source/class. Chỉ dùng nó làm nguyên liệu sau khi xác minh item/recipe phù hợp.

### 9.3. Model/UI marker

TCon model map không tự động thêm overlay cho mọi modifier. Các modifier được liệt kê thủ công theo tool trong `ModifierModelMapProvider`.

Cần quyết định marker `apotheosis`:

- Có overlay/icon như ability bình thường; hoặc
- Là marker data-only nhưng vẫn phải có localization/tooltip đúng, không tạo texture tím/missing model.

Phải kiểm tra pattern của các ability `expanded`, `gilded`, `unbreakable`, `luck`, `melting` trước khi sửa.

### 9.4. Bridge Prosperous

Bridge cũ đang được user chấp nhận. Không xóa hoặc biến nó thành no-op. Nếu marker ability cũng unlock `luck` thì phải tránh:

- cộng Prosperous hai lần;
- bridge effective level lệch raw level;
- requirement check không thấy effective bridge;
- save/load mất modifier thật.

Phương án an toàn ban đầu: giữ bridge cũ độc lập, chưa đưa `luck` vào post-cap batch đầu.

---

## 10. Quy trình implement đề xuất

### Bước A – Xác minh source/API cuối

Đọc lại các vùng liên quan trong canonical TConstruct và TCon4:

- `ModifierProvider` target modules.
- `ModifierRecipeProvider` target recipe caps.
- `AbstractModifierRecipe` validation/apply order.
- `ModifierRequirementsModule` loader/builder.
- `TinkerModifiers` và `AbstractModifierProvider` dynamic registration.
- `ModifierTagProvider` general ability tags.
- `ModifierModelMapProvider` ability model patterns.
- `EnchantmentToModifierProvider` mapping.

Kiểm tra source jar NeoForge 26.1.2 nếu cần Ingredient/condition API. Đọc `Must Read/07_Datagen.md` và tài liệu API recipe/registry liên quan trước sửa.

### Bước B – Thêm marker dynamic

Thêm ID marker, provider definition, tag và localization theo pattern dynamic modifier có sẵn. Không tạo Java behavior class nếu marker chỉ cần làm requirement gate.

### Bước C – Thêm recipe optional

Tạo `RecipeOutput` có `new ModLoadedCondition("apotheosis")`. Dùng builder TCon hiện có. Recipe ability phải tốn 1 ability slot và chỉ add marker level 1.

### Bước D – Thêm requirement + post-cap recipes

Với từng modifier được duyệt:

1. Xác định cap cũ.
2. Xác định extended cap cố định.
3. Thêm requirement module cho range `cap cũ + 1` đến `extended cap`.
4. Nới/tạo recipe range tương ứng.
5. Dùng recipe level band rõ ràng; không cho level vô hạn.
6. Kiểm tra target formula ở level cao không overflow hoặc side effect bất thường.

### Bước E – Datagen/compile

Chạy trên remote Windows session:

```powershell
cd D:\Game\Tcon3\Tcon4
.\gradlew.bat runData *> runData-latest.log
.\gradlew.bat compileJava *> compile-latest.log
.\gradlew.bat build -x test *> build-latest.log
```

Nếu chỉ compile sau thay đổi Java, vẫn cập nhật `compile-latest.log`. Nếu datagen sinh file, kiểm tra diff và không stage rác.

### Bước F – Runtime smoke

Chạy runtime với và không có full Apotheosis stack. User là người test gameplay. Agent chỉ claim build/datagen/runtime startup pass khi log thực sự xác nhận.

---

## 11. Test matrix cho user

| Test | Kết quả mong đợi |
|---|---|
| Chạy TCon4 không có Apotheosis | Game load; không crash; recipe ability/post-cap optional không làm lỗi missing item |
| Chạy với Apotheosis stack đầy đủ | Game load; recipe ability xuất hiện; không NoClassDefFoundError |
| Craft ability `apotheosis` | Tốn đúng 1 ability slot; marker lưu trên tool; tooltip không tím/mất model |
| Nâng target modifier tới cap cũ | Behavior bình thường không thay đổi |
| Nâng target vượt cap khi chưa có ability | Recipe bị từ chối bởi requirement; không ghi level dở dang |
| Add ability rồi nâng target vượt cap | Recipe thành công; raw modifier level tăng thật trong tool |
| Thoát game/load world | Level post-cap và marker còn nguyên; effect vẫn chạy |
| Modifier tool có model | Không texture tím; model overlay đúng pattern TCon |
| Vanilla enchanting table/book | TCon tool vẫn không enchant vanilla; không có vanilla enchant component mới |
| Apotheosis affix/Prosperous | Bridge cũ vẫn hoạt động read-only; không double-count hoặc overwrite NBT |
| Tool thiếu slot | Ability và post-cap recipe tôn trọng slot rule TCon |
| Salvage/extraction | Không tạo cách duplication/cheese ngoài ý muốn; behavior phải được quyết định rõ trong tag/recipe |

Các test gameplay sau đây do user chạy và xác nhận. Agent không được tự nói “hoàn thành” trước khi user nói `ok` hoặc tương đương.

---

## 12. Git/commit handoff

Thay đổi Apotheosis compat sau commit trước chưa được commit/push. Trước commit:

1. Chạy `git status` ở `D:\Game\Tcon3\Tcon4`.
2. Xem diff từng file.
3. Stage chính xác file ability/recipe/provider/resource cần thiết.
4. Không stage `art`, log, temp `.txt`, script thử nghiệm hoặc thay đổi unrelated.
5. Giữ branch `neoforge-26.1.2`.
6. Chỉ commit/push khi user yêu cầu.

Không nhầm project `Tcon3` root với project `Tcon4`.

---

## 13. Kết luận ngắn cho agent tiếp quản

Cơ chế đúng không phải là patch một getter để mọi modifier “ảo” lên level. Cơ chế đúng là:

```text
Apotheosis loaded
    -> recipe optional tạo marker tconstruct:apotheosis
    -> marker chiếm 1 ability slot và lưu như modifier thật
    -> target modifier có recipe post-cap hữu hạn
    -> ModifierRequirementsModule yêu cầu marker ở level post-cap
    -> tool.addModifier ghi raw level thật
    -> tool.tryValidate kiểm tra requirement
    -> mọi TCon hook đọc được level mới
```

Bridge Prosperous hiện tại vẫn giữ vai trò compat read-only cho effective `luck`. Nó không thay thế ability unlock system.

**Trạng thái hiện tại:** Research đã đủ để bắt đầu design/implement sau khi user chốt subset và cap. Chưa có ability `tconstruct:apotheosis`, chưa có post-cap recipe, chưa có tag/localization/model mới được viết cho task này.

---

## 14. TODO compat sau phase Apotheosis cap

User đã xác nhận phase Apotheosis post-cap hiện tại pass. Các hướng compat còn đáng làm, theo thứ tự ưu tiên:

| Ưu tiên | Task | Trạng thái | Ghi chú |
|---|---|---|---|
| 1 | Luck cap role-sensitive | Đã làm | Cap chọn Fortune/Looting/Luck of the Sea theo tool role, dùng max giữa cap Apothic và fallback TCon post-cap để không tụt về vanilla 3. |
| 2 | Apotheosis affix category test sâu | Đã làm | Fishing rod giữ none; melting pan và war pick đã bỏ khỏi override vì không cần Apotheosis can thiệp. Build pass. |
| 3 | Gem socket compat | Đã audit | Không cần bridge riêng: SocketHelper dùng LootCategory.forItem + component sockets/socketed_gems; TCon copyStack giữ component ngoài CUSTOM_DATA. Cần test ingame socket trên item đã map category. |
| 4 | Adventure loot generation | Đã làm | Thêm affix loot entries cho một subset TCon gear hợp lệ: pickaxe, sword, longbow, crossbow, javelin, battlesign, travelers/plate shield, plate armor set. Stack có `minecraft:custom_data.tic_materials` để TCon rebuild stats thay vì sinh bare item lỗi. Cần test ingame loot/reforge roll. |
| 5 | Tooltip polish | Đã làm | Requirement tooltip giờ hiển thị “Requires Apotheosis ability for level VI+” dạng roman min-level thay vì số raw/từng level. |
| 6 | JEI grouping | Đã làm | Modifier JEI sort giữ thứ tự theo slot như cũ nhưng gom recipe `tools/modifiers/upgrade/apotheosis/` thành một cụm, tránh hậu cap xen lẫn recipe thường. |
| 7 | Apothic Attributes bridge | Đã audit / không làm code | Không thêm bridge attribute vì Apotheosis đã apply affix/gem attributes qua `StackAttributeModifiersEvent` (`AdventureEvents.affixModifiers`). TCon chỉ cần LootCategory/data component compat; bridge riêng sẽ double-count. |
| 8 | Config toggle | Đã làm | Thêm common config `apotheosis_prosperous_bridge` cho bridge Prosperous→Luck và `apotheosis_post_cap_recipes` cho ability/post-cap recipes. Data map/affix loot entries giữ datapack-driven để pack maker override bằng datapack. |

---

## References

[1]: `D:\Game\Tcon3\AGENT.md` – hướng dẫn agent workspace.

[2]: `D:\Game\Tcon3\PORTING_RULEBOOK.md` – quy tắc port NeoForge 26.1.

[3]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\library\recipe\modifiers\adding\AbstractModifierRecipe.java` – recipe level validation và apply order.

[4]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\library\recipe\modifiers\adding\ModifierRecipe.java` – add modifier rồi `tool.tryValidate()`.

[5]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\library\modifiers\modules\build\ModifierRequirementsModule.java` – requirement theo modifier và level range.

[6]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\tools\data\ModifierProvider.java` – module/formula/level display của modifier.

[7]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\tools\data\ModifierRecipeProvider.java` – cap và recipe modifier.

[8]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\tools\data\EnchantmentToModifierProvider.java` – mapping enchantment vanilla sang TCon modifier.

[9]: `D:\Game\Tcon3\Mantle\src\main\java\slimeknights\mantle\recipe\data\IRecipeHelper.java` – `RecipeOutput.withConditions(...)` helper.

[10]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\plugin\apotheosis\ApotheosisModifierBridge.java` – bridge Prosperous read-only hiện tại.

[11]: `D:\Game\Tcon3\Tcon4\src\main\java\slimeknights\tconstruct\library\tools\helper\ModifierUtil.java` – effective modifier accessor có bridge.

[12]: `D:\Game\Tcon3\Tcon4\src\main\resources\data\apotheosis\data_maps\item\loot_category_overrides.json` – Apotheosis category compat đã làm.

[13]: `https://github.com/Shadows-of-Fire/Apotheosis` – source Apotheosis 26.1.

[14]: `https://modrinth.com/mod/apotheosis/version/26.1.2-9.0.3` – Apotheosis runtime release đã kiểm tra.

[15]: `/home/ubuntu/apotheosis-compat-findings.md` – research notes Apotheosis/Apothic Enchanting đã xác minh.

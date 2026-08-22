# Kế hoạch làm projectile 3D thật cho Javelin

## 1. Mục tiêu

Tạo projectile Javelin là **một model 3D duy nhất**, có độ dày thật, nhìn rõ từ mọi góc, giữ đúng material tint của Tinkers Construct và không thay đổi physics hoặc behavior gốc.

Model mới không dùng hai mặt phẳng chồng nhau. Cách cross-plane đã gây ra hình chữ X và không được sử dụng lại.

## 2. Phạm vi không thay đổi

Các phần sau phải giữ nguyên:

- Launch velocity.
- Inaccuracy và quỹ đạo.
- Hit detection.
- Pickup behavior.
- Durability.
- Ammo behavior.
- Entity physics.
- Hand animation khi giữ và ném.
- Modifier và material behavior.

Chỉ thay phần **hình học và đường render projectile**.

## 3. Giai đoạn thực hiện

### Giai đoạn 1: Khảo sát workspace và model hiện tại

Đọc lại `AGENT.md` và `PORTING_RULEBOOK.md` trước khi sửa. Kiểm tra các file:

- `ThrownToolRenderer.java`.
- `ThrownToolRenderState.java`.
- `ThrownTool.java`.
- `ToolProjectile.java`.
- Model Javelin base.
- Model Javelin throwing.
- Tool model loader và material model.

Mục tiêu là xác định chính xác model hiện tại lấy texture, part và material tint từ đâu.

### Giai đoạn 2: Đối chiếu upstream và API NeoForge

So sánh renderer và model Javelin của TCon4 với upstream. Kiểm tra API NeoForge 26.1 liên quan đến:

- `ItemStackRenderState`.
- `SubmitNodeCollector`.
- `PoseStack`.
- `CameraRenderState`.
- Culling và bounding box của entity renderer.
- `tconstruct:tool` model loader.

Không đoán API. Nếu API local khác upstream thì phải dùng chữ ký và behavior của NeoForge 26.1.2 local.

### Giai đoạn 3: Thiết kế hình học 3D

Thiết kế projectile low-poly theo phong cách Minecraft, gồm:

- Shaft có độ dày thật.
- Mũi Javelin có phần thân và mặt bên.
- Phần đuôi hoặc chuôi có geometry riêng nếu model gốc hỗ trợ.
- Các mặt bên đủ để nhìn thấy khi nhìn nghiêng.
- Tâm model nằm đúng trục entity để không bị lệch khi bay.

Ưu tiên cuboid/prism 3D đơn giản, ổn định và nhẹ. Không tạo hai bản Javelin độc lập để giả lập 3D.

### Giai đoạn 4: Giữ material tint động

Thử phương án ưu tiên trước:

1. Tạo model projectile riêng.
2. Dùng loader `tconstruct:tool` nếu loader hỗ trợ các part 3D cần thiết.
3. Dùng texture/material dynamic hiện tại của Tinkers Construct.
4. Nối model riêng vào `ThrownToolRenderer` bằng đúng display context projectile.

Nếu `tconstruct:tool` chỉ sinh quad phẳng và không thể tạo geometry cần thiết, dùng phương án dự phòng:

- Dựng mesh low-poly trực tiếp trong `ThrownToolRenderer`.
- Lấy đúng sprite/material từ tool state hiện tại.
- Giữ tint material và modifier texture.
- Không tô màu cố định.
- Không submit thêm một bản Javelin thứ hai.

### Giai đoạn 5: Nối renderer

`ThrownToolRenderer` chỉ submit **một projectile 3D**.

Giữ nguyên:

- Rotation theo `xRot` và `yRot` của entity.
- Offset hiện tại nếu không gây lệch tâm.
- Light coordinates.
- Overlay và outline.
- Cách gọi renderer cha.

Không sửa `ThrowingModule`, `ThrownTool` physics hoặc launch code nếu không có bằng chứng lỗi liên quan.

### Giai đoạn 6: Kiểm tra render kỹ thuật

Kiểm tra các trạng thái sau:

| Trạng thái | Kết quả cần đạt |
|---|---|
| Nhìn trực diện | Thấy đầy đủ mũi và thân Javelin |
| Nhìn nghiêng 90 độ | Vẫn thấy độ dày 3D, không biến mất |
| Nhìn từ phía sau | Không có mặt phẳng phụ hoặc hình duplicate |
| Bay lên | Trục model đi theo hướng bay |
| Bay xuống | Không bị lật sai hoặc lệch tâm |
| Bay sát mặt đất | Không nằm dài sai trục |
| Projectile bị block bắt | Model không giật hoặc tách khỏi entity |
| Projectile bị entity bắt | Model vẫn đúng orientation |
| Nhiều material | Tint material đúng |
| Modifier texture | Texture modifier không mất |
| First-person | Không ảnh hưởng animation cầm/nạp/ném |

### Giai đoạn 7: Build và đọc log

Sau khi sửa:

```powershell
Set-Location 'D:\Game\Tcon3\Tcon4'
.\gradlew.bat runClientData
.\gradlew.bat processResources
.\gradlew.bat compileJava
.\gradlew.bat build
```

Nếu có lỗi network khi tải NeoForm, ghi rõ và dùng artifact local đã cache để kiểm tra source. Không coi build là hoàn toàn xác nhận nếu bước bắt buộc bị bỏ qua.

Đọc log để kiểm tra:

- Model loading error.
- Missing texture.
- Missing sprite.
- Culling warning.
- Duplicate client extension.
- Renderer exception.
- Class loading error.
- Crash trong lúc khởi động hoặc lúc projectile được render.

### Giai đoạn 8: Test trong game

User chạy:

```powershell
Set-Location 'D:\Game\Tcon3\Tcon4'
.\gradlew.bat runClient
```

Test tối thiểu:

1. Tạo Javelin bằng ít nhất ba material khác nhau.
2. Giữ chuột phải ở góc nhìn thứ nhất để xác nhận hand animation vẫn đúng.
3. Ném Javelin theo phương ngang, lên và xuống.
4. Quan sát projectile từ phía trước, phía sau và góc 90 độ.
5. Ném sát mặt đất để kiểm tra model không bị kéo dài sai hướng.
6. Kiểm tra projectile va vào block và entity.
7. Kiểm tra material tint và modifier texture.
8. Kiểm tra không có hình chữ X hoặc bản Javelin thứ hai.

Chỉ coi task hoàn tất sau khi user xác nhận model 3D nhìn đúng trong game.

## 4. Tiêu chí hoàn thành

Task đạt khi tất cả điều kiện sau đúng:

- Projectile là geometry 3D thật, không phải cross-plane.
- Chỉ render một bản Javelin.
- Nhìn nghiêng vẫn thấy được thân và mũi.
- Model xoay đúng theo hướng bay.
- Material tint động vẫn đúng.
- Modifier texture vẫn đúng.
- Không có crash hoặc lỗi model mới.
- Physics và quỹ đạo không bị thay đổi.
- First-person hand animation vẫn hoạt động.
- `compileJava` và build jar pass.
- User test trong `runClient` và xác nhận OK.

## 5. Nguyên tắc an toàn

Không sửa trực tiếp `tinkers-levelling-addon`. Không tạo lại `todo.md` hoặc `todonext.md`. Không dùng `Set-Content` vì có thể tạo BOM; khi cần ghi file bằng PowerShell phải dùng UTF-8 không BOM. Không đánh dấu hoàn thành trước khi user xác nhận rõ ràng.

package com.example.modulelaptrinh;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProgrammingChatMessageTest {
    @Test
    // Hàm `startsWithoutImagesAndCanAttachProgrammingScreenshots()`: Thực hiện chức năng `startsWithoutImagesAndCanAttachProgrammingScreenshots` của class này.
    public void startsWithoutImagesAndCanAttachProgrammingScreenshots() {
        // Khởi tạo đối tượng/nội dung tin nhắn phục vụ cho bước xử lý tiếp theo.
        ProgrammingChatMessage message = new ProgrammingChatMessage("Giải thích lỗi Java", true);

        // Gọi hàm `assertTrue()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertTrue(message.isUser());
        // Gọi hàm `assertFalse()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertFalse(message.hasImages());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(0, message.getImageCount());
        // Gọi hàm `assertNull()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertNull(message.getFirstImage());

        // Gọi `addImage()` trên `message` để thực hiện thao tác tương ứng.
        message.addImage("base64-code-screenshot");
        // Gọi hàm `assertTrue()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertTrue(message.hasImages());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(1, message.getImageCount());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("base64-code-screenshot", message.getFirstImage());
    }

    @Test
    // Hàm `nullImageListsAreHandledSafely()`: Thực hiện chức năng `nullImageListsAreHandledSafely` của class này.
    public void nullImageListsAreHandledSafely() {
        // Khởi tạo đối tượng/nội dung tin nhắn phục vụ cho bước xử lý tiếp theo.
        ProgrammingChatMessage message = new ProgrammingChatMessage("Phân tích code", false, null);
        // Gọi hàm `assertFalse()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertFalse(message.hasImages());

        // Gọi `setImageBase64List()` để cập nhật thuộc tính/trạng thái của `message`.
        message.setImageBase64List(Arrays.asList("img-1", "img-2"));
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals(2, message.getImageCount());
        // Gọi hàm `assertEquals()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertEquals("img-1", message.getFirstImage());

        // Gọi `setImageBase64List()` để cập nhật thuộc tính/trạng thái của `message`.
        message.setImageBase64List(null);
        // Gọi hàm `assertFalse()` để thực hiện bước xử lý tiếp theo trong luồng hiện tại.
        assertFalse(message.hasImages());
    }
}

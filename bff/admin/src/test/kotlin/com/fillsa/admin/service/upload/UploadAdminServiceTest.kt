package com.fillsa.admin.service.upload

import com.fillsa.admin.common.storage.R2UploadClient
import com.fillsa.util.exception.BusinessException
import com.fillsa.util.exception.ErrorCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class UploadAdminServiceTest {

    private val r2UploadClient = mockk<R2UploadClient>()
    private val sut = UploadAdminService(r2UploadClient)

    private val pngBytes = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01)
    private val jpegBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), 0x00)

    @Test
    fun `PNG 업로드 - png 확장자와 image-png 로 저장하고 URL 을 반환`() {
        val key = slot<String>()
        val contentType = slot<String>()
        every { r2UploadClient.put(capture(key), any(), capture(contentType)) } answers {
            "https://img.example.com/${key.captured}"
        }

        val result = sut.uploadImage(pngBytes)

        assertThat(key.captured).startsWith("admin/images/").endsWith(".png")
        assertThat(contentType.captured).isEqualTo("image/png")
        assertThat(result.url).isEqualTo("https://img.example.com/${key.captured}")
    }

    @Test
    fun `JPEG 업로드 - jpg 확장자와 image-jpeg 로 저장`() {
        val key = slot<String>()
        val contentType = slot<String>()
        every { r2UploadClient.put(capture(key), any(), capture(contentType)) } returns "https://img.example.com/x"

        sut.uploadImage(jpegBytes)

        assertThat(key.captured).startsWith("admin/images/").endsWith(".jpg")
        assertThat(contentType.captured).isEqualTo("image/jpeg")
    }

    @Test
    fun `빈 파일은 400 - 스토리지를 호출하지 않는다`() {
        assertThatThrownBy { sut.uploadImage(ByteArray(0)) }
            .isInstanceOfSatisfying(BusinessException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.INVALID_REQUEST)
            }
        verify(exactly = 0) { r2UploadClient.put(any(), any(), any()) }
    }

    @Test
    fun `PNG JPEG 가 아니면 400 - GIF SVG 텍스트 위장 파일 모두 거부`() {
        val gif = "GIF89a".toByteArray()
        val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>".toByteArray()
        // 확장자·Content-Type 을 png 로 속여도 내용이 PNG 가 아니면 거부
        val fakePng = "not really a png".toByteArray()

        listOf(gif, svg, fakePng).forEach { bytes ->
            assertThatThrownBy { sut.uploadImage(bytes) }
                .isInstanceOfSatisfying(BusinessException::class.java) {
                    assertThat(it.errorCode).isEqualTo(ErrorCode.INVALID_REQUEST)
                    assertThat(it.message).contains("PNG, JPEG")
                }
        }
        verify(exactly = 0) { r2UploadClient.put(any(), any(), any()) }
    }

    @Test
    fun `4MB 초과는 400`() {
        val tooBig = pngBytes + ByteArray(UploadAdminService.MAX_IMAGE_BYTES)

        assertThatThrownBy { sut.uploadImage(tooBig) }
            .isInstanceOfSatisfying(BusinessException::class.java) {
                assertThat(it.errorCode).isEqualTo(ErrorCode.INVALID_REQUEST)
                assertThat(it.message).contains("4MB")
            }
        verify(exactly = 0) { r2UploadClient.put(any(), any(), any()) }
    }

    @Test
    fun `정확히 4MB 는 허용`() {
        val exactly = pngBytes + ByteArray(UploadAdminService.MAX_IMAGE_BYTES - pngBytes.size)
        every { r2UploadClient.put(any(), any(), any()) } returns "https://img.example.com/ok"

        assertThat(sut.uploadImage(exactly).url).isEqualTo("https://img.example.com/ok")
    }
}

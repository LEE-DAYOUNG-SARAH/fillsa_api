package store.fillsa.fillsa_api.common.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.web.multipart.MultipartFile
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.storage.useCase.StorageUseCase

@ExtendWith(MockitoExtension::class)
class FileServiceTest {
    @Mock
    lateinit var mockStorage: StorageUseCase

    @InjectMocks
    lateinit var sut: FileService

    private lateinit var mockFile: MultipartFile

    @BeforeEach
    fun setUp() {
        mockFile = mock { MultipartFile::class.java }
    }

    @Test
    fun `파일 스토리지 업로드 성공 시 URL 을 반환한다`() {
        // given
        val path = "test-path"
        val filename = "file.txt"
        val contentType = "text/plain"
        val bytes = byteArrayOf(1, 2, 3)
        val expectedUrl = "https://bucket/test-path/file.txt"

        `when`(mockFile.bytes).thenReturn(bytes)
        `when`(mockFile.originalFilename).thenReturn(filename)
        `when`(mockFile.contentType).thenReturn(contentType)
        `when`(mockStorage.upload(path, bytes, filename, contentType)).thenReturn(expectedUrl)

        // when
        val result = sut.uploadFile(path, mockFile)

        // then
        assertEquals(expectedUrl, result)
        verify(mockStorage, times(1)).upload(path, bytes, filename, contentType)
    }

    @Test
    fun `파일 스토리지 업로드 오류 시 FileException을 던진다`() {
        // given
        val path = "test-path"
        `when`(mockFile.bytes).thenReturn(byteArrayOf())
        `when`(mockFile.originalFilename).thenReturn(null)
        `when`(mockFile.contentType).thenReturn(null)
        doThrow(RuntimeException("fail")).`when`(mockStorage).upload(anyString(), any(), anyString(), anyString())

        // when & then
        assertThrows(BusinessException::class.java) {
            sut.uploadFile(path, mockFile)
        }
    }

    @Test
    fun `파일 스토리지 수정 성공 시 새로운 URL을 반환한다`() {
        // given
        val path = "test-path"
        val filename = "file.png"
        val contentType = "image/png"
        val bytes = byteArrayOf(9, 8, 7)
        val oldUrl = "https://bucket/test-path/old.png"
        val newUrl = "https://bucket/test-path/new.png"

        `when`(mockFile.bytes).thenReturn(bytes)
        `when`(mockFile.originalFilename).thenReturn(filename)
        `when`(mockFile.contentType).thenReturn(contentType)
        `when`(mockStorage.update(path, bytes, filename, contentType, oldUrl)).thenReturn(newUrl)

        // when
        val result = sut.updateFile(path, mockFile, oldUrl)

        // then
        assertEquals(newUrl, result)
        verify(mockStorage, times(1)).update(path, bytes, filename, contentType, oldUrl)
    }

    @Test
    fun `파일 스토리지 삭제를 호출한다`() {
        // given
        val fileUrl = "https://bucket/test-path/file.txt"
        doNothing().`when`(mockStorage).delete(fileUrl)

        // when
        sut.deleteFile(fileUrl)

        // then
        verify(mockStorage, times(1)).delete(fileUrl)
    }

    @Test
    fun `파일 스토리지 삭제 오류 시 FileException을 던진다`() {
        // given
        val fileUrl = "https://bucket/test-path/file.txt"
        doThrow(RuntimeException("err")).`when`(mockStorage).delete(fileUrl)

        // when & then
        assertThrows(BusinessException::class.java) {
            sut.deleteFile(fileUrl)
        }
    }
}

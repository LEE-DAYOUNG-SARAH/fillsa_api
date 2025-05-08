package store.fillsa.fillsa_api.common.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import store.fillsa.fillsa_api.common.exception.ErrorCode.*
import store.fillsa.fillsa_api.common.exception.BusinessException
import store.fillsa.fillsa_api.common.storage.useCase.StorageUseCase

@Service
class FileService(
    private val storage: StorageUseCase
) {
    private val log = KotlinLogging.logger {  }

    fun uploadFile(path: String, file: MultipartFile): String = try {
        storage.upload(
            path,
            file.bytes,
            file.originalFilename ?: "unknown",
            file.contentType ?: "application/octet-stream"
        )
    } catch (e: Exception) {
        log.error(e) { "파일 업로드 실패" }
        throw BusinessException(FILE_UPLOAD_FAILED)
    }

    fun updateFile(path: String, file: MultipartFile, oldUrl: String): String = try {
        storage.update(
            path,
            file.bytes,
            file.originalFilename ?: "unknown",
            file.contentType ?: "application/octet-stream",
            oldUrl
        )
    } catch (e: Exception) {
        log.error(e) { "파일 수정 실패" }
        throw BusinessException(FILE_UPDATE_FAILED)
    }

    fun deleteFile(fileUrl: String) = try {
        storage.delete(fileUrl)
    } catch (e: Exception) {
        log.error(e) { "파일 삭제 실패" }
        throw BusinessException(FILE_DELETE_FAILED)
    }
}
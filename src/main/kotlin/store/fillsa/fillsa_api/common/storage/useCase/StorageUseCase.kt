package store.fillsa.fillsa_api.common.storage.useCase

interface StorageUseCase {
    /**
     * 파일 업로드 후 외부에서 접근 가능한 URL 반환
     */
    fun upload(path: String, bytes: ByteArray, filename: String, contentType: String): String

    /**
     * 기존 파일 대체(업로드+삭제) 후 새 URL 반환
     */
    fun update(path: String, bytes: ByteArray, filename: String, contentType: String, oldFileUrl: String): String

    /**
     * 파일 삭제
     */
    fun delete(fileUrl: String)
}
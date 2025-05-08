package store.fillsa.fillsa_api.common.wrapper

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class CustomHttpServletRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val cachedBody: ByteArray

    init {
        cachedBody = StreamUtils.copyToByteArray(request.inputStream)
    }

    override fun getInputStream(): ServletInputStream {
        val cachedInputStream = ByteArrayInputStream(cachedBody)
        
        return object : ServletInputStream() {
            override fun read(): Int = cachedInputStream.read()
            
            override fun isFinished(): Boolean = cachedInputStream.available() == 0
            
            override fun isReady(): Boolean = true
            
            override fun setReadListener(listener: ReadListener?) {
                throw UnsupportedOperationException("ReadListener is not supported")
            }
        }
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(this.inputStream))
    }

    fun getCachedBody(): ByteArray {
        return cachedBody.clone()
    }
}
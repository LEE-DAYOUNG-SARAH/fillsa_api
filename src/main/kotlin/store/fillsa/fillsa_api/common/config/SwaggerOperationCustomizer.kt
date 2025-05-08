package store.fillsa.fillsa_api.common.config

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode
import org.springframework.http.MediaType as SpringMediaType

@Component
class SwaggerOperationCustomizer : OperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        handlerMethod.getMethodAnnotation(ApiErrorResponses::class.java)
            ?.let { ann ->
                val codes = ann.values
                val grouped = codes.groupBy { it.httpStatus }
                val responses: ApiResponses = operation.responses

                grouped.forEach { (httpStatus, list) ->
                    val apiResp = ApiResponse().apply {
                        description = list.joinToString { it.message }
                        content = createContent(list)
                    }

                    responses.addApiResponse(httpStatus.value().toString(), apiResp)
                }
            }
        return operation
    }

    private fun createContent(list: List<ErrorCode>): Content {
        return Content().apply {
            addMediaType(
                SpringMediaType.APPLICATION_JSON_VALUE,
                createMediaType(list)
            )
        }
    }

    private fun createMediaType(list: List<ErrorCode>): MediaType {
        return MediaType().apply {
            list.forEach { ec ->
                addExamples(ec.name, Example().apply {
                    value = SwaggerErrorResponse(ec.httpStatus.value(), ec.code, ec.message)
                })
            }
        }
    }
}

data class SwaggerErrorResponse(
    val httpStatus: Int,
    val errorCode: Int,
    val message: String
)

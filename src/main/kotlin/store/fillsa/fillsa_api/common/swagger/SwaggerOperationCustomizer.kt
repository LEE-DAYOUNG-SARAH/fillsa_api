package store.fillsa.fillsa_api.common.swagger

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import store.fillsa.fillsa_api.common.exception.ApiErrorResponses
import store.fillsa.fillsa_api.common.exception.ErrorCode
import store.fillsa.fillsa_api.common.exception.ErrorResponse
import org.springframework.http.MediaType as SpringMediaType

@Component
class SwaggerOperationCustomizer : OperationCustomizer {

    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        applyApiErrorResponses(operation, handlerMethod)
        applySecurityIfNeeded(operation, handlerMethod)
        applyAppVersionHeader(operation)
        return operation
    }

    private fun applyApiErrorResponses(operation: Operation, handlerMethod: HandlerMethod) {
        handlerMethod.getMethodAnnotation(ApiErrorResponses::class.java)
            ?.let { ann ->
                val codes = ann.values
                val grouped = codes.groupBy { it.httpStatus }
                val responses: ApiResponses = operation.responses

                grouped.forEach { (httpStatus, list) ->
                    val apiResponse = createApiResponse(list)
                    responses.addApiResponse(httpStatus.value().toString(), apiResponse)
                }
            }
    }

    private fun applySecurityIfNeeded(operation: Operation, handlerMethod: HandlerMethod) {
        val hasSecurity = handlerMethod.method.parameters.any { parameter ->
            parameter.type == Authentication::class.java ||
                parameter.getAnnotation(AuthenticationPrincipal::class.java) != null
        }

        if (hasSecurity) {
            operation.addSecurityItem(SecurityRequirement().addList(SECURITY_SCHEME_NAME))
        }
    }


    private fun createApiResponse(list: List<ErrorCode>) = ApiResponse().apply {
        description = list.joinToString { it.message }
        content = createContent(list)
    }

    private fun createContent(list: List<ErrorCode>) = Content().apply {
        addMediaType(
            SpringMediaType.APPLICATION_JSON_VALUE,
            createMediaType(list)
        )
    }

    private fun createMediaType(list: List<ErrorCode>) = MediaType().apply {
        list.forEach { errorCode ->
            addExamples(errorCode.name, createExample(errorCode))
        }
    }

    private fun createExample(errorCode: ErrorCode) = Example().apply {
        value = ErrorResponse.from(errorCode.httpStatus, errorCode, errorCode.message)
    }

    private fun applyAppVersionHeader(operation: Operation) {
        val existing = operation.parameters.orEmpty()
        val alreadyExists = existing.any { it.name == "X-App-Version" }

        if (!alreadyExists) {
            val versionHeader = Parameter()
                .`in`(ParameterIn.HEADER.toString())
                .name("X-App-Version")
                .description("앱 버전 (예: 1.2.10)")
                .required(true)
                .example("9999")

            operation.addParametersItem(versionHeader)
        }
    }

}

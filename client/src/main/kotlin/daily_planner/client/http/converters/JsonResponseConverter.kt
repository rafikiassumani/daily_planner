package daily_planner.client.http.converters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.linecorp.armeria.common.*
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.annotation.ResponseConverterFunction

class JsonResponseConverter: ResponseConverterFunction {
    companion object {
        fun getMapper(): ObjectMapper {
            return ObjectMapper()
                .registerKotlinModule()
                .registerModule(JavaTimeModule())
                .setDateFormat(StdDateFormat())
        }
    }
    override fun convertResponse(
        ctx: ServiceRequestContext,
        headers: ResponseHeaders,
        result: Any?,
        trailers: HttpHeaders
    ): HttpResponse {

        if (result == null || ctx.request().method() == HttpMethod.DELETE ) {
            return HttpResponse.of(HttpStatus.NO_CONTENT)
        }

        val responseHeaders: ResponseHeaders = if (headers.contentType() == null) {
            headers.toBuilder().contentType(MediaType.JSON_UTF_8).build()
        } else {
            headers
        }

        val httpData: HttpData = HttpData.wrap(getMapper().writeValueAsBytes(result))
        return HttpResponse.of(responseHeaders, httpData, trailers)
    }
}
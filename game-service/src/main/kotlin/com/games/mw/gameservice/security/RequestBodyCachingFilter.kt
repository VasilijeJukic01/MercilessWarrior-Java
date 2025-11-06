package com.games.mw.gameservice.security

import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

const val CACHED_REQUEST_BODY_BYTES_ATTR = "cachedRequestBodyBytes"

@Component
@Order(-100)
class RequestBodyCachingFilter : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val method = exchange.request.method
        val contentType = exchange.request.headers.contentType

        if ((method == HttpMethod.POST || method == HttpMethod.PUT) && contentType == MediaType.APPLICATION_JSON) {
            return DataBufferUtils.join(exchange.request.body)
                .flatMap { dataBuffer ->
                    val bytes = ByteArray(dataBuffer.readableByteCount())
                    dataBuffer.read(bytes)
                    DataBufferUtils.release(dataBuffer)

                    exchange.attributes[CACHED_REQUEST_BODY_BYTES_ATTR] = bytes

                    val mutatedRequest: ServerHttpRequest = object : ServerHttpRequestDecorator(exchange.request) {
                        override fun getBody(): Flux<DataBuffer> {
                            return Flux.just(exchange.response.bufferFactory().wrap(bytes))
                        }
                    }
                    chain.filter(exchange.mutate().request(mutatedRequest).build())
                }
        }
        return chain.filter(exchange)
    }

}
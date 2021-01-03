/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.selenium

import io.github.karlatemp.mxlib.MxLib
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.*
import io.netty.util.AttributeKey
import org.littleshoot.proxy.HttpFilters
import org.littleshoot.proxy.HttpFiltersAdapter
import org.littleshoot.proxy.HttpFiltersSourceAdapter
import org.littleshoot.proxy.impl.DefaultHttpProxyServer
import org.littleshoot.proxy.mitm.Authority
import org.littleshoot.proxy.mitm.CertificateSniffingMitmManager
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.util.zip.GZIPInputStream

internal class ProxyServer(
    val requestHandlers: (uri: String) -> Boolean,
    val editPredicate: (HttpResponse) -> Boolean,
    val responseProcessor: (ResponseProcessor) -> Unit,
    val port: Int,
) : Closeable {
    internal class ResponseProcessor : ByteArrayOutputStream {
        val response: HttpResponse
        val uri: String

        constructor(response: HttpResponse, uri: String) : super() {
            this.response = response
            this.uri = uri
        }

        constructor(size: Int, response: HttpResponse, uri: String) : super(size) {
            this.response = response
            this.uri = uri
        }

        var count: Int
            get() = super.count
            set(value) {
                super.count = value
            }
        var buf: ByteArray
            get() = super.buf
            set(value) {
                super.buf = value
            }


        fun copyAndReset(): ByteArray {
            val result = ByteArray(count)
            System.arraycopy(buf, 0, result, 0, count)
            count = 0
            return result
        }

        var valueAsString: String
            get() = String(buf, 0, count, Charsets.UTF_8)
            set(value) {
                buf = value.toByteArray(Charsets.UTF_8).also {
                    count = it.size
                }
            }

        internal fun complete() {
            when (response.headers()["Content-Encoding"]?.toLowerCase()) {
                "gzip" -> {
                    GZIPInputStream(ByteArrayInputStream(copyAndReset())).use {
                        it.copyTo(this)
                    }
                }
            }
        }
    }

    val server = DefaultHttpProxyServer.bootstrap()
        .withManInTheMiddle(MITM)
        .withPort(port)
        .withFiltersSource(object : HttpFiltersSourceAdapter() {
            private val CONNECTED_URL: AttributeKey<String> = AttributeKey.valueOf("connected_url")

            override fun filterRequest(
                originalRequest: HttpRequest,
                ctx: ChannelHandlerContext?
            ): HttpFilters? {

                val uri = originalRequest.uri()

                if (originalRequest.method() === HttpMethod.CONNECT) {
                    if (ctx != null) {
                        val prefix = "https://" + uri.replaceFirst(":443$".toRegex(), "")
                        ctx.channel().attr(CONNECTED_URL).set(prefix)
                        if (!requestHandlers(prefix)) {
                            return null
                        }
                    }
                    return HttpFiltersAdapter(originalRequest, ctx)
                }
                val connectedUrl = ctx!!.channel().attr(CONNECTED_URL).get().let {
                    if (it == null) uri
                    else it + uri
                }
                if (!requestHandlers(connectedUrl)) {
                    return null
                }
                if (connectedUrl.contains("google.com")) return null

                return object : HttpFiltersAdapter(originalRequest, ctx) {

                    override fun proxyToServerRequest(httpObject: HttpObject?): HttpResponse? {
                        if (httpObject is HttpRequest) {
                            httpObject.headers().set("User-Agent", UserAgent)
                        }
                        return super.proxyToServerRequest(httpObject)
                    }

                    var responses: MutableList<ByteBuf>? = null
                    var rep: HttpResponse? = null
                    override fun proxyToClientResponse(httpObject: HttpObject?): HttpObject? {
                        if (httpObject is HttpResponse) {
                            if (editPredicate(httpObject)) {
                                val headers = httpObject.headers()
                                rep = httpObject
                                responses = mutableListOf()
                                headers["Transfer-Encoding"] = "chunked"
                                headers.remove("Content-Length")
                            }
                        }
                        responses?.let { responses ->
                            if (httpObject is HttpContent) {
                                if (httpObject is LastHttpContent) {
                                    val resp = ResponseProcessor(
                                        httpObject.content().readableBytes() + responses.sumOf { it.readableBytes() },
                                        rep!!,
                                        connectedUrl
                                    )
                                    responses.forEach { bytes ->
                                        bytes.readBytes(resp, bytes.readableBytes())
                                    }
                                    httpObject.content().copy().let {
                                        it.readBytes(resp, it.readableBytes())
                                    }
                                    resp.complete()
                                    responseProcessor(resp)
                                    return DefaultLastHttpContent(
                                        Unpooled.wrappedBuffer(resp.buf, 0, resp.count),
                                    )
                                } else {
                                    responses.add(httpObject.content().copy())
                                }
                                return DefaultHttpContent(Unpooled.EMPTY_BUFFER)
                            }
                        }
                        return httpObject
                    }
                }
            }
        })
        .start()

    companion object {
        val dataStorage = MxLib.getDataStorage().resolve("mirai-selenium").also {
            it.mkdirs()
        }


        @Suppress("SpellCheckingInspection")
        val MITM = CertificateSniffingMitmManager(
            Authority(
                dataStorage,
                "littleproxy-mitm",
                "Karlatemp".toCharArray(),
                "Mamoe",
                "Certificate Authority",
                "Mamoe",
                "Mamoe",
                "Mamoe"
            )
        )
    }

    override fun close() {
        server.stop()
    }
}
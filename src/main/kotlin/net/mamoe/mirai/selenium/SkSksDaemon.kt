/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.selenium

import kotlinx.coroutines.CompletableDeferred
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.io.Closeable
import java.util.concurrent.atomic.AtomicBoolean

internal val scriptInject by lazy {
    """<script>$script</script><script"""
        .replaceFirst("\${MIRAI_SELENIUM-USERAGENT}", UserAgent)
}

internal class SkSksDaemon(
    port: Int,
    def: CompletableDeferred<String?>,
    shouldContinue: AtomicBoolean
) : Closeable {
    val server = ProxyServer(
        requestHandlers = {
            "qq.com" in it
        },
        editPredicate = { response ->
            val headers = response.headers()
            val ctype = headers["Content-Type"]
            ctype == "text/html" || ctype == "application/json"
        },
        responseProcessor = { rep ->
            when (rep.response.headers()["Content-Type"]) {
                "application/json" -> {
                    if (rep.uri == "https://t.captcha.qq.com/cap_union_new_verify") {
                        val jso = Json.decodeFromString(JsonObject.serializer(), rep.valueAsString)
                        def.complete((jso["ticket"] as? JsonPrimitive)?.content)
                        shouldContinue.set(false)
                    }
                }
                "text/html" -> {
                    rep.valueAsString = rep.valueAsString.replaceFirst(
                        "<script", scriptInject
                    )
                }
            }
        },
        port = port
    )

    override fun close() {
        server.close()
    }
}
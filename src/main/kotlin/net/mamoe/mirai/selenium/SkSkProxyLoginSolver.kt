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
import kotlinx.coroutines.delay
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import java.net.InetAddress
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

internal class SkSkProxyLoginSolver(
    private val delegate: LoginSolver
) : LoginSolver() {
    override val isSliderCaptchaSupported: Boolean get() = true

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return delegate.onSolvePicCaptcha(bot, data)
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return delegate.onSolveUnsafeDeviceLoginVerify(bot, url)
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? = executeSksks(url)
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun eprintln(msg: String) = System.err.println(msg)

internal val sksksPort by lazy {
    val file = ProxyServer.dataStorage.resolve("server-port.properties")
    if (!file.isFile) {
        file.writeText(
            """
            port=25671
        """.trimIndent()
        )
    }
    val properties = Properties()
    runCatching {
        file.bufferedReader().use { properties.load(it) }
    }.exceptionOrNull()?.printStackTrace()
    properties.getProperty("port", "25671").toInt().also { port ->
        eprintln("Mirai Selenium - HTTP Proxy Server running on $port")
        eprintln("Mirai Selenium - You can change the port in $file")
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
internal suspend fun executeSksks(url: String): String? {
    val d = CompletableDeferred<String?>()
    println("Mirai Selenium - HTTP 代理服务器运行于 [主机IP]:$sksksPort")
    println("Mirai Selenium - 请在浏览器打开以下链接")
    println("Mirai Selenium - 并修改代理服务器为 " + InetAddress.getLocalHost() + ":$sksksPort")
    println("Mirai Selenium - 并在浏览器中添加信任 CA 根证书: " + ProxyServer.dataStorage + "/littleproxy-mitm.pem")
    println(url)
    return SkSksDaemon(sksksPort, d, AtomicBoolean()).use {
        d.await().also { delay(10) }
    }
}

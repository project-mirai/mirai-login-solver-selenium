/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.selenium

import io.github.karlatemp.mxlib.selenium.MxSelenium
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SwingSolver
import org.openqa.selenium.Dimension
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.remote.AbstractDriverOptions
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.coroutines.suspendCoroutine

internal class SeleniumLoginSolverImpl : LoginSolver() {
    override val isSliderCaptchaSupported: Boolean get() = true

    init {
        setup?.let {
            throw RuntimeException("Exception in loading selenium", it)
        }
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return SwingSolver.onSolvePicCaptcha(bot, data)
    }


    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return SwingSolver.onSolveUnsafeDeviceLoginVerify(bot, url)
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? = suspendCoroutine<String?> { c ->
        thread {
            c.resumeWith(kotlin.runCatching {
                process(url)
            })
        }
    }
}

internal val klass = SeleniumLoginSolverImpl::class.java
internal val classLoader = SeleniumLoginSolverImpl::class.java.classLoader

internal val setup: Throwable? by lazy {
    runCatching {
        val thread = Thread.currentThread()
        val oc = thread.contextClassLoader
        try {
            thread.contextClassLoader = classLoader
            MxSelenium.initialize()
            if (!MxSelenium.isSupported()) {
                MxSelenium.newDriver()
            }
        } finally {
            thread.contextClassLoader = oc
        }
    }.exceptionOrNull()
}

internal val UserAgent =
    "Mozilla/5.0 (Linux; Android 7.1.1; MIUI ONEPLUS/A5000_23_17; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045426 Mobile Safari/537.36 V1_AND_SQ_8.3.9_0_TIM_D QQ/3.1.1.2900 NetType/WIFI WebP/0.3.0 Pixel/720 StatusBarHeight/36 SimpleUISwitch/0 QQTheme/1015712"

internal val script by lazy {
    klass.getResourceAsStream("/mirai-selenium/captcha.inject.js")!!.bufferedReader().use {
        it.readText()
    }
}

internal fun process(url: String): String? {
    System.err.println("[LoginSolverSelenium] Captcha URL: $url")
    Thread.currentThread().contextClassLoader = classLoader
    val provider = MxSelenium.newDriver(UserAgent) { options ->
        when (options) {
            is AbstractDriverOptions<*> -> {
                options.setPageLoadStrategy(PageLoadStrategy.EAGER)
            }
        }
    }
    try {
        provider.manage().window().size = Dimension(425, 900)
        provider.get(url)
        provider.executeScript(script)
        while (true) {
            sleep(1000)
            val handles = provider.windowHandles
            if (handles.isEmpty()) return null

            runCatching {
                val alert = provider.switchTo().alert()
                val title = alert.text
                if (title == "MiraiSelenium - ticket") {
                    alert.accept()
                }
            }
            val response = runCatching {
                provider.executeScript("return window.miraiSeleniumComplete")?.toString()
            }.getOrNull()
            if (response != null) {
                val respObj = Json.decodeFromString(JsonObject.serializer(), response)
                val ticket = respObj["ticket"] as? JsonPrimitive
                if (ticket != null) {
                    return ticket.content
                } else {
                    throw (WrongPasswordException(response))
                }
            }
        }
    } finally {
        provider.quit()
    }
}


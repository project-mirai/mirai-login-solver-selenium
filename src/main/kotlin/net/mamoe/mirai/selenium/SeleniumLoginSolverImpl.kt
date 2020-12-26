package net.mamoe.mirai.selenium

import io.github.karlatemp.mxlib.MxLib
import io.github.karlatemp.mxlib.selenium.MxSelenium
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.mamoe.mirai.Bot
import net.mamoe.mirai.network.WrongPasswordException
import net.mamoe.mirai.utils.LoginSolver
import net.mamoe.mirai.utils.SwingSolver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import java.io.File
import kotlin.concurrent.thread
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class SeleniumLoginSolverImpl : LoginSolver() {
    override val isSliderCaptchaSupported: Boolean get() = true

    init {
        setup
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return SwingSolver.onSolvePicCaptcha(bot, data)
    }


    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return SwingSolver.onSolveUnsafeDeviceLoginVerify(bot, url)
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? = suspendCoroutine<String> { c ->
        thread {
            process(bot, url, c)
        }
    }
}

internal lateinit var ext: File
internal val classLoader = SeleniumLoginSolverImpl::class.java.classLoader

internal val setup: Unit by lazy {
    ext = extractExt()
}

internal val UserAgent =
    "Mozilla/5.0 (Linux; Android 7.1.1; MIUI ONEPLUS/A5000_23_17; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/77.0.3865.120 MQQBrowser/6.2 TBS/045426 Mobile Safari/537.36 V1_AND_SQ_8.3.9_0_TIM_D QQ/3.1.1.2900 NetType/WIFI WebP/0.3.0 Pixel/720 StatusBarHeight/36 SimpleUISwitch/0 QQTheme/1015712"

internal fun process(bot: Bot?, url: String, c: Continuation<String>) {
    Thread.currentThread().contextClassLoader = classLoader
    val provider = MxSelenium.newDriver(UserAgent) { options ->
        when (options) {
            is ChromeOptions -> {
                if (ext.isDirectory) {
                    options.addArguments("--load-extension=$ext")
                } else {
                    options.addExtensions(ext)
                }
            }
            is FirefoxOptions -> {
                throw UnsupportedOperationException("Not supported firefox platform")
            }
            else -> throw UnsupportedOperationException("Not supported. options=$options")
        }
    }
    try {
        provider.get(url)
        while (true) {
            Thread.sleep(1000)
            try {
                val handles = provider.windowHandles
                if (handles.isEmpty()) break

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
                        c.resume(ticket.content)
                    } else {
                        c.resumeWithException(WrongPasswordException(response))
                    }
                    break
                }
            } catch (e: Throwable) {
                c.resumeWithException(e)
                break
            }
        }
    } finally {
        provider.quit()
    }
}

internal fun extractExt(): File {
    val file = File(MxLib.getDataStorage(), "mirai-selenium-ext.zip")
    val updatetime = File(MxLib.getDataStorage(), "mirai-selenium-ext.update-time")
    val uptime = SeleniumLoginSolverImpl::class.java.getResourceAsStream("/mirai-selenium-ext.update-time")
        .bufferedReader().use { it.readLine().trim() }
    val tm = if (updatetime.isFile) {
        updatetime.bufferedReader().use { it.readLine().trim() }
    } else ""
    if (tm != uptime || !file.isFile) {
        file.parentFile.mkdirs()
        SeleniumLoginSolverImpl::class.java.getResourceAsStream("/mirai-selenium-ext.zip").buffered().use { res ->
            file.outputStream().buffered().use { res.copyTo(it) }
        }
        updatetime.bufferedWriter().use { it.append(uptime) }
    }
    return file
}


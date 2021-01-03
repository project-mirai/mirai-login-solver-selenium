/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.selenium.test

import io.github.karlatemp.mxlib.common.utils.SimpleClassLocator
import io.github.karlatemp.mxlib.logger.AnsiMessageFactory
import io.github.karlatemp.mxlib.logger.AwesomeLogger
import io.github.karlatemp.mxlib.logger.MJdkLoggerHandler
import io.github.karlatemp.mxlib.logger.renders.PrefixedRender
import io.github.karlatemp.mxlib.logger.renders.PrefixedRender.PrefixSupplier
import io.github.karlatemp.mxlib.logger.renders.SimpleRender
import io.github.karlatemp.mxlib.utils.StringUtils.BkColors
import java.time.format.DateTimeFormatter
import java.util.logging.ConsoleHandler
import java.util.logging.Logger

fun setupLogger() {
    val mf = AnsiMessageFactory(SimpleClassLocator())
    val renderX = PrefixedRender(
        SimpleRender(mf),
        PrefixSupplier.constant(BkColors._B)
            .plus(PrefixSupplier.dated(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .plus(" " + BkColors._5)
            .plus(PrefixSupplier.dated(DateTimeFormatter.ofPattern("HH:mm:ss")))
            .plus(BkColors.RESET + " [" + BkColors._6)
            .plus(PrefixSupplier.loggerAndRecordName().aligned(PrefixedRender.AlignedSupplier.AlignType.LEFT))
            .plus(BkColors.RESET + "] [" + BkColors._B)
            .plus(PrefixSupplier.loggerLevel().aligned(PrefixedRender.AlignedSupplier.AlignType.CENTER))
            .plus(BkColors.RESET + "] ")
    )
    val root = generateSequence(Logger.getGlobal()) { it.parent }.last()
    root.removeHandler(root.handlers.first { it is ConsoleHandler })
    root.addHandler(MJdkLoggerHandler(AwesomeLogger.Awesome("ROOT", { println(it) }, renderX)))
}

fun main() {
    setupLogger()
    Logger.getLogger("SetupLogger").info("OK")
}

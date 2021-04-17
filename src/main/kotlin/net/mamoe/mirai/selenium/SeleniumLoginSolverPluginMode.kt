/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.selenium

import net.mamoe.mirai.console.extension.PluginComponentStorage
import net.mamoe.mirai.console.extensions.BotConfigurationAlterer
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.version
import net.mamoe.mirai.utils.BotConfiguration
import java.util.*

internal class SeleniumLoginSolverPluginMode : KotlinPlugin(
    JvmPluginDescriptionBuilder(
        id = "net.mamoe.mirai-login-solver-selenium",
        version = "1.0-dev-12"
    ).build()
), BotConfigurationAlterer {
    override fun PluginComponentStorage.onLoad() {
        logger.info("Loading mirai-login-solver-selenium v$version")
        try {
            SeleniumLoginSolver
        } catch (err: Throwable) {
            logger.warning("mirai-login-solver-selenium is not supported.", err)
            logger.warning("Visit https://github.com/project-mirai/mirai-login-solver-selenium for more information", err)
            return
        }
        contributeBotConfigurationAlterer(this@SeleniumLoginSolverPluginMode)
    }

    override fun alterConfiguration(botId: Long, configuration: BotConfiguration): BotConfiguration {
        configuration.loginSolver = SeleniumLoginSolver
        return configuration
    }
}

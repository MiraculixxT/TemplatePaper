package de.miraculixx.template

import de.miraculixx.kpaper.main.KPaper
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig

class Template: KPaper() {

    override fun load() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).shouldHookPaperReload(true).silentLogs(true))

        println("Loaded!")
    }

    override fun startup() {
        CommandAPI.onEnable()

        println("Hello World!")
    }

    override fun shutdown() {
        CommandAPI.onDisable()

        println("Good bye!")
    }
}
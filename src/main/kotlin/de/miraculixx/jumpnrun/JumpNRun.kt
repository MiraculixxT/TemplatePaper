package de.miraculixx.jumpnrun

import de.miraculixx.kpaper.main.KPaper
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig

class JumpNRun: KPaper() {
    companion object {
        lateinit var INSTANCE: KPaper
    }

    override fun load() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).silentLogs(true))
        INSTANCE = this
    }

    override fun startup() {
        CommandAPI.onEnable()
        Manager
        MainCommand
        JumpChecker
    }

    override fun shutdown() {
        CommandAPI.onDisable()
        Manager.save()
    }
}

val INSTANCE by lazy { JumpNRun.INSTANCE }
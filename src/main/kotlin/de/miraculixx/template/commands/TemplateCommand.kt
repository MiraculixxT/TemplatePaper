package de.miraculixx.template.commands

import de.miraculixx.kpaper.extensions.bukkit.cmp
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import net.kyori.adventure.text.format.NamedTextColor

object TemplateCommand {
    private val command = commandTree("template") {
        playerExecutor { player, _ ->
            player.sendMessage(cmp("Hello World", NamedTextColor.BLUE))
        }
    }
}
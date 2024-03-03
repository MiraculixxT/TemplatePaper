package de.miraculixx.jumpnrun

import de.miraculixx.jumpnrun.Manager.toLite
import de.miraculixx.kpaper.event.listen
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import org.bukkit.Material
import org.bukkit.event.player.PlayerSwapHandItemsEvent

object MainCommand {
    private var creator: Manager.JumpAndRun? = null
    private var runName: String? = null

    private val command = commandTree("jumpnrun") {
        literalArgument("new") {
            stringArgument("name") {
                replaceSuggestions(ArgumentSuggestions.stringCollection { Manager.jumps.keys })
                playerExecutor { player, args ->
                    runName = args[0] as String
                    creator = Manager.jumps.getOrDefault(runName, Manager.JumpAndRun(player.location.toLite(), player.location.toLite(), mutableListOf()))
                    player.sendMessage("Jump and Run creator started")
                }
            }
        }

        literalArgument("remove-last") {
            playerExecutor { player, _ ->
                creator?.jumps?.removeLast() ?: return@playerExecutor
                player.sendMessage("Last jump removed")
            }
        }

        literalArgument("delete") {
            stringArgument("name") {
                replaceSuggestions(ArgumentSuggestions.stringCollection { Manager.jumps.keys })
                playerExecutor { player, args ->
                    Manager.jumps.remove(args[0] as String)
                    player.sendMessage("Jump and Run deleted")
                }
            }
        }
    }

    private val onF = listen<PlayerSwapHandItemsEvent> {
        if (creator == null) return@listen
        val loc = it.player.location.toLite()
        val type = when (it.player.location.block.type) {
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE -> Manager.JumpType.CHECKPOINT
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE -> Manager.JumpType.END
            else -> Manager.JumpType.NORMAL
        }
        creator!!.jumps.add(Manager.Jump(loc, type))
        it.player.sendMessage("Jump added $type")

        if (type == Manager.JumpType.END) {
            Manager.jumps[runName!!] = creator!!
            creator = null
            Manager.save()
            it.player.sendMessage("Jump and Run saved")
        }
    }
}
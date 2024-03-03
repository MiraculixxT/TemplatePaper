package de.miraculixx.jumpnrun

import com.destroystokyo.paper.ParticleBuilder
import de.miraculixx.kpaper.event.listen
import de.miraculixx.kpaper.extensions.bukkit.cmp
import de.miraculixx.kpaper.items.customModel
import de.miraculixx.kpaper.items.itemStack
import de.miraculixx.kpaper.items.meta
import de.miraculixx.kpaper.items.name
import de.miraculixx.kpaper.runnables.task
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import java.util.*

object JumpChecker {
    private val isActive = mutableMapOf<UUID, Manager.JumpAndRun>()
    private val nextJump = mutableMapOf<UUID, Location>()
    private val lastCheckpoint = mutableMapOf<UUID, Manager.LiteLocation>()
    private val killItem = itemStack(Material.RED_DYE) {
        meta {
            name = cmp("Zurück zum Checkpoint", NamedTextColor.RED)
            customModel = 1001
        }
    }

    private val onMove = listen<PlayerMoveEvent> {
        if (!it.hasChangedBlock()) return@listen
        val block = it.to.block.location
        val player = it.player.uniqueId
        val jumpnrun = isActive[player]

        if (jumpnrun == null) {
            val jump = Manager.jumps.values.firstOrNull { it.start.checkLocation(block) } ?: return@listen

            isActive[player] = jump
            lastCheckpoint[player] = jump.start
            it.player.inventory.remove(Material.RED_DYE)
            it.player.inventory.addItem(killItem)
            it.player.playSound(it.player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
        } else {

            val jump = jumpnrun.jumps.firstOrNull { it.position.checkLocation(block) } ?: return@listen

            if (jump.type == Manager.JumpType.CHECKPOINT) {
                if (jump.position != lastCheckpoint[player]) {
                    lastCheckpoint[player] = jump.position
                    it.player.playSound(it.player, Sound.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, 1f, 1f)
                }
            } else if (jump.type == Manager.JumpType.END) {
                isActive.remove(player)
                nextJump.remove(player)
                lastCheckpoint.remove(player)
                it.player.inventory.remove(Material.RED_DYE)
                it.player.playSound(it.player, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1f)
            }

            jumpnrun.jumps.lastIndexOf(jump).let { index ->
                if (index + 1 < jumpnrun.jumps.size) {
                    nextJump[player] = jumpnrun.jumps[index + 1].position.toLocation(it.player.world)
                }
            }
        }
    }

    private fun warpBack(player: Player) {
        val checkpoint = lastCheckpoint[player.uniqueId] ?: return
        player.teleport(checkpoint.toLocation(player.world).apply {
            yaw = player.location.yaw
            pitch = player.location.pitch
        })
        player.playSound(player.location, Sound.ENTITY_PLAYER_TELEPORT, 1f, 1.2f)
    }

    private val onDamage = listen<EntityDamageEvent> {
        val player = it.entity
        if (player !is Player) return@listen
        it.isCancelled = true
        if (it.cause == EntityDamageEvent.DamageCause.FALL) {
            warpBack(player)
        }
    }

    private val onDrop = listen<PlayerDropItemEvent> {
        if (isActive[it.player.uniqueId] != null) {
            it.isCancelled = true
        }
    }

    private val onInteract = listen<PlayerInteractEvent> {
        if (isActive[it.player.uniqueId] == null) return@listen
        it.isCancelled = true
        if (it.item?.itemMeta?.customModel == 1001) {
            it.player.fallDistance = 0f
            warpBack(it.player)
        }
    }

    private val task = task(false, 1, 5) {
        nextJump.forEach { (player, loc) ->
            val op = Bukkit.getPlayer(player) ?: return@forEach
            ParticleBuilder(Particle.CRIT)
                .location(loc.clone().add(0.5, 0.3, 0.5))
                .count(2)
                .offset(0.1, 0.3, 0.1)
                .extra(0.0)
                .receivers(op)
                .spawn()
        }
    }
}
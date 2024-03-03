package de.miraculixx.jumpnrun

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Location
import org.bukkit.World
import java.io.File

object Manager {
    private val file = File(INSTANCE.dataFolder, "jumps.json")
    val jumps = mutableMapOf<String, JumpAndRun>()

    fun save() {
        if (!file.exists()) file.parentFile.mkdirs()
        file.writeText(Json.encodeToString(jumps))
    }

    @Serializable
    data class JumpAndRun(
        val start: LiteLocation,
        val end: LiteLocation,
        val jumps: MutableList<Jump>
    )

    @Serializable
    data class Jump(
        val position: LiteLocation,
        val type: JumpType
    )

    @Serializable
    data class LiteLocation(
        val x: Int,
        val y: Int,
        val z: Int
    ) {
        fun checkLocation(location: Location) = location.blockX == x && location.blockY == y && location.blockZ == z
        fun toLocation(world: World) = Location(world, x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5)
    }

    fun Location.toLite() = LiteLocation(blockX, blockY, blockZ)


    enum class JumpType {
        NORMAL,
        CHECKPOINT,
        END
    }

    init {
        if (file.exists()) {
            jumps.putAll(Json.decodeFromString(file.readText()))
        }
    }
}
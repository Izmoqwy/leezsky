package lz.izmoqwy.boxes.obj

import org.bukkit.inventory.ItemStack
import java.util.*

val random = Random()

class Box(val name: String,
          val broadcast: Boolean,
          private val outcomes: Array<BoxOutcome>) {

    fun pickOutcome(): ItemStack? {
        if (outcomes.isEmpty())
            return null

        val picked: Double = random.nextDouble()

        var current = 0.0
        for (outcome in outcomes) {
            current += outcome.chance / 100f
            if (picked <= current)
                return outcome.reward
        }
        return outcomes[0].reward
    }

}

class BoxOutcome(val name: String,
                 val description: String,
                 val chance: Float,
                 val reward: ItemStack)
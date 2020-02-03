package lz.izmoqwy.boxes

import com.google.common.collect.Maps
import kt.leezsky.core.api.ConfigLoader
import kt.leezsky.core.api.SignInput
import kt.leezsky.core.defaultNotNull
import kt.leezsky.core.isIn
import kt.leezsky.core.send
import lz.izmoqwy.boxes.DigitalBoxes.Companion.PREFIX
import lz.izmoqwy.boxes.gui.BoxOpeningGUI
import lz.izmoqwy.boxes.obj.Box
import lz.izmoqwy.boxes.obj.BoxOutcome
import org.apache.commons.lang3.RandomStringUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

object BoxManager : ConfigLoader(DigitalBoxes.instance!!.dataFolder, "boxes.yml") {

    private val boxes = ArrayList<Box>()
    private val codes = Maps.newHashMap<String, Box>()

    override fun load() {
        boxes.clear()
        for (box in sections(null).orEmpty()) {
            val outcomes = ArrayList<BoxOutcome>()
            for (outcome in sections("outcomes", box).orEmpty()) {
                outcomes.add(BoxOutcome(name = outcome.getString("name", "Sans nom"),
                        description = outcome.getString("description", "Aucune description"),
                        chance = defaultNotNull(outcome.getInt("chance").toFloat(), 1f) {
                            DigitalBoxes.instance?.logger?.warning("La chance de tirage du coffre '${outcome.name}' n'est pas indiquée !")
                        },
                        reward = defaultNotNull(item("reward", outcome), ItemStack(Material.STONE)) {
                            DigitalBoxes.instance?.logger?.warning("La récompense du coffre '${outcome.name}' n'est pas indiquée !")
                        }))
            }

            boxes.add(Box(name = box.getString("name", "Sans nom"),
                    broadcast = box.getBoolean("broadcast", false),
                    outcomes = outcomes.toTypedArray()))
        }
    }

    fun askClaim(player: Player) {
        SignInput.openInput(player, arrayOf("", "^^^^^^^^^^^^", "Code d'accès", "au coffre-fort")) { lines ->
            claim(player, lines[0])
            true
        }
    }

    fun claim(player: Player, code: String) {
        if (code isIn codes) {
            BoxOpeningGUI(player).open()
        }
        else
            player.send("$PREFIX &cAucun coffre-fort trouvé avec le code d'accès '$code' !")
    }

    fun createCode(box: Box): String {
        var code: String
        do
            code = RandomStringUtils.randomAlphanumeric(12)
        while (code isIn codes)

        codes[code] = box
        return code
    }

}

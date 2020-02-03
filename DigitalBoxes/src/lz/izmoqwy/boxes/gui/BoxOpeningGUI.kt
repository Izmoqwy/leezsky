package lz.izmoqwy.boxes.gui

import kt.leezsky.core.color
import lz.izmoqwy.boxes.DigitalBoxes
import lz.izmoqwy.core.gui.MinecraftGUIListener
import lz.izmoqwy.core.gui.UniqueMinecraftGUI
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class BoxOpeningGUI(player: Player)
    : UniqueMinecraftGUI(null, '&' color "&b~ &9Box Opening &b~", player), MinecraftGUIListener, Runnable {

    init {
        addListener(this)
    }

    private var taskId: Int? = null

    override fun onOpen(player: Player) {
        Bukkit.getScheduler().runTaskTimer(DigitalBoxes.instance, this, 0, 20)
    }

    override fun onClose(player: Player?) {
        Bukkit.getScheduler().cancelTask(taskId!!)
    }

    override fun run() {

    }
}
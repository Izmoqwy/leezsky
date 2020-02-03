package lz.izmoqwy.boxes

import kt.leezsky.core.KotlinPlugin
import kt.leezsky.core.register
import lz.izmoqwy.boxes.commands.BoxCommand

class DigitalBoxes : KotlinPlugin() {

    companion object {
        const val PREFIX = "&3Digital Safe Inc. &8»"

        var instance: DigitalBoxes? = null
    }

    override fun onEnable() {
        instance = this

        server.register(BoxCommand())
        BoxManager.load()
    }

}
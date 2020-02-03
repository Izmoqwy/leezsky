package lz.izmoqwy.boxes.commands

import kt.leezsky.core.sub
import lz.izmoqwy.boxes.BoxManager
import lz.izmoqwy.core.command.CommandOptions
import lz.izmoqwy.core.command.CoreCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BoxCommand : CoreCommand("boxes", CommandOptions.builder()
        .playerOnly(true)
        .build()) {

    override fun execute(commandSender: CommandSender, usedCommand: String, args: Array<out String>) {
        val player = commandSender as Player

        if (match(args, 0, "open")) {
            if (args.size >= 2)
                BoxManager.claim(player, args.sub(1))
            else
                BoxManager.askClaim(player)
        }
    }

}
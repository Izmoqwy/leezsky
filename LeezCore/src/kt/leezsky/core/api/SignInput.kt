package kt.leezsky.core.api

import com.comphenix.tinyprotocol.TinyProtocol
import io.netty.channel.Channel
import kt.leezsky.core.isIn
import lz.izmoqwy.core.self.LeezCore
import net.minecraft.server.v1_12_R1.*
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage
import org.bukkit.entity.Player
import org.bukkit.util.Vector

object SignInput {

    private val signs = HashMap<Pair<Player, Vector>, SignInputObject>()

    private val protocol = object : TinyProtocol(LeezCore.instance) {
        override fun onPacketInAsync(sender: Player?, channel: Channel?, packet: Any?): Any {
            if (packet is PacketPlayInUpdateSign) {
                val position = packet.a()
                sendPacket(sender, PacketPlayOutBlockChange((sender as CraftPlayer).handle.world, position))
                val vector = Vector(position.x, position.y, position.z)

                val signKey = sender to vector
                if (signKey isIn signs) {
                    val signObj = signs[signKey]
                    if (signObj!!.onEdit.invoke(packet.b())) {
                        signs.remove(signKey)
                        uninjectPlayer(sender)
                    }
                    else {
                        sendPacket(sender, updateSign(vector, signObj.lines))
                        sendPacket(sender, PacketPlayOutOpenSignEditor(position))
                    }
                }
            }

            return super.onPacketInAsync(sender, channel, packet)
        }
    }

    fun openInput(player: Player, lines: Array<String?>, onEdit: (Array<String>) -> Boolean) {
        if (!protocol.hasInjected(player))
            protocol.injectPlayer(player)

        val craftPlayer = player as CraftPlayer

        val signVector = Vector(player.location.blockX, if (player.location.blockY > 100) 1 else 255, player.location.blockZ)
        val position = BlockPosition(signVector.blockX, signVector.blockY, signVector.blockZ)
        val blockChangePacket = PacketPlayOutBlockChange(craftPlayer.handle.world, position)
        blockChangePacket.block = Blocks.STANDING_SIGN.blockData
        protocol.sendPacket(player, blockChangePacket)
        protocol.sendPacket(player, updateSign(signVector, lines))

        val signPacket = PacketPlayOutOpenSignEditor(position)
        protocol.sendPacket(player, signPacket)
        signs[player to signVector] = SignInputObject(lines, onEdit)
    }

    private fun updateSign(signVector: Vector, lines: Array<String?>): PacketPlayOutTileEntityData? {
        val sign = TileEntitySign()
        sign.position = BlockPosition(signVector.blockX, signVector.blockY, signVector.blockZ)
        System.arraycopy(sanitizeLines(lines), 0, sign.lines, 0, 4)

        return sign.updatePacket
    }

    private fun sanitizeLines(lines: Array<String?>): Array<IChatBaseComponent?> {
        val components = arrayOfNulls<IChatBaseComponent>(4)
        for (i in 0..3) {
            if (i < lines.size && lines[i] != null) {
                components[i] = CraftChatMessage.fromString(lines[i])[0]
            }
            else {
                components[i] = ChatComponentText("")
            }
        }
        return components
    }

}

class SignInputObject(val lines: Array<String?>,
                      val onEdit: (Array<String>) -> Boolean)
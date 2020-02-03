package kt.leezsky.core

import lz.izmoqwy.core.FireAction
import lz.izmoqwy.core.command.CoreCommand
import lz.izmoqwy.core.utils.ItemUtil
import lz.izmoqwy.core.utils.ServerUtil
import lz.izmoqwy.core.utils.TextUtil
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Server
import org.bukkit.command.CommandExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

typealias KotlinPlugin = org.bukkit.plugin.java.JavaPlugin

/*
Server
 */
fun Server.register(instance: KotlinPlugin, vararg listeners: Listener) = ServerUtil.registerListeners(instance, *listeners)
fun Server.register(name: String, command: CommandExecutor) = ServerUtil.registerCommand(name, command)
fun Server.register(command: CoreCommand) = ServerUtil.registerCommand(command.name, command)

/*
Player
 */
fun Player.send(message: String, colored: Boolean = true) = sendMessage(if (colored) ChatColor.translateAlternateColorCodes('&', message) else message)
fun Player.give(vararg items: ItemStack) = ItemUtil.give(this, *items)
fun Player.take(vararg items: ItemStack) = ItemUtil.take(this, *items)

fun OfflinePlayer.hasAlreadyPlayed() = this.hasPlayedBefore() || this.isOnline

/*
Utils
 */
fun Array<out String>.sub(start: Int) = TextUtil.getFinalArg(this, start)!!

infix fun <T, V> T.isIn(map: Map<T, V>) = map.containsKey(this)
infix fun <T> T.isIn(arr: Array<T>) = arr.contains(this)

fun <T> default(value: T?, def: T? = null, onNull: () -> Unit): T? {
    if (value == null) {
        onNull()
        return def
    }
    return value
}

fun <T> defaultNotNull(value: T?, def: T, onNull: () -> Unit): T {
    return default(value, def, onNull)!!
}
package kt.leezsky.core

import net.md_5.bungee.api.ChatColor

infix fun Char.color(s: String): String = ChatColor.translateAlternateColorCodes(this, s)
package kt.leezsky.core.database

import kt.leezsky.core.KotlinPlugin

class RemoteDatabase(plugin: KotlinPlugin, name: String,
                     private val host: String,
                     private val dbname: String,
                     private val user: String,
                     private val password: String)
    : AbstractDatabase(plugin, name) {

    override fun connect() {
        super.connect()
        connectDriver("com.mysql.jdbc.Driver", "jdbc:mysql://$host/$dbname?autoReconnect=true&useUnicode=yes", user, password)
    }

    override fun sqlQuery(sql: String) = sql

}
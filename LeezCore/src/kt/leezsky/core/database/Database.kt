package kt.leezsky.core.database

import kt.leezsky.core.KotlinPlugin
import java.sql.PreparedStatement
import java.sql.ResultSet

interface Database {

    val plugin: KotlinPlugin
    val name: String

    fun connect()
    fun disconnect()
    fun isConnected() = false

    fun prepare(sql: String): PreparedStatement
    fun execute(sql: String)
    fun query(sql: String, consumer: (resultSet: ResultSet) -> Unit)

    fun prepareTable(name: String, vararg fields: DatabaseField<*>): DatabaseTable
    fun getTable(name: String): DatabaseTable?

}
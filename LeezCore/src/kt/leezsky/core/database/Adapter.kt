package kt.leezsky.core.database

interface Adapter {

    fun sqlQuery(sql: String): String

}
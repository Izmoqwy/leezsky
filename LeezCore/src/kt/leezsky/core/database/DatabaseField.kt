package kt.leezsky.core.database

import java.sql.PreparedStatement

open class DatabaseField <T> (val name: String,
                         val type: DataType<T>,
                         private val limit: Int? = null,
                         private val secondary_limit: Int? = null,
                         private val unique: Boolean = false) {

    fun prepare(): String {
        val limitText = limit?.let { "($limit${secondary_limit?.let {", $secondary_limit"} ?: ""})" }
        return "`$name` ${type.sqlType()}$limitText${if (unique) " UNIQUE" else ""}"
    }

    // brute-force method
    fun setArgument(statement: PreparedStatement, index: Int, value: Any?) {
        @Suppress("UNCHECKED_CAST")
        type.setArgument(statement, index, value as T?)
    }

}
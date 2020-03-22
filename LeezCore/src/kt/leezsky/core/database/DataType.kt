package kt.leezsky.core.database

import java.sql.PreparedStatement
import java.sql.ResultSet

interface DataType <T> {

    fun isValid(value: Any?): Boolean

    fun sqlType(): String

    fun setArgument(statement: PreparedStatement, index: Int, value: T?)
    fun getArgument(resultSet: ResultSet, index: Int? = null, name: String? = null): T?

}
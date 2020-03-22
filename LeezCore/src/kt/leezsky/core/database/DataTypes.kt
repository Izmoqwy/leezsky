package kt.leezsky.core.database

import java.sql.PreparedStatement
import java.sql.ResultSet

object DataTypes {

    val Varchar = object : DataType<String> {
        override fun sqlType() = "VARCHAR"

        override fun setArgument(statement: PreparedStatement, index: Int, value: String?) {
            statement.setString(index, value)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): String? {
            index?.let { return resultSet.getString(index) }
            name?.let { return resultSet.getString(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is String?
        }
    }

    val Text = object : DataType<String> {
        override fun sqlType() = "TEXT"

        override fun setArgument(statement: PreparedStatement, index: Int, value: String?) {
            statement.setString(index, value)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): String? {
            index?.let { return resultSet.getString(index) }
            name?.let { return resultSet.getString(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is String?
        }
    }

    val Boolean = object : DataType<Boolean> {
        override fun sqlType() = "TINYINT"

        override fun setArgument(statement: PreparedStatement, index: Int, value: Boolean?) {
            statement.setBoolean(index, value ?: false)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): Boolean? {
            index?.let { return resultSet.getBoolean(index) }
            name?.let { return resultSet.getBoolean(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is Boolean?
        }
    }

    val Integer = object : DataType<Int> {
        override fun sqlType() = "INTEGER"

        override fun setArgument(statement: PreparedStatement, index: Int, value: Int?) {
            statement.setInt(index, value ?: 0)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): Int? {
            index?.let { return resultSet.getInt(index) }
            name?.let { return resultSet.getInt(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is Int?
        }
    }

    val Long = object : DataType<Long> {
        override fun sqlType() = "BIGINT"

        override fun setArgument(statement: PreparedStatement, index: Int, value: Long?) {
            statement.setLong(index, value ?: 0)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): Long? {
            index?.let { return resultSet.getLong(index) }
            name?.let { return resultSet.getLong(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is Long?
        }
    }

    val Double = object : DataType<Double> {
        override fun sqlType() = "DOUBLE"

        override fun setArgument(statement: PreparedStatement, index: Int, value: Double?) {
            statement.setDouble(index, value ?: .0)
        }

        override fun getArgument(resultSet: ResultSet, index: Int?, name: String?): Double? {
            index?.let { return resultSet.getDouble(index) }
            name?.let { return resultSet.getDouble(name) }
            return null
        }

        override fun isValid(value: Any?): Boolean {
            return value is Double?
        }
    }

}
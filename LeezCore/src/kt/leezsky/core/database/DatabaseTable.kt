package kt.leezsky.core.database

import lz.izmoqwy.core.self.LeezCore
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.text.MessageFormat
import kotlin.math.abs

typealias Field = Pair<String, Any?>

class DatabaseTable(val parent: Database,
                    val name: String,
                    private val fields: Map<String, DatabaseField<*>>) {


    fun insert(vararg values: Field) {
        val statementFields = getStatementFields("insert", *values) ?: return

        val fieldsNames = statementFields.joinToString(", ") { pair -> pair.first.name }
        val statement = parent.prepare("INSERT INTO $name ($fieldsNames) VALUES (${statementFields.joinToString(", ") { "?" }})")
        setStatementValues(statement, statementFields)
        statement.execute()
        statement.close()
    }

    fun delete(vararg where: Field) {
        val whereFields = getStatementFields("delete", *where) ?: return

        val statement = parent.prepare("DELETE FROM $name WHERE ${getPairedEqual(whereFields)}")
        setStatementValues(statement, whereFields)
        statement.execute()
        statement.close()
    }

    fun update(vararg values: Field, where: Field) = update(values=*values, where=arrayOf(where))

    fun update(vararg values: Field, where: Array<Field>) {
        val valuesFields = getStatementFields("update", *values) ?: return
        val whereFields = getStatementFields("update", *where) ?: return

        val statement = parent.prepare("UPDATE $name SET ${getPairedEqual(valuesFields)} WHERE ${getPairedEqual(whereFields)}")
        setStatementValues(statement, valuesFields, whereFields)
        statement.executeUpdate()
        statement.close()
    }

    fun select(vararg fields: String, where: Field, consumer: (resultSet: ResultSet) -> Unit) = select(fields=*fields, where=arrayOf(where), consumer=consumer)

    fun select(vararg fields: String, where: Array<Field>, consumer: (resultSet: ResultSet) -> Unit) {
        val whereFields = getStatementFields("select", *where) ?: return

        val statement = parent.prepare("SELECT ${if (fields.isEmpty()) "*" else "`${fields.joinToString("`, `")}`"} FROM $name WHERE ${getPairedEqual(whereFields)}")
        setStatementValues(statement, whereFields)
        consumer.invoke(statement.executeQuery())
        statement.close()
    }

    fun hasResult(field: String, where: Field) = hasResult(field, arrayOf(where))

    fun hasResult(field: String, where: Array<Field>): Boolean {
        var hasResult = false
        select(field, where=where) { resultSet ->
            hasResult = resultSet.next()
        }
        return hasResult
    }

    fun increase(field: String, by: Int = 1, where: Field) = increase(field, by, arrayOf(where))

    fun increase(field: String, by: Int = 1, where: Array<Field>) {
        val whereFields = getStatementFields("increase", *where) ?: return

        val statement = parent.prepare("UPDATE $name SET $field = $field + ? WHERE ${getPairedEqual(whereFields)}")
        statement.setInt(1, by)
        statement.executeUpdate()
        statement.close()
    }

    fun decrease(field: String, by: Int = 1, where: Field) = decrease(field, by, arrayOf(where))

    fun decrease(field: String, by: Int = 1, where: Array<Field>) = increase(field, -abs(by), where)

    fun <T> get(fieldName: String, where: Field): T? = get(fieldName, arrayOf(where))

    @Suppress("UNCHECKED_CAST")
    fun <T> get(fieldName: String, where: Array<Field>): T? {
        val field = fields[fieldName] ?: return null

        var get: Any? = null
        select(field.name, where=where) { resultSet ->
            get = field.type.getArgument(resultSet, name=field.name)
        }
        if (field.type.isValid(get))
            return get as T?
        return null
    }

    private fun getPairedEqual(fieldsPairs: List<Pair<DatabaseField<*>, Any?>>): String {
        return fieldsPairs.joinToString(", ") { pair -> "${pair.first.name} = ?" }
    }

    private fun setStatementValues(statement: PreparedStatement, vararg valuesPair: List<Pair<DatabaseField<*>, Any?>>) {
        var nextIndex = 1
        for (group in valuesPair) {
            for (i in group.indices) {
                val pair = group[i]
                pair.first.setArgument(statement, nextIndex + i, pair.second)
            }
            nextIndex += group.size
        }
    }

    private fun getStatementFields(action: String, vararg values: Field): List<Pair<DatabaseField<*>, Any?>>? {
        val statementFields = ArrayList<Pair<DatabaseField<*>, Any?>>()
        for (value in values) {
            val field = checkField(action, value.first, value.second) ?: return null
            statementFields.add(field to value.second)
        }
        return statementFields
    }

    private fun checkField(action: String, fieldName: String, value: Any?): DatabaseField<*>? {
        if (!fields.containsKey(fieldName.toLowerCase())) {
            error(action, "Unknown field \"{0}\"", fieldName)
            return null
        }

        val field = fields.getValue(fieldName.toLowerCase())
        if (!field.type.isValid(value)) {
            error(action, "\"{0}\" ({1}) is not a valid value for field \"{2}\"", value, value?.javaClass?.name, field.name)
            return null
        }
        return field
    }

    private fun error(action: String, message: String, vararg objects: Any?) {
        LeezCore.instance.logger.severe(arrayOf(
                "!!! Database error report !!!",
                "> Error while performing ${action.toUpperCase()} action in table \"$name\":",
                "  ${MessageFormat.format(message, *objects)}",
                "!!! Fix this error as quickly as possible !!!"
        ).joinToString("\n" ))
    }

}
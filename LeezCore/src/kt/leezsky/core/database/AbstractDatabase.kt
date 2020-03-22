package kt.leezsky.core.database

import kt.leezsky.core.KotlinPlugin
import lz.izmoqwy.core.self.CorePrinter
import java.sql.*
import java.text.MessageFormat

abstract class AbstractDatabase(override val plugin: KotlinPlugin,
                                override val name: String)
    : Database, Adapter {

    companion object {
        val REGISTRY: List<Database> = ArrayList()
    }

    private var connection: Connection? = null
    private val tables: Map<String, DatabaseTable> = HashMap()

    override fun connect() {
        if (!REGISTRY.contains(this)) {
            (REGISTRY as ArrayList).add(this)
        }
    }

    protected fun connectDriver(classPath: String, connectionUrl: String, user: String? = null, password: String? = null) {
        try {
            Class.forName(classPath)
            connection = DriverManager.getConnection(connectionUrl, user, password)
            connection!!.autoCommit = true
            CorePrinter.print("Connection to database \"{0}\" established successfully.", name)
        }
        catch (ex: SQLException) {
            CorePrinter.err("!!! Database error report !!!")
            ex.printStackTrace()
            CorePrinter.err("!!! This is a database-init error, server should be stopped right now !!!")
        }
        catch (ignored: ClassNotFoundException) {
            error("Driver not found, cannot instantiate connection")
        }
    }

    override fun disconnect() {
        if (isConnected()) {
            try {
                connection!!.close()
                connection = null
                CorePrinter.print("Connection to database \"{0}\" closed.", name)
            }
            catch (ex: SQLException) {
                ex.printStackTrace()
            }
        }
    }

    protected fun error(message: String, vararg objects: Any?) {
        CorePrinter.err(arrayOf(
                "!!! Database error report !!!",
                "> ${MessageFormat.format(message, *objects)}",
                "!!! This is a database-init error, server should be stopped right now !!!"
        ).joinToString("\n"))
    }

    override fun prepare(sql: String): PreparedStatement {
        return connection!!.prepareStatement(sql)
    }

    override fun execute(sql: String) {
        val statement = prepare(sql)
        statement.execute()
        statement.close()
    }

    override fun query(sql: String, consumer: (resultSet: ResultSet) -> Unit) {
        val statement = prepare(sql)
        consumer.invoke(statement.executeQuery())
        statement.close()
    }

    override fun prepareTable(name: String, vararg fields: DatabaseField<*>): DatabaseTable {
        execute("CREATE TABLE IF NOT EXISTS `$name` (${fields.joinToString(", ") { field -> field.prepare() }})")

        val mappedFields = HashMap<String, DatabaseField<*>>()
        for (field in fields) {
            mappedFields[field.name.toLowerCase()] = field
        }

        val table = DatabaseTable(this, name, mappedFields)
        (tables as HashMap)[name] = table
        return table
    }

    override fun getTable(name: String): DatabaseTable? {
        return tables[name]
    }

}
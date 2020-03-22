package kt.leezsky.core.database

import kt.leezsky.core.KotlinPlugin
import java.io.File

class LocalDatabase(plugin: KotlinPlugin, name: String, val file: File) : AbstractDatabase(plugin, name) {

    init {
        if (!file.isDirectory) {
            if (!file.exists()) {
                if (!file.parentFile.exists())
                    file.parentFile.mkdirs()
                file.createNewFile()
            }
        }
        else {
            error("SQLite requires a file, not a dir ({0}).", file.absolutePath)
        }
    }

    override fun connect() {
        super.connect()
        connectDriver("org.sqlite.JDBC", "jdbc:sqlite:${file.absolutePath}")
    }

    override fun sqlQuery(sql: String): String {
        return "$sql COLLATE NOCASE"
    }

}
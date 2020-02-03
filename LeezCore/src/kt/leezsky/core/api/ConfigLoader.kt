package kt.leezsky.core.api

import kt.leezsky.core.defaultNotNull
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import java.io.File

abstract class ConfigLoader(directory: File,
                            fileName: String) {

    private val file: File = File(directory, fileName)
    private var yaml: YamlConfiguration? = null

    init {
        if (directory.exists() && !directory.isDirectory)
            throw Error("Config's parent cannot be a file in class '${javaClass.name}'! Expected type: directory")

        if (directory.exists() || directory.mkdirs()) {
            if (!file.exists() && !file.createNewFile())
                throw Error("Config's file cannot be created for file '$fileName' in class '${javaClass.name}'")

            yaml = YamlConfiguration.loadConfiguration(file)
        }
    }

    abstract fun load()

    private fun section(path: String?, parent: ConfigurationSection?): ConfigurationSection? {
        return yaml?.getConfigurationSection((parent?.currentPath ?: "") + path)
    }

    protected fun sections(path: String? = null, parent: ConfigurationSection? = null): Array<ConfigurationSection>? {
        val section = section(path, parent) ?: return null

        val sections = ArrayList<ConfigurationSection>()
        for (key in section.getKeys(false)) {
            sections.add(section.getConfigurationSection(key))
        }

        return sections.toTypedArray()
    }

    protected fun item(path: String? = null, parent: ConfigurationSection? = null): ItemStack? {
        val section = section(path, parent) ?: return null
        return ItemStack(defaultNotNull(Material.getMaterial(section.getString("type")?.toUpperCase()), Material.STONE) {
            Bukkit.getLogger().warning("Unknown material '${section.getString("type")}' for section '${section.parent?.name}' (file: ${file.name}; class: ${javaClass.name})")
        }, section.getInt("amount", 1), section.getInt("damage", section.getInt("data", 0)).toShort())
    }

    fun saveYaml() {
        yaml?.save(file)
    }

}
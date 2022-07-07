package xyz.oniverse.music.entities

import java.io.File
import java.io.FileReader
import java.util.*

/** Literally just stole the code from Config.kt.
 * Might change it later to a more efficient one tbh **/
class Reply(filePath: String)
{
    private val conf = FileReader(filePath).use { fr -> Properties().apply { load(fr) } }

    operator fun contains(key: String) = conf.containsKey(key) && conf.getProperty(key).isNotEmpty()
    operator fun get(key: String, default: String? = null): String = conf.getProperty(key)
        ?: default
        ?: throw IllegalArgumentException("$key is not present in config!")

    fun opt(key: String, default: String? = null): String? = conf.getProperty(key, default)

    companion object {
        fun load(): Reply {
            val f = File("").absolutePath
            val relativePath = "/resources/replies.properties"

            val configPath = System.getenv("replies")
                ?: f.plus(relativePath)


            return Reply(configPath)
        }
    }
}
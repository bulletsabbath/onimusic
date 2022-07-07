package xyz.oniverse.music.entities

import java.awt.Color
import java.io.File
import java.io.FileReader
import java.util.*

class Config(filePath: String) {



    private val conf = FileReader(filePath).use { fr -> Properties().apply { load(fr) } }

    operator fun contains(key: String) = conf.containsKey(key) && conf.getProperty(key).isNotEmpty()
    operator fun get(key: String, default: String? = null): String = conf.getProperty(key)
        ?: default
        ?: throw IllegalArgumentException("$key is not present in config!")

    fun opt(key: String, default: String? = null): String? = conf.getProperty(key, default)

    val token = get("token")
    val prefix = get("prefix")
    val color = Color.decode(opt("color")) ?: Color.decode("#000000")
    val clientId = get("clientid")
    val clientSecret = get("clientsecret")

    companion object {
        fun load(): Config {
            val f = File("").absolutePath
            val relativePath = "/resources/config.properties"

            val configPath = System.getenv("config")
                ?: f.plus(relativePath)

            println(configPath)


            return Config(configPath)
        }
    }
}
package dev.shog.osmlink.handle

import dev.shog.osmlink.formatTextArray
import org.json.JSONObject

object MessageHandler {
    private val data: JSONObject by lazy {
        val reader = MessageHandler::class.java.getResourceAsStream("/messages.yml")

        JSONObject(String(reader.readBytes()))
    }

    fun getMessage(message: String): String {
        val split = message.split(".").toMutableList()
        val msg = split.last()
        split.removeAt(split.size - 1)

        var pointer = data
        for (spl in split) {
            pointer = pointer.getJSONObject(spl)
        }

        return pointer.getString(msg)
    }

    fun getMessage(message: String, vararg args: String?): String =
        formatTextArray(
            getMessage(
                message
            ), args.toList()
        )
}
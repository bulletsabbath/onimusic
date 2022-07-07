package xyz.oniverse.music.audio.entities

import java.io.*


class TrackData(val requester: Long, val requestedChannel: Long) {

    fun serialize(stream: ByteArrayOutputStream) {
        val writer = DataOutputStream(stream)
        writer.writeInt(1)

        writer.writeLong(requester)
        writer.writeLong(requestedChannel)
        writer.close()
    }

    companion object {
        fun deserialize(stream: ByteArrayInputStream): TrackData? {
            if (stream.available() == 0) {
                return null
            }

            try {
                val reader = DataInputStream(stream)
                val contextType = reader.readInt()
                val requester = reader.readLong()
                val requestedChannel = reader.readLong()

                val ctx = when (contextType) {
                    1 -> TrackData(requester, requestedChannel)
                    else -> throw IllegalArgumentException("Lol what")
                }

                reader.close()
                return ctx
            } catch (e: EOFException) {
                println("End of stream; no user data to be read. Remaining bytes: ${stream.available()}")
                return null
            }
        }
    }
}
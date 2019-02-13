package com.example.democraticlabyrinthjavaclient

import android.util.Log
import com.beust.klaxon.*
import org.jetbrains.anko.doAsync
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


data class GoalData(
    @Json(name = "short_desc")
    val shortDescription: String,
//    val fullDescription: String,
    val aim: Int,
    val progress: Int,
    val achieved: Boolean,
    val achievable: Boolean

)

data class TrapData(
    @Json(name = "pos")
    val position: String,
    val type: String
)

data class GameInfo(
    val name: String,
    val power: Int,
    val color: String,
    val goals: Array<GoalData>,
    @Json(name = "pos_info")
    val traps: Array<TrapData>,
    val id: Int
)


val trapDataConverter = object: Converter {
    override fun canConvert(cls: Class<*>): Boolean {
        return cls == TrapData::class.java
    }

    override fun fromJson(jv: JsonValue): Any? {
        return TrapData(
            jv.objString("pos"),
            jv.objString("type")
        )
    }

    override fun toJson(value: Any): String {
        return """{"type": "${(value as TrapData).type}", "pos": "${value.position}}"""
    }
}


val goalDataConverter = object : Converter{
    override fun canConvert(cls: Class<*>) = cls == GoalData::class.java
    override fun fromJson(jv: JsonValue): Any? {
        return GoalData(
            jv.objString("short_desc"),
            jv.objInt("aim"),
            jv.objInt("progress"),
            (jv.inside as JsonObject).boolean("achieved")!!,
            (jv.inside as JsonObject).boolean("achievable")!!
        )
    }
    override fun toJson(value: Any) = """
        {
        "short_desc": "${(value as GoalData).shortDescription}",
        "aim": ${(value as GoalData).aim},
        "achieved": ${(value as GoalData).achieved},
        "achievable": ${(value as GoalData).achievable},
        "progress": ${(value as GoalData).progress}
        }
    """.trimIndent()
}

class ConnectionUtils {
    val registrationSocket: DatagramSocket = DatagramSocket(5555)
    val playtimeSocket = DatagramSocket(5554)
    var playerName: String = ""
    var serverIP: InetAddress? = null
    var playerId: Int? = null
    var playerGoals: String = ""

    var gameInfo: GameInfo? = null
    val TAG = "ConnUtil"

    fun register(name: String, ip: String) {
        Log.i(TAG, "Starting async")
        doAsync {
            val mPacket = DatagramPacket(name.toByteArray(), name.toByteArray().size)
            mPacket.address = InetAddress.getByName(ip)
            mPacket.port = 6665
            Log.i(TAG, "sending registration packet")
            registrationSocket.send(mPacket)
            Log.i(TAG, "Packet sent")
            val receivedBytes = ByteArray(1024)
            val receivePacket = DatagramPacket(receivedBytes, receivedBytes.size)
            Log.i(TAG, "Ready to receive")
            registrationSocket.receive(receivePacket)
            Log.i(TAG, "got goal packet")
            val byteStream = ByteArrayInputStream(receivePacket.data)
            val receivedStream = DataInputStream(byteStream)
            playerId = receivedStream.readInt()

            val goalBytes = ByteArray(1024)
            try {
                receivedStream.readFully(goalBytes)
            } catch (e: EOFException) {
            }
            playerGoals = String(goalBytes)
            Log.i(TAG, "Goals fully read")
        }
    }

    fun startAutoUpdate(){
        doAsync {
            while(true){
                Log.i(TAG, "loop started")
                val receivedBytes = ByteArray(1024)
                val receivedPacket = DatagramPacket(receivedBytes, receivedBytes.size)
                Log.i(TAG, "Ready to receive info packet")
                playtimeSocket.receive(receivedPacket)
                Log.i(TAG, "got info packet")
                val byteStream = ByteArrayInputStream(receivedPacket.data)
                var resString = byteStream.bufferedReader().use { it.readText() }
                resString = resString.replace("\u0000".toRegex(), "")

                Log.i(TAG, resString)
                Log.i(TAG, "Still alive")
                gameInfo = Klaxon().converter(trapDataConverter).converter(goalDataConverter).parse<GameInfo>(resString)
                Log.i(TAG, gameInfo!!.goals[0].shortDescription + gameInfo!!.color + gameInfo!!.name)
                Log.i(TAG, "loop ended")
            }
        }
    }

    fun sendMovement(direction: Int){
        doAsync {
            val byteOut = ByteArrayOutputStream()
            val dataOut = DataOutputStream(byteOut)

            dataOut.writeInt(playerId!!)
            dataOut.writeInt(direction)

            val bytes = byteOut.toByteArray()

            val mPacket = DatagramPacket(bytes, bytes.size)
            mPacket.address = serverIP
            mPacket.port = 6666

            playtimeSocket.send(mPacket)
        }
    }
}
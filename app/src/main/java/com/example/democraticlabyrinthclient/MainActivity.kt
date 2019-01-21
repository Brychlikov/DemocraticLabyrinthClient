package com.example.democraticlabyrinthclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.*
import java.io.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    val registrationSocket: DatagramSocket = DatagramSocket(5555)
    val playtimeSocket = DatagramSocket(5554)
    var playerId: Int = -1

    val TAG = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun register(ipAddr: Inet4Address, name: String): MutableList<String>{

        val result: MutableList<String> = mutableListOf()

        doAsync {
            Log.i(TAG,  "Start Registering")
            val mPacket = DatagramPacket(name.toByteArray(), name.toByteArray().size)
            mPacket.address = ipAddr
            mPacket.port = 6665
            registrationSocket.send(mPacket)
            Log.i(TAG, "Registration packet sent")
            val receivedBytes = ByteArray(128)
            val receivePacket = DatagramPacket(receivedBytes, receivedBytes.size)
            registrationSocket.receive(receivePacket)
            Log.i(TAG, "Got packet")
            val byteStream = ByteArrayInputStream(receivePacket.data)
            val receivedStream = DataInputStream(byteStream)
            playerId = receivedStream.readInt()
            Log.i(TAG, "Assigned with id $playerId")

            val goalBytes = ByteArray(512)
            try {
                receivedStream.readFully(goalBytes)
            } catch (e: EOFException){}
            val playerGoals = String(goalBytes)

            Log.i(TAG, "Player goals read")

            result.addAll(playerGoals.split('\n'))

            uiThread {
                findViewById<TextView>(R.id.idTextView).text = playerId.toString()
                findViewById<TextView>(R.id.goalTextView).text = playerGoals
                longToast("Registered with id $playerId with goals $playerGoals")
            }
        }


        return result
    }


    fun sendMovement(ipAddr: Inet4Address, direction: Int){
        doAsync {

            val byteOut = ByteArrayOutputStream()
            val dataOut = DataOutputStream(byteOut)

            dataOut.writeInt(playerId)
            dataOut.writeInt(direction)

            val bytes = byteOut.toByteArray()

            val mPacket = DatagramPacket(bytes, bytes.size)
            Log.i(TAG, "Packet with direction $direction sent")
            mPacket.address = ipAddr
            mPacket.port = 6666

            playtimeSocket.send(mPacket)
        }
    }

}

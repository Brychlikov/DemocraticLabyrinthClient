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
    var playerName: String = ""
    var serverIP: InetAddress? = null
    var playerId: Int? = null
    var playerGoals: String = ""

    val TAG = "MyActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "Entered Activity")

    }

    fun register(){
        doAsync {
            Log.i(TAG,  "This works")
            val mPacket = DatagramPacket(playerName.toByteArray(), playerName.toByteArray().size)
            mPacket.address = serverIP
            mPacket.port = 6665
            registrationSocket.send(mPacket)
            val receivedBytes = ByteArray(128)
            val receivePacket = DatagramPacket(receivedBytes, receivedBytes.size)
            registrationSocket.receive(receivePacket)
            Log.i(TAG, "go packet")
            val byteStream = ByteArrayInputStream(receivePacket.data)
            val receivedStream = DataInputStream(byteStream)
            playerId = receivedStream.readInt()

            val goalBytes = ByteArray(512)
            try {
                receivedStream.readFully(goalBytes)
            } catch (e: EOFException){}
            playerGoals = String(goalBytes)

            uiThread {
                findViewById<TextView>(R.id.idTextView).text = playerId.toString()
                findViewById<TextView>(R.id.goalTextView).text = playerGoals
                longToast("Registered with id $playerId with goals $playerGoals")
            }

        }
    }

    fun registerButton(view: View){
        val ipString = findViewById<EditText>(R.id.ipTextEdit).text.toString()
        val resultIP = Inet4Address.getByName(ipString)
        serverIP = resultIP
        playerName = findViewById<EditText>(R.id.nameTextEdit).text.toString()

        register()
    }

    fun sendMovement(view: View){
        doAsync {
            val direction = view.tag.toString().toInt()

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

    fun sendPacket(view: View) {
        doAsync {
            val message = "Testowa wiadomość".toByteArray()
            val mPacket: DatagramPacket = DatagramPacket(message, message.size)
            mPacket.port = 6666
            val remoteAddress = Inet4Address.getByName("192.168.1.55")
            mPacket.address = remoteAddress
            registrationSocket.send(mPacket)
            uiThread {
                toast("Wysłane")
            }
            val receiveArray = ByteArray(128)
            val receivePacket = DatagramPacket(receiveArray, receiveArray.size)
            registrationSocket.receive(receivePacket)
            val recdata = receivePacket.data
            val resultString = String(recdata)

            uiThread {
                toast(resultString)
            }
        }


    }
}

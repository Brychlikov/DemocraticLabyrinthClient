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


    val TAG = "MyActivity"
    var mConnUtils: ConnectionUtils = ConnectionUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "Entered Activity")

        doAsync{
            while(true){
                val goalString = ""
                for(g in mConnUtils.gameInfo.goals){
                    goalString += "${g.shortDescription} \n"
                }
                findViewById<TextView>(R.id.Power_text).text = mConnUtils.gameInfo.power.toString()
                var trapString = ""
                for(t in mConnUtils.gameInfo.traps){
                    trapString += "${t.position} \n"
                }

            }
        }

    }

    fun register_player(view: View){
        val ip_string = findViewById<EditText>(R.id.ipTextEdit).toString()
        val name = findViewById<EditText>(R.id.nameTextEdit).toString()
        mConnUtils.register(name, ip_string)
    }

    fun sendRight(view: View) {
        mConnUtils.sendMovement(0)
    }

    fun sendDown(view: View) {
        mConnUtils.sendMovement(1)
    }

    fun sendLeft(view: View) {
        mConnUtils.sendMovement(2)
    }

    fun sendUp(view: View) {
        mConnUtils.sendMovement(3)
    }
}

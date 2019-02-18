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
        mConnUtils.startAutoUpdate()
    }

    fun register_player(view: View){
        val ip_string = findViewById<EditText>(R.id.ipTextEdit).text.toString()
        val p_name: String = findViewById<EditText>(R.id.nameTextEdit).text.toString()
        mConnUtils.register(p_name, ip_string)
        startUpdating()
    }

    fun startUpdating(){

        doAsync{
            while(true){
                if (!mConnUtils.infoSafe){
                    continue
                }
                var goalString = ""
                for(g in mConnUtils.gameInfo!!.goals){
                    goalString += "${g.shortDescription}: ${g.progress}/${g.aim} \n"
                }
                var trapString = ""
                for(t in mConnUtils.gameInfo!!.traps){
                    trapString += "${t.position} \n"
                }
                uiThread {
                    findViewById<TextView>(R.id.Trap_text).text = trapString
                    findViewById<TextView>(R.id.goalTextView).text = goalString
                    findViewById<TextView>(R.id.Power_text).text = mConnUtils.gameInfo!!.power.toString()
                }

                Thread.sleep(10)
            }
        }
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

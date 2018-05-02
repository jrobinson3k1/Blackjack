package us.jasonrobinson.fun21carnival.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import us.jasonrobinson.fun21carnival.R
import java.io.InputStreamReader

class ExceptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exception)
        try {
            openFileInput("crash.txt").let {
                InputStreamReader(it).useLines {
                    findViewById<TextView>(R.id.crash).text = it.joinToString { it + '\n' }
                }
            }
            deleteFile("crash.txt")
        } catch (e: Exception) {
            startActivity(Intent(this, MainActivity::class.java))
        }

        findViewById<Button>(R.id.closeButton).setOnClickListener { startActivity(Intent(this, MainActivity::class.java)) }
    }
}
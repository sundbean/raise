package com.sundbean.raise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SettingsActivity : AppCompatActivity() {

    private lateinit var logout: Button
    private lateinit var back: ImageButton
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        logout = findViewById(R.id.btnLogout)
        back = findViewById(R.id.btnBackToProfile)
        auth = Firebase.auth

        logout.setOnClickListener {
            auth.signOut()
            // since user is signed out, they need to be directed back to LoginActivity
            val logoutIntent = Intent(this, LoginActivity::class.java)
            // clear the whole backstack
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
        }

        back.setOnClickListener {
            finish()
        }
    }

}
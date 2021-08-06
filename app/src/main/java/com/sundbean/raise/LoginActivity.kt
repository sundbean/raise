package com.sundbean.raise

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 4926
    }

    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var signUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.btnLogin)
        signUpLink = findViewById(R.id.tvSignUpLink)
        auth = Firebase.auth

        // underline sign up link
        signUpLink.setPaintFlags(signUpLink.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        signUpLink.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterAccountActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            // Check that user has entered something into edit text fields
            when {
                TextUtils.isEmpty(etLoginEmail.text.toString().trim { it <= ' ' }) -> {
                    //TODO: validate name
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter your email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(etLoginPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter your password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val email: String = etLoginEmail.text.toString().trim { it <= ' ' }
                    val password: String = etLoginPassword.text.toString().trim { it <= ' ' }

                    //Log in to Firebase
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                // if registration is successfully done, create a firebase user and go the the Set Location Activity
                                if (task.isSuccessful) {
                                    // Create a registered Firebase user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@LoginActivity,
                                        "You are logged in.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@LoginActivity,
                                        MainActivity::class.java
                                    )
                                    //get rid of any Login or Register Activities running in the background
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra(
                                        "user_id",
                                        FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                                    intent.putExtra("email_id", email)
                                    // start next activity with the intent that includes user_id and email_id
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // If the registration is not successful, then show an error message.
                                    Toast.makeText(
                                        this@LoginActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                }
            }
        }

    }
}
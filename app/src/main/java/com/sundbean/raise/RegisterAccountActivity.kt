package com.sundbean.raise

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterAccountActivity : AppCompatActivity() {

    private lateinit var btnCreateAccount: Button
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)

        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        etFullName = findViewById(R.id.etRegisterName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)

        btnCreateAccount.setOnClickListener {
            // Check that user has entered something into edit text fields
            when {
                TextUtils.isEmpty(etFullName.text.toString().trim { it <= ' ' }) -> {
                    //TODO: validate name
                    Toast.makeText(
                        this@RegisterAccountActivity,
                        "Please enter your name.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                    //TODO: validate email address
                    Toast.makeText(
                        this@RegisterAccountActivity,
                        "Please enter your email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterAccountActivity,
                        "Please enter a password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@RegisterAccountActivity,
                        "Please confirm your password.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                //TODO: make sure that passwords match each other, if not then make Toast

                else -> {
                    val name: String = etFullName.text.toString().trim { it <= ' ' }
                    val email: String = etEmail.text.toString().trim { it <= ' ' }
                    val password: String = etPassword.text.toString().trim { it <= ' ' }
                    val confirmedPassword: String =
                        etConfirmPassword.text.toString().trim { it <= ' ' }
                    //TODO: store the name and profile photo of the authenticated user
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener<AuthResult> { task ->
                                // if registration is successfully done, create a firebase user and go the the Set Location Activity
                                if (task.isSuccessful) {
                                    // Create a registered Firebase user
                                    val firebaseUser: FirebaseUser = task.result!!.user!!

                                    Toast.makeText(
                                        this@RegisterAccountActivity,
                                        "You have successfully registered.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    val intent = Intent(
                                        this@RegisterAccountActivity,
                                        RegistrationSetLocationActivity::class.java
                                    )
                                    //get rid of any Login or Register Activities running in the background
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    // start next activity with the intent that includes user_id and email_id
                                    startActivity(intent)
                                    finish()
                                } else {
                                    // If the registration is not successful, then show an error message.
                                    Toast.makeText(
                                        this@RegisterAccountActivity,
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
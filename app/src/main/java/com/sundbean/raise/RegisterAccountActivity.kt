package com.sundbean.raise

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register_account.*
import java.util.*

class RegisterAccountActivity : AppCompatActivity() {

    private lateinit var btnCreateAccount: Button
    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUploadPhoto: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_account)

        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        etFullName = findViewById(R.id.etRegisterName)
        etEmail = findViewById(R.id.etRegisterEmail)
        etPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)
        btnUploadPhoto = findViewById(R.id.cvRoundedProfileImage)

        btnCreateAccount.setOnClickListener {
            performRegister()
        }

        btnUploadPhoto.setOnClickListener {
            Log.d("RegisterAccountActivity", "Try to show photo selector")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data!= null) {
            // proceed and check what the selected image was ...
            Log.d("RegisterAccountActivity", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            llUploadPhotoIconOverlay.setVisibility(View.GONE)
            ivProfileImage.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
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
            TextUtils.isEmpty(etPassword.text.toString()) -> {
                Toast.makeText(
                    this@RegisterAccountActivity,
                    "Please enter a password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            TextUtils.isEmpty(etConfirmPassword.text.toString()) -> {
                Toast.makeText(
                    this@RegisterAccountActivity,
                    "Please confirm your password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            etConfirmPassword.text.toString() !== etPassword.text.toString() -> {
                Toast.makeText(
                    this@RegisterAccountActivity,
                    "Your passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
            }

            //TODO: make sure that passwords match each other, if not then make Toast

            else -> {
                val email: String = etEmail.text.toString().trim { it <= ' ' }
                val password: String = etPassword.text.toString().trim { it <= ' ' }
                //TODO: store the name and profile photo of the authenticated user
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(
                        OnCompleteListener<AuthResult> { task ->
                            // if registration is successfully done, create a firebase user and go the the Set Location Activity
                            if (task.isSuccessful) {
                                // Create a registered Firebase user
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                uploadImageToFirebaseStorage()
                                Toast.makeText(this@RegisterAccountActivity, "You have successfully registered.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@RegisterAccountActivity, RegistrationSetLocationActivity::class.java)
                                //get rid of any Login or Register Activities running in the background
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) {
            //TODO: make profile picture optional
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterAccountActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterAccountActivity", "File location: $it")

                    saveUserToFirestore(it.toString())
                }
            }
            .addOnFailureListener {
                // do some logging here
            }
    }

    private fun saveUserToFirestore(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val db = Firebase.firestore
        val data = hashMapOf(
            "name" to etFullName.text.toString().trim(),
            "profileImageUrl" to profileImageUrl
        )

        db.collection("users").document(uid).set(data)
            .addOnSuccessListener { documentReference ->
                Log.d("RegisterAccountActivity", "Document added to firestore")
            }
            .addOnFailureListener { e ->
                Log.w("RegisterAccountActivity", "Error adding document", e)
            }
    }
}
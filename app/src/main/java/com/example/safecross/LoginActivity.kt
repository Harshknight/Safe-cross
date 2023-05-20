package com.example.safecross

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.safecross.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding:ActivityLoginBinding
    private lateinit var googleSignInClient : GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        auth= FirebaseAuth.getInstance()

        binding.registerLayout.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.login.setOnClickListener {
            if (checking()){
                val email= binding.Email.text.toString()
                val password=  binding.Password.text.toString()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            var intent = Intent(this,MainActivity::class.java)
                            intent.putExtra("email",email)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Wrong Details", Toast.LENGTH_LONG).show()
                        }
                    }
            }
            else{
                Toast.makeText(this,"Enter the Details", Toast.LENGTH_LONG).show()
            }

            }







        // For Google Authentication
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.sign_In))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)

        binding.cardView2.setOnClickListener {
            signInGoogle()
        }


        //initialize Animation
        var fade_in = AnimationUtils.loadAnimation(this,R.anim.fade_in)
        var bottom_down = AnimationUtils.loadAnimation(this,R.anim.bottom_down)

        // setting the bottom down animation on top layout
        binding.topLinearLayout.animation = bottom_down

        // let's create handler for other layouts

        var handler = Handler()
        var runnable = Runnable{
            // let's set fade in animation on other layouts
            binding.cardView5.animation = fade_in
            binding.cardView.animation = fade_in
            binding.cardView2.animation = fade_in
            binding.cardView3.animation = fade_in
            binding.cardView4.animation = fade_in
            binding.textView.animation = fade_in

            binding.registerLayout.animation = fade_in
        }
        handler.postDelayed(runnable,1000)


    }

    // For Google Authentication
    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)
        }
    }

    // For Google Authentication
    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                updateUI(account)
            }
        }else{
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }
    // For Google Authentication
    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        auth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                val intent : Intent = Intent(this , MainActivity::class.java)

                startActivity(intent)
            }else{
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }

    // For phone Authentication
    private fun checking():Boolean
    {
        if( binding.Email.text.toString().trim{it<=' '}.isNotEmpty()
            &&  binding.Password.text.toString().trim{it<=' '}.isNotEmpty())
        {
            return true
        }
        return false
    }


}
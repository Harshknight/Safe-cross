package com.example.safecross

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import com.example.safecross.databinding.ActivityFeedBackBinding

class FeedBackActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedBackBinding
    private lateinit var tvFeedback:TextView
    private lateinit var rbStars:RatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityFeedBackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvFeedback = findViewById(R.id.tvFeedback)
        rbStars = findViewById(R.id.rbStars)



        rbStars.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if(rbStars.rating==0f)
            {
                tvFeedback.setText("Very Dissatisfied")
            }
            else if(rbStars.rating==1f)
            {
                tvFeedback.setText(" Dissatisfied")
            }
            else if(rbStars.rating==2f || rbStars.rating==3f)
            {
                tvFeedback.setText(" OK ")
            }
            else if(rbStars.rating==4f)
            {
                tvFeedback.setText("Satisfied")
            }
            else if(rbStars.rating==5f)
            {
                tvFeedback.setText("Very Satisfied")
            }


            binding.btnsend.setOnClickListener {
                if (binding.edtext.text.toString().trim { it <= ' ' }.isNotEmpty()) {
                    Toast.makeText(this, "Feedback Send", Toast.LENGTH_SHORT)
                    startActivity(Intent(this,MainActivity::class.java))
                }else{
                    Toast.makeText(this, "Please Enter Your Feedback", Toast.LENGTH_SHORT)
                }
            }
        }
    }
}



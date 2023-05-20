package com.example.safecross

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.example.safecross.databinding.ActivityIncidentReportBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class IncidentReport : AppCompatActivity() {
    private lateinit var incidentReportRef: DatabaseReference
    private lateinit var binding :ActivityIncidentReportBinding
    private lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editText =findViewById(R.id.editText)


        binding.save.setOnClickListener {
            addDataToFirebase()
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        binding.backButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }


    }

    private fun addDataToFirebase() {
        editText =findViewById(R.id.editText)
        //submit this list to Firebase
        FirebaseDatabase.getInstance()
            .getReference("IncidentReport")
            .child("TypeofIncidence")
            .setValue(editText.text.toString())
            .addOnCompleteListener {
                Toast.makeText(this,"Your Report has been Taken", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { ex ->
                Toast.makeText(this,""+ex.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
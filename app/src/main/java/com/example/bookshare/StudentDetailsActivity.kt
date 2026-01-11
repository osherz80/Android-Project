package com.example.bookshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookshare.model.Model

class StudentDetailsActivity : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var idTextView: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var avatarImageView: ImageView
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_student_details)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.detailsToolbar)
        setSupportActionBar(toolbar)
        
        // Enable back button in toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        nameTextView = findViewById(R.id.detailsNameValue)
        idTextView = findViewById(R.id.detailsIdValue)
        checkBox = findViewById(R.id.detailsCheckBox)
        avatarImageView = findViewById(R.id.detailsAvatar)
        editButton = findViewById(R.id.detailsEditButton)

        val studentId = intent.getStringExtra("student_id")

        if (studentId != null) {
            val student = Model.shared.getStudentById(studentId)
            if (student != null) {
                nameTextView.text = student.name
                idTextView.text = student.id
                checkBox.isChecked = student.checkStatus
                // Assuming avatarUrl means resource name or logic, but for now using placeholder
                avatarImageView.setImageResource(R.drawable.avatar_placeholder) 
            } else {
                 Toast.makeText(this, "Student not found!", Toast.LENGTH_SHORT).show()
                 finish()
            }
        } else {
            Toast.makeText(this, "No student ID provided!", Toast.LENGTH_SHORT).show()
            finish()
        }

        editButton.setOnClickListener {
            val intent = Intent(this, EditStudentActivity::class.java)
            intent.putExtra("student_id", studentId)
            startActivity(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data in case it was changed
        val studentId = intent.getStringExtra("student_id")
        if (studentId != null) {
             val student = Model.shared.getStudentById(studentId)
             if (student != null) {
                 nameTextView.text = student.name
                 idTextView.text = student.id
                 checkBox.isChecked = student.checkStatus
             } else {
                 // Student might have been deleted
                 finish()
             }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

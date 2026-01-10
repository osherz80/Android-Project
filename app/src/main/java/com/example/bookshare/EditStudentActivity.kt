package com.example.bookshare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookshare.model.Model
import com.example.bookshare.model.Student

class EditStudentActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var cancelButton: Button
    
    private var studentId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.editToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameEditText = findViewById(R.id.editNameEditText)
        idEditText = findViewById(R.id.editIdEditText)
        checkBox = findViewById(R.id.editCheckBox)
        saveButton = findViewById(R.id.editSaveButton)
        deleteButton = findViewById(R.id.editDeleteButton)
        cancelButton = findViewById(R.id.editCancelButton)

        studentId = intent.getStringExtra("student_id")

        
        if (studentId != null) {
            val student = studentId?.let {
                Model.shared.getStudentById(it)
            }
            if (student != null) {
                nameEditText.setText(student.name)
                idEditText.setText(student.id)
                checkBox.isChecked = student.checkStatus
            } else {
                 Toast.makeText(this, "Student not found!", Toast.LENGTH_SHORT).show()
                 finish()
            }
        } else {
            Toast.makeText(this, "No student ID provided!", Toast.LENGTH_SHORT).show()
            finish()
        }

        saveButton.setOnClickListener {
            updateStudent()
        }

        deleteButton.setOnClickListener {
            deleteStudent()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun updateStudent() {
        val name = nameEditText.text.toString().trim()
        val isChecked = checkBox.isChecked
        
        // id is read-only in this screen, so we rely on the member variable
        if (studentId == null) return

        if (name.isEmpty()) {
             Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
             return
        }

        studentId?.let{
            val updatedStudent = Student(id = it, name = name, checkStatus = isChecked)
            Model.shared.updateStudent(it, updatedStudent)
        }

        
        Toast.makeText(this, "Student updated!", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

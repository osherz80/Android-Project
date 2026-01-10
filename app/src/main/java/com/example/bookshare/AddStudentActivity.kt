package com.example.bookshare

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
import com.google.android.material.appbar.MaterialToolbar

class AddStudentActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_student)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: MaterialToolbar = findViewById(R.id.addToolbar)
        setSupportActionBar(toolbar)

        nameEditText = findViewById(R.id.addNameEditText)
        idEditText = findViewById(R.id.addIdEditText)
        checkBox = findViewById(R.id.addCheckBox)
        saveButton = findViewById(R.id.addSaveButton)
        cancelButton = findViewById(R.id.addCancelButton)

        saveButton.setOnClickListener {
            saveStudent()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun saveStudent() {
        val name = nameEditText.text.toString().trim()
        val id = idEditText.text.toString().trim()
        val isChecked = checkBox.isChecked

        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(this, "Please enter both Name and ID", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Simple check if ID already exists
        val existingStudent = Model.shared.getStudentById(id)
        if (existingStudent != null) {
             Toast.makeText(this, "Student with this ID already exists!", Toast.LENGTH_SHORT).show()
             return
        }

        val student = Student(id = id, name = name, checkStatus = isChecked)
        Model.shared.addStudent(student)
        
        Toast.makeText(this, "Student saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

package com.example.bookshare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshare.adapter.StudentsAdapter
import com.example.bookshare.model.Model
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var studentsRecyclerView: RecyclerView
    private lateinit var addStudentFab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: com.google.android.material.appbar.MaterialToolbar = findViewById(R.id.mainToolbar)
        setSupportActionBar(toolbar)

        studentsRecyclerView = findViewById(R.id.studentsRecyclerView)
        addStudentFab = findViewById(R.id.addStudentFab)

        studentsRecyclerView.setHasFixedSize(true)
        studentsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // In the future (Milestone 6), we should move this to onResume to refresh the list
        
        addStudentFab.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list
        val adapter = StudentsAdapter(Model.shared.getAllStudents())
        adapter.onStudentClick = { student ->
             val intent = Intent(this, StudentDetailsActivity::class.java)
             intent.putExtra("student_id", student.id)
             startActivity(intent)
        }
        studentsRecyclerView.adapter = adapter
    }
}
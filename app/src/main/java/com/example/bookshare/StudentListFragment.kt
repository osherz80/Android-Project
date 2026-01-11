package com.example.bookshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshare.adapter.StudentsAdapter
import com.example.bookshare.model.Model

class StudentListFragment : Fragment() {

    private lateinit var studentsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_student_list, container, false)

        studentsRecyclerView = view.findViewById(R.id.studentsRecyclerView)
        studentsRecyclerView.setHasFixedSize(true)
        studentsRecyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list
        val adapter = StudentsAdapter(Model.shared.getAllStudents())
        adapter.onStudentClick = { student ->
             // Milestone 2.3: We will add Navigation here later
             Toast.makeText(context, "Clicked on ${student.name}", Toast.LENGTH_SHORT).show()
        }
        studentsRecyclerView.adapter = adapter
    }
}

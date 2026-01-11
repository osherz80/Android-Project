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

import androidx.navigation.fragment.findNavController

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

        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.menu_student_list, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        findNavController().navigate(R.id.action_studentListFragment_to_newStudentFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, androidx.lifecycle.Lifecycle.State.RESUMED)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list
        val adapter = StudentsAdapter(Model.shared.getAllStudents())
        adapter.onStudentClick = { student ->
             val bundle = Bundle()
             bundle.putString("student_id", student.id)
             findNavController().navigate(R.id.action_studentListFragment_to_studentDetailsFragment, bundle)
        }
        studentsRecyclerView.adapter = adapter
    }
}

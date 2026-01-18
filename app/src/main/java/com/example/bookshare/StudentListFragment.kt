package com.example.bookshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookshare.adapter.StudentsAdapter
import com.example.bookshare.databinding.FragmentStudentListBinding
import com.example.bookshare.model.Model

class StudentListFragment : Fragment() {

    private var binding: FragmentStudentListBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentListBinding.inflate(inflater, container, false)
        val view = binding?.root

        binding?.studentsRecyclerView?.setHasFixedSize(true)
        binding?.studentsRecyclerView?.layoutManager = LinearLayoutManager(context)

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
        binding?.studentsRecyclerView?.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

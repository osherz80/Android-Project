package com.example.bookshare

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookshare.databinding.FragmentEditStudentBinding
import com.example.bookshare.model.Model
import com.example.bookshare.model.Student
import java.util.Calendar

class EditStudentFragment : Fragment() {

    private var binding: FragmentEditStudentBinding? = null
    private var studentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditStudentBinding.inflate(inflater, container, false)
        val view = binding?.root

        studentId = arguments?.getString("student_id")

        if (studentId != null) {
            val student = Model.shared.getStudentById(studentId!!)
            if (student != null) {
                binding?.editNameEditText?.setText(student.name)
                binding?.editIdEditText?.setText(student.id)
                binding?.editCheckBox?.isChecked = student.checkStatus
                
                // Parse birthDate if available
                if (student.birthDate.isNotEmpty()) {
                    val parts = student.birthDate.split(" ")
                    if (parts.isNotEmpty()) {
                        binding?.editDateEditText?.setText(parts[0])
                        if (parts.size > 1) {
                            binding?.editTimeEditText?.setText(parts[1])
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Student not found!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() 
            }
        } else {
            Toast.makeText(context, "No student ID provided!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }

        binding?.editDateEditText?.setOnClickListener {
            showDatePicker()
        }

        binding?.editTimeEditText?.setOnClickListener {
            showTimePicker()
        }

        binding?.editSaveButton?.setOnClickListener {
            updateStudent()
        }

        binding?.editDeleteButton?.setOnClickListener {
            deleteStudent()
        }

        binding?.editCancelButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            binding?.editDateEditText?.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding?.editTimeEditText?.setText(formattedTime)
        }, hour, minute, true).show()
    }

    private fun updateStudent() {
        val name = binding?.editNameEditText?.text.toString().trim()
        val isChecked = binding?.editCheckBox?.isChecked
        val date = binding?.editDateEditText?.text.toString().trim()
        val time = binding?.editTimeEditText?.text.toString().trim()
        
        // id is read-only in this screen
        if (studentId == null) return

        if (name.isEmpty()) {
            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val birthDate = if (date.isNotEmpty() && time.isNotEmpty()) {
            "$date $time"
        } else if (date.isNotEmpty()) {
            date
        } else {
            ""
        }

        studentId?.let {
            val updatedStudent = Student(id = it, name = name, checkStatus = isChecked == true, birthDate = birthDate)
            Model.shared.updateStudent(it, updatedStudent)
        }

        Toast.makeText(context, "Student updated!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun deleteStudent() {
        studentId?.let {
            Model.shared.deleteStudent(it)
            Toast.makeText(context, "Student deleted!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack(R.id.studentListFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

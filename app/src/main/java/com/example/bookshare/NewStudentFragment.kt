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
import com.example.bookshare.databinding.FragmentNewStudentBinding
import com.example.bookshare.model.Model
import com.example.bookshare.model.Student
import java.util.Calendar

class NewStudentFragment : Fragment() {

    private var binding: FragmentNewStudentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewStudentBinding.inflate(inflater, container, false)

        binding?.addDateEditText?.setOnClickListener {
            showDatePicker()
        }

        binding?.addTimeEditText?.setOnClickListener {
            showTimePicker()
        }

        binding?.addSaveButton?.setOnClickListener {
            saveStudent()
        }

        binding?.addCancelButton?.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding?.root
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val formattedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
            binding?.addDateEditText?.setText(formattedDate)
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            binding?.addTimeEditText?.setText(formattedTime)
        }, hour, minute, true).show()
    }

    private fun saveStudent() {
        val name = binding?.addNameEditText?.text.toString().trim()
        val id = binding?.addIdEditText?.text.toString().trim()
        val isChecked = binding?.addCheckBox?.isChecked
        val date = binding?.addDateEditText?.text.toString().trim()
        val time = binding?.addTimeEditText?.text.toString().trim()

        if (name.isEmpty() || id.isEmpty()) {
            Toast.makeText(context, "Please enter both Name and ID", Toast.LENGTH_SHORT).show()
            return
        }

        val birthDate = if (date.isNotEmpty() && time.isNotEmpty()) {
            "$date $time"
        } else if (date.isNotEmpty()) {
            date
        } else {
            ""
        }

        // Simple check if ID already exists
        val existingStudent = Model.shared.getStudentById(id)
        if (existingStudent != null) {
            Toast.makeText(context, "Student with this ID already exists!", Toast.LENGTH_SHORT).show()
            return
        }

        val student = Student(id = id, name = name, checkStatus = isChecked == true, birthDate = birthDate)
        Model.shared.addStudent(student)

        Toast.makeText(context, "Student saved!", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

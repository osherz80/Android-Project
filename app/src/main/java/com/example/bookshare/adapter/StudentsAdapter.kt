package com.example.bookshare.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookshare.R
import com.example.bookshare.model.Model
import com.example.bookshare.model.Student

class StudentsAdapter(private val students: List<Student>) :
    RecyclerView.Adapter<StudentsAdapter.StudentViewHolder>() {

    var onStudentClick: ((Student) -> Unit)? = null

    class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.studentRowNameTextView)
        val idTextView: TextView = itemView.findViewById(R.id.studentRowIdTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.studentRowCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.student_list_row, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.nameTextView.text = student.name
        holder.idTextView.text = "ID: ${student.id}"
        
        // Remove listener before setting state to avoid triggering it while scrolling
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = student.checkStatus

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            Model.shared.updateStudentStatus(student.id, isChecked)
        }

        holder.itemView.setOnClickListener {
            onStudentClick?.invoke(student)
        }
    }

    override fun getItemCount(): Int = students.size
}

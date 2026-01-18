package com.example.bookshare.model

class Model private constructor() {

    private val students: MutableList<Student> = ArrayList()

    companion object {
        val shared = Model()
    }

    init {
        // initial data for the screen
        students.add(Student(id = "123456", name = "Alice Smith", checkStatus = false, birthDate = "2000-01-01 12:00"))
        students.add(Student(id = "789012", name = "Bob Jones", checkStatus = true, birthDate = "1999-05-15 08:30"))
        students.add(Student(id = "345678", name = "Charlie Brown", checkStatus = false, birthDate = "2001-12-31 23:59"))
    }

    fun getAllStudents(): List<Student> {
        return students
    }

    fun addStudent(student: Student) {
        students.add(student)
    }

    fun getStudentById(id: String): Student? {
        return students.find { it.id == id }
    }

    fun updateStudent(oldId: String, updatedStudent: Student) {
        val index = students.indexOfFirst { it.id == oldId }
        if (index != -1) {
            students[index] = updatedStudent
        }
    }
    
    fun deleteStudent(id: String) {
         val index = students.indexOfFirst { it.id == id }
         if (index != -1) {
             students.removeAt(index)
         }
    }

    fun updateStudentStatus(id: String, isChecked: Boolean) {
        val index = students.indexOfFirst { it.id == id }
        if (index != -1) {
            val student = students[index]
            val newStudent = student.copy(checkStatus = isChecked)
            students[index] = newStudent
        }
    }
}

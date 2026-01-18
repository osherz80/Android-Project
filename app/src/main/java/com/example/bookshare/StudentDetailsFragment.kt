package com.example.bookshare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bookshare.model.Model

class StudentDetailsFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var idTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var avatarImageView: ImageView
    private var studentId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_student_details, container, false)

        nameTextView = view.findViewById(R.id.detailsNameValue)
        idTextView = view.findViewById(R.id.detailsIdValue)
        phoneTextView = view.findViewById(R.id.detailsPhoneValue)
        addressTextView = view.findViewById(R.id.detailsAddressValue)
        checkBox = view.findViewById(R.id.detailsCheckBox)
        avatarImageView = view.findViewById(R.id.detailsAvatar)

        // Arguments are passed strictly via Bundle in Navigation Component
        studentId = arguments?.getString("student_id")

        val menuHost: androidx.core.view.MenuHost = requireActivity()
        menuHost.addMenuProvider(object : androidx.core.view.MenuProvider {
            override fun onCreateMenu(menu: android.view.Menu, menuInflater: android.view.MenuInflater) {
                menuInflater.inflate(R.menu.menu_student_details, menu)
            }

            override fun onMenuItemSelected(menuItem: android.view.MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_edit -> {
                         val bundle = Bundle()
                         bundle.putString("student_id", studentId)
                         findNavController().navigate(R.id.action_studentDetailsFragment_to_editStudentFragment, bundle)
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
        if (studentId != null) {
             val student = Model.shared.getStudentById(studentId!!)
             if (student != null) {
                 nameTextView.text = student.name
                 idTextView.text = student.id
                 phoneTextView.text = student.phone
                 addressTextView.text = student.address
                 checkBox.isChecked = student.checkStatus
                 avatarImageView.setImageResource(R.drawable.avatar_placeholder)
             } else {
                 // Student might have been deleted, pop back
                 findNavController().popBackStack()
             }
        } else {
             Toast.makeText(context, "No student ID provided!", Toast.LENGTH_SHORT).show()
             findNavController().popBackStack()
        }
    }
}

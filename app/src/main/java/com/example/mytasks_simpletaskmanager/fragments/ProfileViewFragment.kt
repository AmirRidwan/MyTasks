package com.example.mytasks_simpletaskmanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.databinding.FragmentProfileViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileViewFragment : Fragment() {

    private lateinit var binding: FragmentProfileViewBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileViewFragment_to_profileEditFragment)
        }

        getUserProfileFromFirestore()

        // Override back press to navigate to HomeFragment
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_profileViewFragment_to_homeFragment)
            }
        })
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser?.uid ?: ""

        if (authId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        firestore = FirebaseFirestore.getInstance()
    }

    private fun getUserProfileFromFirestore() {
        val userDocRef = firestore.collection("users").document(authId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val bio = document.getString("bio")
                    val profileImageUrl = document.getString("profileImageUrl")

                    binding.profileName.setText(name)
                    binding.profileEmail.setText(email)
                    binding.profileBio.setText(bio)

                    profileImageUrl?.let { url ->
                        Glide.with(this).load(url).circleCrop().into(binding.profileImage)
                    }
                } else {
                    Toast.makeText(context, "No such document", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
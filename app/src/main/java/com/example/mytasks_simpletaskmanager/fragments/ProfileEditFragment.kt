package com.example.mytasks_simpletaskmanager.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mytasks_simpletaskmanager.R
import com.example.mytasks_simpletaskmanager.databinding.FragmentProfileEditBinding
import com.example.mytasks_simpletaskmanager.utils.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class ProfileEditFragment : Fragment() {

    private val TAG = "ProfileEditFragment"
    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var auth: FirebaseAuth
    private lateinit var authId: String
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()

        binding.profileImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        binding.saveButton.setOnClickListener {
            saveUserProfile()
        }

        getUserProfileFromFirestore()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        authId = auth.currentUser?.uid ?: ""

        if (authId.isEmpty()) {
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("profileImages").child(authId)
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

    private fun saveUserProfile() {
        val name = binding.profileName.text.toString()
        val email = binding.profileEmail.text.toString()
        val bio = binding.profileBio.text.toString()

        val userProfile = UserProfile(name, email, bio, null)

        firestore.collection("users").document(authId).set(userProfile)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    if (selectedImageUri != null) {
                        uploadImageToFirebase()
                    } else {
                        findNavController().navigate(R.id.action_profileEditFragment_to_profileViewFragment)
                    }
                } else {
                    Toast.makeText(context, "Profile update failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebase() {
        selectedImageUri?.let { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uri)
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = storageReference.putBytes(data)
            uploadTask.addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    firestore.collection("users").document(authId).update("profileImageUrl", uri.toString())
                    findNavController().navigate(R.id.action_profileEditFragment_to_profileViewFragment)
                    Toast.makeText(context, "Profile image updated successfully", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Image upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            binding.profileImage.setImageURI(selectedImageUri)
        }
    }
}
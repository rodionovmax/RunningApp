package com.rodionovmax.runningapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.databinding.FragmentAccountBinding
import com.rodionovmax.runningapp.other.Constants
import com.rodionovmax.runningapp.other.Constants.KEY_FIREBASE_EMAIL
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    val binding get() = _binding!!

    private lateinit var emailId: String
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        emailId = sharedPreferences.getString(KEY_FIREBASE_EMAIL, "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvAccount.text = "Account $emailId"

        binding.buttonChangeEmail.setOnClickListener {
            Toast.makeText(requireActivity(), "Action will be added soon", Toast.LENGTH_SHORT)
                .show()
        }

        binding.buttonDisplay.setOnClickListener {
            Toast.makeText(requireActivity(), "Action will be added soon", Toast.LENGTH_SHORT)
                .show()
        }

        binding.buttonLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(activity, LoginActivity::class.java))
            requireActivity().finish()
        }
    }

}
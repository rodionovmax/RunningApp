package com.rodionovmax.runningapp.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.databinding.FragmentSettingsBinding
import com.rodionovmax.runningapp.other.Constants.KEY_NAME
import com.rodionovmax.runningapp.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var myMenu: Menu? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
                myMenu = menu
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                myMenu?.getItem(1)?.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mi_account -> {
                        findNavController().navigate(R.id.action_settingsFragment_to_accountFragment)
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        loadFieldsFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else {
                Snackbar.make(view, "Please fill out all fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(KEY_NAME, "")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Let's go $nameText"
        val toolbar: MaterialTextView = requireActivity().findViewById(R.id.tv_toolbar_title)
        toolbar.text = toolbarText
        return true
    }

}

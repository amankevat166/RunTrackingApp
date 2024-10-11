package com.example.runningapp2.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.runningapp2.R
import com.example.runningapp2.databinding.FragmentSettingBinding
import com.example.runningapp2.other.Constants.KEY_NAME
import com.example.runningapp2.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFrag : Fragment(R.layout.fragment_setting) {

    lateinit var binding:FragmentSettingBinding
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        loadFieldsFromSharedPref()
        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success){
                Snackbar.make(view,"Saved Changes",Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(view,"Please fill out all the fields", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldsFromSharedPref(){
        val name = sharedPreferences.getString(KEY_NAME,"")
        val weight = sharedPreferences.getFloat(KEY_WEIGHT,80f)

        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean{
        val nameTxt = binding.etName.text.toString()
        val weightTxt = binding.etWeight.text.toString()
        if (nameTxt.isEmpty() || weightTxt.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME,nameTxt)
            .putFloat(KEY_WEIGHT,weightTxt.toFloat())
            .apply()
        return true
    }

}
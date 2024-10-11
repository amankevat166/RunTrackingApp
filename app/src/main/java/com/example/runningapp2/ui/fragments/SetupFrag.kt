package com.example.runningapp2.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningapp2.R
import com.example.runningapp2.databinding.FragmentRunBinding
import com.example.runningapp2.databinding.FragmentSetupBinding
import com.example.runningapp2.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp2.other.Constants.KEY_NAME
import com.example.runningapp2.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SetupFrag : Fragment(R.layout.fragment_setup) {

    lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DataBindingUtil.bind(view)!!
        val tvContinue = view.findViewById<TextView>(R.id.tvContinue)

        if (!isFirstAppOpen){
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFrag,true)
                .build()
            findNavController().navigate(
                R.id.action_setupFrag_to_runFrag,
                savedInstanceState,
                navOptions
            )
        }
       tvContinue.setOnClickListener{
           val success = writePersonalDataToSharedPref()
           if (success){
               findNavController().navigate(R.id.action_setupFrag_to_runFrag)
           }else{
               Snackbar.make(requireView(), "Please enter all the fields",Snackbar.LENGTH_SHORT).show()
           }

        }
    }

    private fun writePersonalDataToSharedPref(): Boolean{
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()){
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME,name)
            .putFloat(KEY_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()
        //val toolbarTxt = "Let's go, $name!"
        return true
    }

}
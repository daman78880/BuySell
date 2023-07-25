package com.buysell.screens.guestaccount

import SharedPref
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.databinding.FragmentGuestProfileBinding
import setToolBar


class GuestAccountFragment : Fragment() {
    private lateinit var binding:FragmentGuestProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentGuestProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        val appBarTitle = requireActivity().resources.getString(R.string.myProfile)
        setToolBar(binding.appBarGuestAccount, requireContext(), true, false, appBarTitle, 10f,true)
        clickListener()
    }
    private fun clickListener(){
        binding.apply {
            cLayoutHelpAndSupportGuestAccount.setOnClickListener {
                findNavController().navigate(R.id.action_guestAccountFragment_to_helpAndSupportFragment)
            }
            cLayoutLoginRegisterGuestAccount.setOnClickListener {
                    SharedPref(requireContext()).clearData()
                    findNavController().navigate(R.id.action_guestAccountFragment_to_welcomeFragment)
            }
        }
    }

}
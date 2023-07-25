package com.buysell.screens.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.databinding.FragmentNotificationBinding
import setToolBar


class NotificationFragment : Fragment() {
    private lateinit var binding:FragmentNotificationBinding
    private lateinit var list:ArrayList<NotificationPojo>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding= FragmentNotificationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        val appBarTitle=requireActivity().resources.getString(R.string.notifications)
        setToolBar(binding.appBarNotificaion,requireContext(),true,true,appBarTitle,10f)

        setAdapter()
        clickListners()
    }

    private fun clickListners() {
        binding.apply {
            appBarNotificaion.tbBackBtn.setOnClickListener {
                findNavController().popBackStack()
            }
            appBarNotificaion.tbLastTxt.setOnClickListener {
                Toast.makeText(requireContext(), "first pay 1000 rupes then clear notification function work", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setAdapter(){
        list=ArrayList()
        for(i in 1..20){
            list.add(NotificationPojo("$i title","$i Message",i))
        }

        binding.rvNotification.adapter=NotificationAdapter(requireContext(),list)
    }

}
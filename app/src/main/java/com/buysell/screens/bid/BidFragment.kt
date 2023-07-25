package com.buysell.screens.bid

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.buysell.R
import com.buysell.databinding.FragmentBidBinding
import com.buysell.screens.productdetail.PojoProductDetail
import dagger.hilt.android.AndroidEntryPoint
import hideKeyBoard
import setToolBar

class BidFragment : Fragment() {
    private lateinit var binding:FragmentBidBinding
    private var previousBundle:Bundle?=null
    private var passDataClass: PojoProductDetail?=null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding=FragmentBidBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }
    private fun init(){
        if(arguments!=null)
        previousBundle=arguments

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            passDataClass=previousBundle?.getParcelable("passDataClass",PojoProductDetail::class.java)
        }
        else{
            passDataClass=previousBundle?.getParcelable("passDataClass")
        }

        if(passDataClass!=null && passDataClass?.data?.title?.isNotEmpty()!!)
        {
            binding.etPriceBid.setText( passDataClass!!.data.price)
            binding.etMinBidBid.setText( passDataClass!!.data.minBid)
        }
        val appBarTitle = requireActivity().resources.getString(R.string.addSomeData)
        setToolBar(binding.appBarBid, requireContext(), true, false, appBarTitle, 10f)

        clickListerner()
        binding.apply {

        }
    }
    private fun clickListerner(){
        binding.apply {
            btnNextBid.setOnClickListener {
                if(etPriceBid.text?.isNotEmpty()!!){
                 if(etMinBidBid.text?.isNotEmpty()!!){
                     val price:Double=etPriceBid.text?.toString()?.toDouble()?:0.0
                     val minPrice:Double=etMinBidBid.text?.toString()?.toDouble()?:0.0
                     if(minPrice <= price ){
                         previousBundle?.putString("price",etPriceBid.text.toString())
                         previousBundle?.putString("minBid",etMinBidBid.text.toString())
                         findNavController().navigate(R.id.action_bidFragment_to_locationFragment,previousBundle)
                     }else{
                         Toast.makeText(requireContext(), "Please enter valid min bid amount", Toast.LENGTH_SHORT).show()
                     }
                 }
                else{
                     Toast.makeText(requireContext(), "Please enter min bid", Toast.LENGTH_SHORT).show()
                 }
            }
                else{
                    Toast.makeText(requireContext(), "Please enter price", Toast.LENGTH_SHORT).show()
                }
            }
            viewBid.setOnClickListener {
                requireActivity().hideKeyBoard()
            }

        }
    }

}
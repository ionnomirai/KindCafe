package com.example.kindcafe.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kindcafe.KindCafeApplication
import com.example.kindcafe.MainActivity
import com.example.kindcafe.R
import com.example.kindcafe.adapters.AdapterBasket
import com.example.kindcafe.data.DetailedOrderItem
import com.example.kindcafe.databinding.FragBasketBinding
import com.example.kindcafe.firebase.DbManager
import com.example.kindcafe.utils.AuxillaryFunctions
import com.example.kindcafe.utils.Locations
import com.example.kindcafe.viewModels.MainViewModel
import kotlinx.coroutines.launch

class BasketFrag: Fragment() {
    /*---------------------------------------- Properties ----------------------------------------*/
    private var _binding: FragBasketBinding? = null
    private val binding
        get() : FragBasketBinding {
            return checkNotNull(_binding) {
                "Cannot access binding because it is null. Is the view visible"
            }
        }

    /* Common viewModel between activity and this fragment */
    private val mainVM: MainViewModel by activityViewModels()

    private val my_tag = "BasketFragmentTag"
    private val currentFragmentName = "Basket"

    private lateinit var myAdapter : AdapterBasket
    private val dbManager = DbManager()

    private val detailedListI = mutableListOf<DetailedOrderItem>()

    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
    private lateinit var deleteIcon: Drawable

    /*---------------------------------------- Functions -----------------------------------------*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragBasketBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)!!

        myAdapter = AdapterBasket(
            AuxillaryFunctions.deafultItemMoveDirections(this, mainVM, null),
            AuxillaryFunctions.defaultClickSettingOrder(this, mainVM)
        )

        mainVM.currentLocation
        val mainAct = activity as? MainActivity

        mainAct?.let {
            it.binding.tvToolbarTitle.text = currentFragmentName
            it.supportActionBar?.title = ""
        }

        binding.rvItemsBuy.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myAdapter
        }

        Log.d(my_tag, "basket: ${mainVM.orderBasket.value}")

        viewLifecycleOwner.lifecycleScope.launch{
            mainVM.getOrderItemsLocal()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.orderBasket.collect{
                dbManager.setOrderBasketToRDB(KindCafeApplication.myAuth.currentUser, it)

                val detailedList = AuxillaryFunctions.transformOrdItemToDishesDetailed(
                    it, mainVM.allDishes.value
                )
                detailedListI.clear()
                detailedListI.addAll(detailedList)

                Log.d(my_tag, "size detailed list: ${detailedList.size}")
                Log.d(my_tag, "list: ${detailedList}")
                myAdapter.setNewData(detailedList)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            mainVM.needUpdate.collect {
                if (it) {
                    myAdapter.notifyDataSetChanged()
                    mainVM.needUpdate.value = false
                }
            }
        }

        binding.clButtonMakeOrder.setOnClickListener {

            val someDishesHaveNotQuantity = detailedListI
                .filter { it.count == "0" || it.count == null}
                .isNotEmpty()
            /* if all dishes have quantity (count), then we can move further */
            if (!someDishesHaveNotQuantity){
                val action = BasketFragDirections.actionBasketFragToOrderSummaryFragment(detailedListI.toTypedArray())
                findNavController().navigate(action)
            } else {
                Toast.makeText(context, R.string.quatity_zero, Toast.LENGTH_SHORT).show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(AuxillaryFunctions.defaultSwipeDelBasketOrder(
            deleteIcon, swipeBackground, mainVM = mainVM
        ))
        itemTouchHelper.attachToRecyclerView(binding.rvItemsBuy)
    }

    override fun onResume() {
        super.onResume()
        mainVM.currentLocation = Locations.BASKET.nameL
        Log.d(my_tag, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
package com.dam.acmeexplorer.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dam.acmeexplorer.databinding.ActivitySelectedTravelsBinding
import com.dam.acmeexplorer.listadapters.TravelListAdapter
import com.dam.acmeexplorer.viewmodels.SelectedTravelsViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class SelectedTravelsActivity : AppCompatActivity() {

    private val vm: SelectedTravelsViewModel by viewModel()
    private lateinit var binding: ActivitySelectedTravelsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySelectedTravelsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            vm.travels.observe(this@SelectedTravelsActivity) {

                travelList.adapter = TravelListAdapter(this@SelectedTravelsActivity, it, vm.userTravels) { travelPos: Int, checkboxClicked: Boolean ->
                    onItemClick(travelPos, checkboxClicked)
                }

                if(it.size > 0) {
                    Toast.makeText(this@SelectedTravelsActivity, "Hay ${it.size} viajes seleccionados", Toast.LENGTH_SHORT).show()
                }
            }
        }

        vm.requestTravels()
    }

    private fun onItemClick(travelPos: Int, checkboxClicked: Boolean): Boolean {
        if (checkboxClicked) {
            with(binding) {
                if(travelList.isComputingLayout){
                    return true
                }

                vm.onCheckboxClicked(travelPos)

                travelList.adapter?.notifyItemRemoved(travelPos)
                travelList.adapter?.notifyItemRangeChanged(travelPos, vm.userTravels.size)
            }
        } else {
            startActivity(vm.getTravelIntent(this@SelectedTravelsActivity, travelPos))
        }

        return false
    }
}

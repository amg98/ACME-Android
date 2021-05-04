package com.dam.acmeexplorer.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.slider.library.Animations.DescriptionAnimation
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivityTravelDetailBinding
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.viewmodels.TravelDetailViewModel
import com.squareup.picasso.Picasso
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class TravelDetailActivity : AppCompatActivity() {

    private val vm: TravelDetailViewModel by viewModel()
    private lateinit var binding: ActivityTravelDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTravelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val travel = intent.getParcelableExtra<Travel>("TRAVEL")
        val buyEnabled = intent.getBooleanExtra("BUY", false)

        vm.updateSelectButton(travel.id, buyEnabled)

        with(binding) {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            title.text = travel.title
            startDate.text = getString(R.string.start_date, dateFormatter.format(travel.startDate))
            endDate.text = getString(R.string.end_date, dateFormatter.format(travel.endDate))
            price.text = getString(R.string.price, travel.price)
            startPlace.text = getString(R.string.start_place, travel.startPlace)
            Picasso.with(this@TravelDetailActivity)
                    .load(travel.imagesURL[0])
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_error)
                    .into(image)

            travel.imagesURL
                .filterIndexed { i: Int, _ -> i != 0 }
                .map { image ->
                    TextSliderView(this@TravelDetailActivity)
                    .image(image)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                }
                .forEach { sliderView -> imageSlider.addSlider(sliderView) }

            imageSlider.setPresetTransformer(SliderLayout.Transformer.Accordion)
            imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
            imageSlider.setDuration(4000)

            actionButton.setOnClickListener { vm.onActionButton(travel.id, buyEnabled) }

            vm.actionButtonText.observe(this@TravelDetailActivity) {
                actionButton.text = it
            }
        }

        vm.toastMessage.observe(this) {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        binding.imageSlider.stopAutoCycle()
        super.onDestroy()
    }
}

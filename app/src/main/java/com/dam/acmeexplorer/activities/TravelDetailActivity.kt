package com.dam.acmeexplorer.activities

import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daimajia.slider.library.Animations.DescriptionAnimation
import com.daimajia.slider.library.SliderLayout
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.TextSliderView
import com.dam.acmeexplorer.R
import com.dam.acmeexplorer.databinding.ActivityTravelDetailBinding
import com.dam.acmeexplorer.extensions.formatted
import com.dam.acmeexplorer.extensions.showMessage
import com.dam.acmeexplorer.extensions.tryRequestLocationUpdates
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.utils.Units
import com.dam.acmeexplorer.viewmodels.TravelDetailViewModel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class TravelDetailActivity : AppCompatActivity() {

    private val vm: TravelDetailViewModel by viewModel()
    private lateinit var binding: ActivityTravelDetailBinding
    private lateinit var locationServices: FusedLocationProviderClient

    companion object {
        const val INTENT_TRAVEL = "TRAVEL"
        const val INTENT_BUY = "BUY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationServices = LocationServices.getFusedLocationProviderClient(this)

        val travel = intent.getParcelableExtra<Travel>(INTENT_TRAVEL)
        val buyEnabled = intent.getBooleanExtra(INTENT_BUY, false)

        vm.setTravelLocation(travel.weather.coords.latitude, travel.weather.coords.longitude)
        vm.updateSelectButton(this, travel.id, buyEnabled)

        binding = ActivityTravelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            title.text = travel.title
            startDate.text = getString(R.string.start_date, travel.startDate.formatted())
            endDate.text = getString(R.string.end_date, travel.endDate.formatted())
            price.text = getString(R.string.price, travel.price)
            startPlace.text = getString(R.string.start_place, travel.startPlace)

            temperature.text = getString(R.string.temperatureText, travel.weather.main.temp + Units.KELVIN_TO_CELSIUS)
            humidity.text = getString(R.string.humidityText, travel.weather.main.humidity)
            windSpeed.text = getString(R.string.windSpeedText, travel.weather.wind.speed * Units.MS_TO_KMH)
            pressure.text = getString(R.string.pressureText, travel.weather.main.pressure * Units.HPA_TO_BAR)

            Picasso.with(this@TravelDetailActivity)
                    .load(travel.imagesURL[0])
                    .resize(300, 300)
                    .centerCrop()
                    .placeholder(R.drawable.ic_loading)
                    .error(R.drawable.ic_error)
                    .into(image)

            travel.imagesURL
                .filterIndexed { i: Int, _ -> i > 0 }
                .map { image ->
                    TextSliderView(this@TravelDetailActivity)
                    .image(image)
                    .setScaleType(BaseSliderView.ScaleType.Fit)
                }
                .forEach { sliderView -> imageSlider.addSlider(sliderView) }

            imageSlider.setPresetTransformer(SliderLayout.Transformer.Accordion)
            imageSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
            imageSlider.setDuration(4000)

            actionButton.setOnClickListener {
                vm.onActionButton(this@TravelDetailActivity, travel.id, buyEnabled)
            }

            vm.actionButtonText.observe(this@TravelDetailActivity) {
                actionButton.text = it
            }

            vm.distance.observe(this@TravelDetailActivity) {
                distance.text = getString(R.string.distanceText, if(it < Units.MIN_DISTANCE) getString(R.string.loading) else DecimalFormat("#.##").format(it))
            }
        }

        vm.toastMessage.observe(this) {
            showMessage(it)
        }
    }

    override fun onDestroy() {
        binding.imageSlider.stopAutoCycle()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        vm.startLocation(locationServices)
    }

    override fun onPause() {
        super.onPause()
        vm.stopLocation(locationServices)
    }
}

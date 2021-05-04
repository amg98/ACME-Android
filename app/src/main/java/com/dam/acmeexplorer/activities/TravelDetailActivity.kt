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
import com.dam.acmeexplorer.models.Travel
import com.dam.acmeexplorer.viewmodels.TravelDetailViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import org.koin.android.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class TravelDetailActivity : AppCompatActivity() {

    private val vm: TravelDetailViewModel by viewModel()
    private lateinit var binding: ActivityTravelDetailBinding
    private lateinit var travelLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTravelDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val travel = intent.getParcelableExtra<Travel>("TRAVEL")
        val buyEnabled = intent.getBooleanExtra("BUY", false)

        travelLocation = Location("")
        travelLocation.latitude = travel.weather.coords.latitude
        travelLocation.longitude = travel.weather.coords.longitude

        vm.updateSelectButton(travel.id, buyEnabled)

        getLocation()

        with(binding) {
            val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            title.text = travel.title
            startDate.text = getString(R.string.start_date, dateFormatter.format(travel.startDate))
            endDate.text = getString(R.string.end_date, dateFormatter.format(travel.endDate))
            price.text = getString(R.string.price, travel.price)
            startPlace.text = getString(R.string.start_place, travel.startPlace)

            temperature.text = getString(R.string.temperatureText, travel.weather.main.temp - 273.15)
            humidity.text = getString(R.string.humidityText, travel.weather.main.humidity)
            windSpeed.text = getString(R.string.windSpeedText, travel.weather.wind.speed * 3.6)
            pressure.text = getString(R.string.pressureText, travel.weather.main.pressure * 0.001)
            distance.text = getString(R.string.distanceText, 0.0f)

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

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            val location = locationResult.lastLocation

            with(binding) {
                distance.text = getString(R.string.distanceText, location.distanceTo(travelLocation) / 1000.0f)
            }
        }
    }

    private fun getLocation() {
        try {
            val req = LocationRequest.create()
            req.interval = 5000
            req.priority = LocationRequest.PRIORITY_LOW_POWER
            req.smallestDisplacement = 5.0f

            val locationServices = LocationServices.getFusedLocationProviderClient(this)
            locationServices.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())

        } catch (e: SecurityException) {
            // TODO
        }
    }
}

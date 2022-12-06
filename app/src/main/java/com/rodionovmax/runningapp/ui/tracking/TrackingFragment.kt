package com.rodionovmax.runningapp.ui.tracking

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.rodionovmax.runningapp.R
import com.rodionovmax.runningapp.databinding.FragmentTrackingBinding
import com.rodionovmax.runningapp.db.RunEntity
import com.rodionovmax.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.rodionovmax.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.rodionovmax.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.rodionovmax.runningapp.other.Constants.MAP_ZOOM
import com.rodionovmax.runningapp.other.Constants.POLYLINE_COLOR
import com.rodionovmax.runningapp.other.Constants.POLYLINE_WIDTH
import com.rodionovmax.runningapp.other.TrackingUtility
import com.rodionovmax.runningapp.services.Polyline
import com.rodionovmax.runningapp.services.TrackingService
import com.rodionovmax.runningapp.ui.CancelTrackingDialog
import com.rodionovmax.runningapp.ui.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "Cancel Dialog"

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var map: GoogleMap? = null

    private var curTimeInMillis = 0L

    private lateinit var myMenu: Menu

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.toolbar_menu, menu)
                myMenu = menu
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                if (curTimeInMillis > 0L) {
                    myMenu.getItem(0)?.isVisible = true
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.mi_cancel_tracking -> {
                        showCancelTrackingDialog()
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }
        // cancel dialog with screen rotation
        if (savedInstanceState != null) {
            val cancelTrackingDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }
        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis, true)
            binding.tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            myMenu.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && curTimeInMillis > 0L) {
            binding.btnToggleRun.text = "Start"
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            binding.btnToggleRun.text = "Stop"
            myMenu.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = RunEntity(
                bmp,
                dateTimeStamp,
                avgSpeed,
                distanceInMeters,
                curTimeInMillis,
                caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    /*override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }*/

}
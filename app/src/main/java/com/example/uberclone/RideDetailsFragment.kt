package com.example.uberclone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.Toast
import com.example.uberclone.databinding.FragmentRideDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class RideDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentRideDetailsBinding? = null
    private val binding get() = _binding!!

    // Create an instance of RideDetailsFragment with the provided data.
    companion object {
        fun newInstance(pickup: String, drop: String, fare: String): RideDetailsFragment {
            val fragment = RideDetailsFragment()
            val args = Bundle().apply {
                putString("pickup", pickup)
                putString("drop", drop)
                putString("fare", fare)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRideDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Populate the UI with ride details.
        // In a production app, you can pass these as arguments or via a shared ViewModel.
        val pickup = arguments?.getString("pickup") ?: "Current Location"
        val drop = arguments?.getString("drop") ?: "Drop Location"
        val fare = arguments?.getString("fare") ?: "12.34"

        binding.textViewTitle.text = "Confirm Your Ride"
        binding.textViewPickup.text = "Pickup: $pickup"
        binding.textViewDrop.text = "Drop: $drop"
        binding.textViewFare.text = "Estimated Fare: ₹ $fare"

        // Simulate booking action.
        binding.buttonBookCab.setOnClickListener {
            // Insert real booking logic here.
            dismiss()
        }
        // Cancel the booking.
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
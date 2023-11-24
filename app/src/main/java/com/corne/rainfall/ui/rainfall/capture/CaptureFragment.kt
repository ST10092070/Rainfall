package com.corne.rainfall.ui.rainfall.capture

import LocationListItemAdapter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.corne.rainfall.databinding.FragmentCaptureBinding
import com.corne.rainfall.ui.base.state.BaseStateFragment
import com.corne.rainfall.ui.hiltMainNavGraphViewModels
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CaptureFragment :
    BaseStateFragment<FragmentCaptureBinding, CaptureState, CaptureViewModel>() {
    override val viewModel: CaptureViewModel by hiltMainNavGraphViewModels()
    private var count = 0
    private val calendar: Calendar = Calendar.getInstance()
    private lateinit var locationAdapter: LocationListItemAdapter
    private var firstRun = true


    override fun updateState(state: CaptureState) {
        count++
        println("We have set the state to loading$count")

        binding.saveBtn.isEnabled = viewModel.isFormValid()

        viewModel.setOnSuccessCallback {
            findNavController().popBackStack()
            Toast.makeText(requireContext(), "Successfully added Rain Log!", Toast.LENGTH_SHORT).show()
        }

        viewModel.setOnFailCallback {
            Toast.makeText(requireContext(), "Something went wrong. ${viewModel.state.value.error}", Toast.LENGTH_SHORT).show()
        }

        state.formValues.forEach {
            val value = it.value
            when (it.key) {
                CaptureForm.DATE -> showError(
                    value.errorMessage, binding.dateInput.binding.valueInputLayout
                )

                CaptureForm.START_TIME -> showError(
                    value.errorMessage, binding.startTimeInput.binding.valueInputLayout
                )

                CaptureForm.END_TIME -> showError(
                    value.errorMessage, binding.endTimeInput.binding.valueInputLayout
                )

                CaptureForm.RAIN_MM -> showError(
                    value.errorMessage, binding.rainMmInput.binding.valueInputLayout
                )

                CaptureForm.NOTES -> showError(
                    value.errorMessage, binding.rainNotes.binding.valueInputLayout
                )
            }
        }

        binding.dateInput.binding.value.inputType = 0
        binding.dateInput.binding.value.isFocusable = false
        binding.dateInput.binding.value.setOnClickListener { showDatePicker() }

        binding.startTimeInput.binding.value.inputType = 0
        binding.startTimeInput.binding.value.isFocusable = false
        binding.startTimeInput.binding.value.setOnClickListener { showTimePicker(binding.startTimeInput.binding.value) }

        binding.endTimeInput.binding.value.inputType = 0
        binding.endTimeInput.binding.value.isFocusable = false
        binding.endTimeInput.binding.value.setOnClickListener { showTimePicker(binding.endTimeInput.binding.value) }

        //Spinner
        if (state.allLocationsList.isNotEmpty()) {
//            val adapter = ArrayAdapter(requireContext(),
//                android.R.layout.simple_spinner_item,
//                state.allLocationsList.map { it.name })

            val adapter = LocationListItemAdapter(requireContext(), state.allLocationsList.map { it.name })

            binding.locationSpinner.adapter = adapter

            fun onItemSelectedListener() = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    //Set one to user for rainfall saving?
//                    viewModel.saveDefaultLocation(state.allLocationsList[position].locationUID)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

            binding.locationSpinner.onItemSelectedListener = onItemSelectedListener()

            if (state.defaultLocation != null) {
                state.allLocationsList.find { it.locationUID == state.defaultLocation!! }?.let {
                    binding.locationSpinner.onItemSelectedListener = null
                    var add = 1
                    if (firstRun) {
                        add = 0
                        firstRun = false
                    }
                    binding.locationSpinner.setSelection(adapter.getPosition(it.name) + add)
                    binding.locationSpinner.onItemSelectedListener = onItemSelectedListener()

                }
            }
        }

    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            //set text of textinput
            val format = "dd/mm/yyyy"
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            binding.dateInput.binding.value.setText(sdf.format(calendar.time))
        }

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            datePicker,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the maximum date selectable to today's date
        val datePicker1 = datePickerDialog.datePicker
        datePicker1.maxDate = System.currentTimeMillis()

        // Show the date picker dialog
        datePickerDialog.show()
    }

    private fun showTimePicker(value: TextInputEditText) {
        val timePicker = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                // when user sets the time in the time picker dialog, update the calendar instance
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                // update the label text with the selected time
                val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeString = sdf.format(Date(calendar.timeInMillis))
                // update the button text with the selected time
                value.setText(timeString)
            },
            // set the initial time in the time picker dialog to the current time
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            // set the time picker dialog to display in 24-hour format
            true
        )
        timePicker.show()
    }

    private fun showError(errorMessage: Int?, valueInputLayout: TextInputLayout) {
        errorMessage?.let { errorMsg ->
            valueInputLayout.error = requireContext().getString(errorMsg)
        } ?: run {
            valueInputLayout.error = null
        }
    }


    fun showProgressLoader(show: Boolean) {
        if (show) {

        } else {

        }
    }

    override suspend fun addContentToView() {
        viewModel.setUpForm()
        viewModel.loadUserLocationData()

        binding.dateInput.binding.value.doAfterTextChanged {
            viewModel.updateState(CaptureForm.DATE, it.toString())
        }
        binding.startTimeInput.binding.value.doAfterTextChanged {
            viewModel.updateState(CaptureForm.START_TIME, it.toString())
        }
        binding.endTimeInput.binding.value.doAfterTextChanged {
            viewModel.updateState(CaptureForm.END_TIME, it.toString())
        }
        binding.rainMmInput.binding.value.doAfterTextChanged {
            viewModel.updateState(CaptureForm.RAIN_MM, it.toString())
        }
        binding.rainNotes.binding.value.doAfterTextChanged {
            viewModel.updateState(CaptureForm.NOTES, it.toString())
        }
        binding.saveBtn.setOnClickListener { viewModel.add() }


    }



    override fun createViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentCaptureBinding = FragmentCaptureBinding.inflate(inflater, container, false)
}

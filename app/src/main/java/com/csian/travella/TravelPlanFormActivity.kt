package com.csian.travella

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TravelPlanFormActivity : AppCompatActivity() {

    private lateinit var locationDropDown: Spinner

    //Buttons
    private lateinit var startDatePicker: Button
    private lateinit var endDatePicker: Button
    private lateinit var startTimePicker: Button
    private lateinit var endTimePicker: Button
    private lateinit var nextButton: Button


    // Values
    private var locationValue: String? = "Select Location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_travel_plan_form)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }


    private fun init(){
        getViews()
        initViews()
        initListeners()
    }

    private fun getViews(){
        locationDropDown = findViewById(R.id.location_dropdown)
        startDatePicker = findViewById(R.id.start_date_picker)
        endDatePicker = findViewById(R.id.end_date_picker)
        startTimePicker = findViewById(R.id.start_time_picker)
        endTimePicker = findViewById(R.id.end_time_picker)
        nextButton = findViewById(R.id.next_button)
    }

    private fun initViews(){
        ArrayAdapter.createFromResource(
            this,
            R.array.malaysia_states_and_territories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationDropDown.adapter = adapter
        }

        startDatePicker.text = "Start Date"
        endDatePicker.text = "End Date"
        startTimePicker.text = "Start Time"
        endTimePicker.text = "End Time"
    }

    private fun initListeners(){
        locationDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                locationValue = selectedItem
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }


        startDatePicker.setOnClickListener {
            showDatePickerDialog(startDatePicker)
        }

        endDatePicker.setOnClickListener {
            showDatePickerDialog(endDatePicker)
        }

        startTimePicker.setOnClickListener {
            showTimePickerDialog(startTimePicker)
        }

        endTimePicker.setOnClickListener {
            showTimePickerDialog(endTimePicker)
        }

        nextButton.setOnClickListener {
            submitForm()
        }

    }

    private fun showDatePickerDialog(picker: Button) {
        // Get the current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(this,
            { view, year, monthOfYear, dayOfMonth ->
                // Handle the date selection
                val selectedDate = "${dayOfMonth}/${monthOfYear + 1}/${year}"
                // Display the selected date on the button
                picker.text = selectedDate
                // Optionally show a toast with the selected date
                Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
            }, year, month, day)

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(picker: Button) {
        // Get the current time
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        // Create a TimePickerDialog
        val timePickerDialog = TimePickerDialog(this,
            { view, hourOfDay, minute ->
                // Handle the time selection
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                // Display the selected time on the button
                picker.text = selectedTime
                // Optionally show a toast with the selected time
                Toast.makeText(this, "Selected Time: $selectedTime", Toast.LENGTH_SHORT).show()
            }, hour, minute, true)

        // Show the TimePickerDialog
        timePickerDialog.show()
    }


    private fun inputValidation(): Boolean {
        val location = locationValue
        val startDateValue = startDatePicker.text.toString()
        val endDateValue = endDatePicker.text.toString()
        val startTimeValue = startTimePicker.text.toString()
        val endTimeValue = endTimePicker.text.toString()

        if (location == "Select Location") {
            Toast.makeText(this, "Please select a location.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (startDateValue == "Start Date" || endDateValue == "End Date") {
            Toast.makeText(this, "Please select both start and end dates.", Toast.LENGTH_SHORT).show()
            return false
        }

        if (startTimeValue == "Start Time" || endTimeValue == "End Time") {
            Toast.makeText(this, "Please select both start and end times.", Toast.LENGTH_SHORT).show()
            return false
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val todayDate = dateFormat.format(calendar.time)
        val todayTime = timeFormat.format(calendar.time)

        // Check if start date and start time are valid
        val startDate = dateFormat.parse(startDateValue) ?: return false
        val startTime = timeFormat.parse(startTimeValue) ?: return false
        calendar.time = startDate
        calendar.set(Calendar.HOUR_OF_DAY, startTime.hours)
        calendar.set(Calendar.MINUTE, startTime.minutes)
        val startDateTime = calendar.time

        val todayDateTime = dateFormat.parse(todayDate) ?: return false
        calendar.time = todayDateTime
        val todayDateTimeWithCurrentTime = calendar.time
        calendar.time = todayDateTimeWithCurrentTime
        calendar.set(Calendar.HOUR_OF_DAY, startTime.hours)
        calendar.set(Calendar.MINUTE, startTime.minutes)
        val todayDateTimeWithCurrentTimeAndTime = calendar.time

        if (startDateTime.before(todayDateTimeWithCurrentTimeAndTime)) {
            Toast.makeText(this, "Start date and time must be greater than or equal to the current date and time.", Toast.LENGTH_SHORT).show()
            return false
        }

        // Check if end date and end time are valid
        val endDate = dateFormat.parse(endDateValue) ?: return false
        val endTime = timeFormat.parse(endTimeValue) ?: return false
        calendar.time = endDate
        calendar.set(Calendar.HOUR_OF_DAY, endTime.hours)
        calendar.set(Calendar.MINUTE, endTime.minutes)
        val endDateTime = calendar.time

        if (endDateTime.before(startDateTime)) {
            Toast.makeText(this, "End date and time must be greater than or equal to start date and time.", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun submitForm(): Boolean {
        if (!inputValidation()) return false

        val location = locationValue
        val startDateValue = startDatePicker.text.toString()
        val endDateValue = endDatePicker.text.toString()
        val startTimeValue = startTimePicker.text.toString()
        val endTimeValue = endTimePicker.text.toString()

        val intent = Intent(this, MushTryActivity::class.java).apply {
            putExtra("location", location)
            putExtra("startDate", startDateValue)
            putExtra("endDate", endDateValue)
            putExtra("startTime", startTimeValue)
            putExtra("endTime", endTimeValue)
        }

        startActivity(intent)
        finish()
        return true
    }

}
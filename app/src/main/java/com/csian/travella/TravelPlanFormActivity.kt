package com.csian.travella

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

class TravelPlanFormActivity : AppCompatActivity() {

    private lateinit var locationDropDown: Spinner

    //Buttons
    private lateinit var startDatePicker: Button
    private lateinit var endDatePicker: Button
    private lateinit var startTimePicker: Button
    private lateinit var endTimePicker: Button

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

    }

    private fun getViews(){
        locationDropDown = findViewById(R.id.location_dropdown)
        startDatePicker = findViewById(R.id.start_date_picker)
        endDatePicker = findViewById(R.id.end_date_picker)
        startTimePicker = findViewById(R.id.start_time_picker)
        endTimePicker = findViewById(R.id.end_time_picker)
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


    }

    private fun initListeners(){
        locationDropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Toast.makeText(this@TravelPlanFormActivity, "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }



    }
}
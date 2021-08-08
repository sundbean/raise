package com.sundbean.raise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CreateFundraiserActivity : AppCompatActivity() {

    private lateinit var causesRecyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_fundraiser)

        causesRecyclerView = findViewById(R.id.rvChooseFundraiserCauses)
        val causes = mutableListOf("img_animal_rights_foreground", "img_arts_and_culture_foreground", "img_black_lives_matter_foreground", "img_climate_change_foreground", "img_conservation_foreground", "img_education_foreground", "img_food_access_foreground", "img_homelessness_poverty_foreground", "img_lgbt_rights_foreground", "img_mental_health_foreground", "img_political_reform_foreground", "img_prison_reform_foreground", "img_refugee_rights_foreground", "img_water_sanitation_foreground", "img_womens_rights_foreground")
        GridLayoutManager(
            this.baseContext,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            causesRecyclerView.layoutManager = this
        }

        causesRecyclerView.adapter = RecyclerViewAdapter(this, causes)

        // fixes uneven scrolling issue
        causesRecyclerView.setNestedScrollingEnabled(false)
    }

}
package com.sundbean.raise

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChooseCausesActivity : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_causes)

        recyclerView = findViewById(R.id.rvChooseInterests)
        //initialize a mutable list of images
        // val causes = mutableListOf("@drawable/img_animal_rights_background", "@drawable/img_arts_and_culture_background", "@drawable/img_black_lives_matter_background", "@drawable/img_climate_change_background", "@drawable/img_conservation_background", "@drawable/img_education_background", "@drawable/img_food_access_background", "@drawable/img_homelessness_poverty_background", "@drawable/img_lgbt_rights_background", "@drawable/img_mental_health_background", "@drawable/img_political_reform_background", "@drawable/img_prison_reform_background", "@drawable/img_refugee_rights_background", "@drawable/img_water_sanitation_background", "@drawable/img_water_sanitation_background")
        val causes = mutableListOf("img_animal_rights_foreground", "img_arts_and_culture_foreground", "img_black_lives_matter_foreground", "img_climate_change_foreground", "img_conservation_foreground", "img_education_foreground", "img_food_access_foreground", "img_homelessness_poverty_foreground", "img_lgbt_rights_foreground", "img_mental_health_foreground", "img_political_reform_foreground", "img_prison_reform_foreground", "img_refugee_rights_foreground", "img_water_sanitation_foreground", "img_womens_rights_foreground")
        //initialize grid layout manager
        GridLayoutManager(
            this,
            3,
            RecyclerView.VERTICAL,
            false
        ).apply {
            //specify the layout manager for recycler view
            recyclerView.layoutManager = this
        }

        // finally, data bind the recycler view with adapter
        recyclerView.adapter = RecyclerViewAdapter(this, causes)
    }
}
package com.sundbean.raise

import ExploreFragment
import MainFeedFragment
import NotificationsFragment
import OrganizeFragment
import ProfileFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val mainFeedFragment=MainFeedFragment()
        val exploreFragment=ExploreFragment()
        val organizeFragment=OrganizeFragment()
        val notificationsFragment=NotificationsFragment()
        val profileFragment=ProfileFragment()

        setCurrentFragment(mainFeedFragment)

        // adds click listener to items of Bottom Navigation Bar so we display corresponding Fragment when an item is clicked
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.feed->setCurrentFragment(mainFeedFragment)
                R.id.explore->setCurrentFragment(exploreFragment)
                R.id.organize->setCurrentFragment(organizeFragment)
                R.id.notifications->setCurrentFragment(notificationsFragment)
                R.id.profile->setCurrentFragment(profileFragment)
            }
            true
        }
    }

    // sets provided fragment in our FrameLayout of activity_main.xml
    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}
package com.example.tvapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tvapp.model.Movie

class DetailsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fragment = VideoDetailsFragment()
        fragment.arguments = Bundle().apply {
            putParcelable("movie", intent.getParcelableExtra<Movie>("movie"))
        }
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, fragment)
            .commit()
    }
}

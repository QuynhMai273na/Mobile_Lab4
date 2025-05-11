package com.example.tvapp

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.tvapp.model.Movie

class VideoDetailsFragment : DetailsSupportFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val movie = arguments?.getParcelable<Movie>("movie") ?: return

        val detailsRow = DetailsOverviewRow(movie)
        val presenter = FullWidthDetailsOverviewRowPresenter(CustomDetailsDescriptionPresenter())

        Glide.with(requireContext())
            .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    detailsRow.imageDrawable = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

        val rowsAdapter = ArrayObjectAdapter(presenter).apply {
            add(detailsRow)
        }

        // Fake seasons
        val seasonAdapter = ArrayObjectAdapter(CardPresenter())
        for (i in 1..4) {
            seasonAdapter.add(
                Movie(
                    id = movie.id,
                    title = "${movie.title} SEASON $i",
                    overview = movie.overview,
                    posterPath = movie.posterPath
                )
            )
        }
        rowsAdapter.add(ListRow(HeaderItem(1, "Seasons"), seasonAdapter))

        adapter = rowsAdapter
    }
}

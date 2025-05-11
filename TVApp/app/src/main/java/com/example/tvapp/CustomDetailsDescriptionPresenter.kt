package com.example.tvapp

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import com.example.tvapp.model.Movie

class CustomDetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(vh: ViewHolder?, item: Any?) {
        val movie = item as Movie
        vh?.title?.text = movie.title
        vh?.subtitle?.text = "Netflix"
        vh?.body?.text = movie.overview
    }
}

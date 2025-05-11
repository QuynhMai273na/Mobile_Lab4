// MainFragment.kt
package com.example.tvapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.example.tvapp.model.Movie
import com.example.tvapp.network.ApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainFragment : BrowseSupportFragment() {
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        title = "Quynh Mai's  Movies"
        headersState = HEADERS_ENABLED
        brandColor = ContextCompat.getColor(requireContext(), R.color.purple_500)

        setupListeners()
        loadMovies()
    }

    private fun setupListeners() {
        setOnItemViewClickedListener { _, item, _, _ ->
            val intent = Intent(requireContext(), DetailsActivity::class.java)
            intent.putExtra("movie", item as Movie)
            startActivity(intent)
        }
    }

    private fun loadMovies() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.api
                val apiKey = "683940b4e8acc2732223abeaf08fa155"

                val (seriesList, newMovies, oldMovies) = awaitAll(
                    async { api.getTopRatedMovies(apiKey).results },
                    async { api.getNowPlayingMovies(apiKey).results },
                    async { api.getPopularMovies(apiKey).results }
                )

                val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
                val newMoviesDistinct = newMovies.distinctBy { it.id }
                val oldMoviesDistinct = oldMovies.distinctBy { it.id }
                    .filter { old -> newMoviesDistinct.none { it.id == old.id }}

                val sectionMap = mapOf(
                    "Series" to seriesList.distinctBy { it.id },
                    "New Movies" to newMoviesDistinct,
                    "Old Movies" to oldMoviesDistinct
                )

                sectionMap.entries.forEachIndexed { index, entry ->
                    val rowAdapter = ArrayObjectAdapter(CardPresenter())
                    entry.value.forEach { rowAdapter.add(it) }
                    rowsAdapter.add(ListRow(HeaderItem(index.toLong(), entry.key), rowAdapter))
                }

                adapter = rowsAdapter
            } catch (e: Exception) {
                Log.e("TVAPP", "Failed to load movies", e)
            }
        }
    }
}

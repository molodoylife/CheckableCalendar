package ru.narod.pricolistov.gridviewexample

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PreCachingLayoutManager(context: Context) : LinearLayoutManager(context) {
    private val defaultExtraLayoutSpace = 600
    private var extraLayoutSpace = -1

    override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
        return if (extraLayoutSpace > 0) {
            extraLayoutSpace
        } else defaultExtraLayoutSpace
    }
}
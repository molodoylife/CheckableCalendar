package ru.narod.pricolistov.drugcalendar

import android.app.Activity
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class MyAdapter(private val ctx: Activity, private val dataList: Array<String>) :
    RecyclerView.Adapter<MyAdapter.GridViewHolder>() {

    val calendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())


    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return GridViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        (holder.myItemView as DrugCalendarView).setDate(dataList[position])
    }

    override fun getItemId(position: Int): Long {
        return dataList[position].hashCode().toLong()
    }

    inner class GridViewHolder(val myItemView: View) : RecyclerView.ViewHolder(myItemView)
}
package ru.narod.pricolistov.drugcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class MyAdapter(private val dateList: Array<String>, private val data: List<Array<DateState>>) :
    RecyclerView.Adapter<MyAdapter.GridViewHolder>() {


    override fun getItemCount(): Int {
        return dateList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return GridViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        (holder.myItemView as DrugCalendarView).setDateAndData(dateList[position], data[position])
    }

    override fun getItemId(position: Int): Long {
        return dateList[position].hashCode().toLong()
    }

    inner class GridViewHolder(val myItemView: View) : RecyclerView.ViewHolder(myItemView)
}
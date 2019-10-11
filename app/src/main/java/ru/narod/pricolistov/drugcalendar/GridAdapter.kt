package ru.narod.pricolistov.drugcalendar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class MyAdapter(private val dataList: Array<String>, private val data: Array<DateState>) :
    RecyclerView.Adapter<MyAdapter.GridViewHolder>() {


    override fun getItemCount(): Int {
        return dataList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)

        return GridViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
        (holder.myItemView as DrugCalendarView).setDateAndData(dataList[position], data)
    }

    inner class GridViewHolder(val myItemView: View) : RecyclerView.ViewHolder(myItemView)
}
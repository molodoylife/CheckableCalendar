package ru.narod.pricolistov.drugcalendar

import android.graphics.PointF

data class DateSquare(val position: PointF, val date: Int = 0, val isActive: Boolean = true,
                      val isSelected: Boolean = false)

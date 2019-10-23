package ru.narod.pricolistov.drugcalendar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min


//TODO refactor me
class DrugCalendarView : View {

    companion object {
        const val GRAY = Color.GRAY
        const val GREEN = Color.GREEN
        const val WHITE = Color.WHITE
        const val BLACK = Color.BLACK

        const val CIRCLE_STROKE_WIDTH = 3f
        const val TEXT_STROKE_WIDTH = 3f
        const val TEXT_SIZE_DEFAULT = 16f
        const val ANIMATION_RIPPLE_DURATION = 240L


        val ALIGN_CENTER = Paint.Align.CENTER
        val ALIGN_LEFT = Paint.Align.LEFT
        val SHADOW_COLOR = Color.parseColor("#80e0e0e0")

        const val WEEK_NAME_FORMAT = "EEEEE"
        const val RECEIVED_MONTH_DEFAULT = "dd.MM.yy"
        const val DISPLAYED_MONTH_AND_YEAR = "MMMM yyyy"
    }

    private val calendar = Calendar.getInstance()
    private val locale = Locale.getDefault()
    private val dateFormat = SimpleDateFormat(RECEIVED_MONTH_DEFAULT, locale)

    private var numberOfDaysInCurrentMonth = 0
    private var dateFormatMonthTitle = ""

    /**
     * Screen params
     * */
    private val screenWidth = getCurrentScreenWidth()
    private var squareSize = 0

    /**
     *This view sizes
     * */
    private var mWidth = 0
    private var mHeight = 0

    /**
     * Distance to center of square
     * */
    private var initOffset = 0

    /**
     * Should be set form xml by selectionRadiusMultiplier params
     * */
    private var initSquareSelectionRadiusMulitplier = 0.75f


    /**
     * Will be calculated later after obtaining multiplier
     * */
    private var initSquareSelectionRadius = 10f

    /**
     * Get and store localized names for days in week
     * */
    private val daysInWeekNames = getDayNamesForWeekArray()
    /**
     * Get localized first day in week (it can be different for different countries)
     * */
    private val positionOfFirstDayInWeekInCurrentLocale = calendar.firstDayOfWeek

    /**
     * Vars and default values for date appearance
     * */
    private var selectionColor = GREEN
    private var selectedRippleColor = GREEN
    private var rippleColor = GRAY
    private var radiusRippleEffect = 0f

    /**
     * Variables for managing selecting and canceling dates with animations
     * */
    private var datePressedNow: DateSquare? = null
    private var ifFingerTouches = false

    /**
     * Storing selectedDates in set of [DateSquare] objects
     * */
    private val selectedDates: MutableSet<DateSquare> = hashSetOf()

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = TEXT_STROKE_WIDTH
        color = BLACK
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            TEXT_SIZE_DEFAULT, resources.displayMetrics
        )
        textAlign = ALIGN_CENTER
    }

    /**
     * Values for drawing text exactly in the middle of the square
     * */
    private val textVerticalOffsetToBeDrawnInCenter = (textPaint.descent() + textPaint.ascent()) / 2
    private var yPositionForTextWithOffset = 0f

    private val textPaintMonthName = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = TEXT_STROKE_WIDTH
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = BLACK
        textAlign = ALIGN_LEFT
        textSize = textPaint.textSize
    }

    private val emptyCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = GREEN
    }

    private val shadowCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = SHADOW_COLOR
    }

    private val activeCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = WHITE
    }

    private val filledCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = CIRCLE_STROKE_WIDTH
        color = GRAY
    }


    /**
     * Developer is responsible for call [setDateAndData] to set date and data
     * */
    private var date: String? = null
    private var data: List<DateState>? = null

    /**
     * Storing coordinates for names of days in week
     * */
    private var daysInWeekNamesPositions: List<PointF>? = null

    /**
     * Storing coordinates and data for dates in [DateSquare] class
     * */
    private var datePositionsWithData: List<DateSquare>? = null


    var onElementSelectListener: OnElementSelectedListener? = null


    private var widthMeasureSpec: Int = 0
    private var heightMeasureSpec: Int = 0

    private fun initComputation(attrs: AttributeSet?) {

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.DrugCalendarView
            )

            val obtainedTextSize =
                typedArray.getDimension(R.styleable.DrugCalendarView_textSize, 0f)

            textPaint.textSize =
                if (obtainedTextSize > 0) obtainedTextSize else TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_DEFAULT, resources.displayMetrics
                )

            textPaint.color =
                typedArray.getColor(R.styleable.DrugCalendarView_textColor, BLACK)

            selectionColor = typedArray.getColor(R.styleable.DrugCalendarView_selectedColor, GREEN)
            selectedRippleColor =
                typedArray.getColor(R.styleable.DrugCalendarView_selectedRippleColor, GREEN)
            rippleColor = typedArray.getColor(R.styleable.DrugCalendarView_rippleColor, GRAY)

            emptyCirclePaint.color = selectionColor
            filledCirclePaint.color = rippleColor


            initSquareSelectionRadiusMulitplier =
                typedArray.getFloat(R.styleable.DrugCalendarView_selectionRadiusMultiplier, 0.75f)


            typedArray.recycle()
        }
    }

    private fun startAnimationRipple(isElementSelected: Boolean) {
        filledCirclePaint.color = if (isElementSelected) selectedRippleColor else rippleColor
        val animator = ValueAnimator.ofInt(0, initSquareSelectionRadius.toInt()).apply {
            duration = ANIMATION_RIPPLE_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { valueAnimator ->
                radiusRippleEffect = (valueAnimator.animatedValue as Int).toFloat()
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    if (!ifFingerTouches) {
                        radiusRippleEffect = 0f
                        addOrRemoveDate()
                    }
                }
            })
        }
        animator.start()
    }

    private fun addOrRemoveDate() {
        datePressedNow?.let {
            val selectedDate = "${it.date}." + date!!.substring(3)
            if (!selectedDates.contains(it)) {
                selectedDates.add(it)
                onElementSelectListener?.let { listener ->
                    listener.onElementSelect(true, selectedDate)

                }
            } else {
                selectedDates.remove(it)
                onElementSelectListener?.let { listener ->
                    listener.onElementSelect(false, selectedDate)
                }
            }
            datePressedNow = null
            invalidate()
        }
    }

    private fun drawDates(canvas: Canvas) {
        datePositionsWithData?.let {
            for (date in it) {

                var paintForCircles: Paint
                if (!date.isActive) {
                    paintForCircles = shadowCirclePaint

                } else {
                    paintForCircles = activeCirclePaint
                    canvas.drawCircle(
                        date.position.x + 2, date.position.y + 3,
                        initSquareSelectionRadius - 3, shadowCirclePaint
                    )
                }

                canvas.drawCircle(
                    date.position.x, date.position.y,
                    initSquareSelectionRadius - 3, paintForCircles
                )

                canvas.drawText(
                    "${date.date}",
                    date.position.x,
                    date.position.y - textVerticalOffsetToBeDrawnInCenter,
                    textPaint
                )
            }
        }
    }

    private fun drawAnimRipple(canvas: Canvas) {
        datePressedNow?.let {
            canvas.drawCircle(it.position.x, it.position.y, radiusRippleEffect, filledCirclePaint)

            canvas.drawText(
                "${it.date}",
                it.position.x,
                it.position.y - textVerticalOffsetToBeDrawnInCenter,
                textPaint
            )
        }
    }

    private fun drawMonthAndYearTitle(canvas: Canvas) {
        textPaintMonthName.textAlign = Paint.Align.LEFT
        canvas.drawText(
            dateFormatMonthTitle,
            initOffset.toFloat() / 2,
            yPositionForTextWithOffset,
            textPaintMonthName
        )
    }

    private fun drawNumberOfSelectedElements(canvas: Canvas) {
        textPaintMonthName.textAlign = ALIGN_CENTER

        canvas.drawText(
            "${selectedDates.size}/${datePositionsWithData?.filter { it.isActive }?.size}",
            (mWidth - initOffset).toFloat(),
            yPositionForTextWithOffset,
            textPaintMonthName
        )
    }

    private fun drawNamesOfWeekDays(canvas: Canvas) {
        daysInWeekNames?.let {
            for (i in 0..6) {
                canvas.drawText(
                    "${it[(i + positionOfFirstDayInWeekInCurrentLocale) % 7]}",
                    daysInWeekNamesPositions!![i].x,
                    daysInWeekNamesPositions!![i].y - textVerticalOffsetToBeDrawnInCenter,
                    textPaint
                )
            }
        }
    }

    private fun drawSelectedElements(canvas: Canvas) {
        for (selected in selectedDates) {
            canvas.drawCircle(
                selected.position.x,
                selected.position.y,
                initSquareSelectionRadius,
                emptyCirclePaint
            )
        }
    }

    override fun onDraw(canvas: Canvas) {

        drawMonthAndYearTitle(canvas)

        drawNumberOfSelectedElements(canvas)

        drawNamesOfWeekDays(canvas)

        drawDates(canvas)

        drawAnimRipple(canvas)

        drawSelectedElements(canvas)
    }


    fun setDateAndData(date: String, data: List<DateState>?) {
        clearData()
        this.date = date
        this.data = data
        applyDependentSizes()
    }

    private fun clearData() {
        selectedDates.clear()
        this.data = null
        this.date = null
        this.datePositionsWithData = null
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val selectedSquare = getSelectedDateSquare(event.x, event.y)
                selectedSquare?.let {
                    ifFingerTouches = true
                    datePressedNow = it
                    startAnimationRipple(selectedDates.contains(selectedSquare))
                }
            }
            MotionEvent.ACTION_MOVE -> {
                datePressedNow?.let { pressedItem ->
                    if (!isInArea(
                            event.x,
                            event.y,
                            pressedItem.position.x,
                            pressedItem.position.y
                        )
                    ) {
                        datePressedNow = null
                        ifFingerTouches = false
                        invalidate()
                        return false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                ifFingerTouches = false

                datePressedNow?.let { pressedItem ->
                    if (isInArea(
                            event.x,
                            event.y,
                            pressedItem.position.x,
                            pressedItem.position.y
                        )
                    ) {
                        if (radiusRippleEffect + 1 >= initSquareSelectionRadius) {
                            radiusRippleEffect = 0f
                            addOrRemoveDate()
                        }
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                ifFingerTouches = false
                radiusRippleEffect = 0f
                datePressedNow = null
                invalidate()
            }
        }
        return true
    }

    private fun getSelectedDateSquare(x: Float, y: Float): DateSquare? {
        datePositionsWithData?.let {
            for (date in it) {

                //TODO adjust radius and create more productive algorithm for finding selected element
                if (isInArea(x, y, date.position.x, date.position.y)) {
                    return if (date.isActive) DateSquare(
                        date.position,
                        date.date,
                        date.isActive,
                        date.isSelected
                    ) else null
                }
            }
        }

        return null
    }

    private fun isInArea(touchX: Float, touchY: Float, centerX: Float, centerY: Float): Boolean {
        val rect = RectF(
            centerX - initOffset, centerY - initOffset,
            centerX + initOffset, centerY + initOffset
        )
        return rect.contains(touchX, touchY)
    }

    private fun getPositionsForWeekDayNames(): List<PointF> {
        val squareList = arrayListOf<PointF>()

        for (i in 0..7) {
            val x = (initOffset + i * squareSize).toFloat()
            val y = (initOffset).toFloat() + squareSize
            squareList.add(PointF(x, y))
        }

        return squareList
    }

    private fun getPositionsForDates(): List<DateSquare> {
        if (date.isNullOrEmpty()) {
            throw Exception("You must specify the date by calling setDateAndData() method!")
        }

        val squareList = arrayListOf<DateSquare>()

        date?.let {
            calendar.time = dateFormat.parse(it)

            dateFormatMonthTitle =
                SimpleDateFormat(DISPLAYED_MONTH_AND_YEAR, locale).format(calendar.time)

            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val numFirstDayOfWeek = calendar.firstDayOfWeek

            val dayOfWeekForFirstOfCurrentMonth = calendar.get(Calendar.DAY_OF_WEEK)

            val gapOfFirstDayOfMonth = if (dayOfWeekForFirstOfCurrentMonth >= numFirstDayOfWeek)
                calendar.get(Calendar.DAY_OF_WEEK) - numFirstDayOfWeek
            else
                7 - numFirstDayOfWeek

            numberOfDaysInCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)



            for (i in 0 until numberOfDaysInCurrentMonth) {

                var isActive = true
                var isSelected = false

                data?.let { dataArray ->
                    val nextDate = data.takeIf { (dataArray.size - 1) >= i }

                    nextDate?.let {
                        isActive = when (it[i]) {
                            DateState.NORMAL -> true
                            DateState.SELECTED -> true
                            else -> false
                        }

                        isSelected = when (it[i]) {
                            DateState.NORMAL -> false
                            DateState.SELECTED -> true
                            else -> false
                        }
                    }
                }

                //Don't ask what is the logic of computation x and y coordinates
                val x = (initOffset + (gapOfFirstDayOfMonth + i) % 7 * squareSize).toFloat()
                val y =
                    (initOffset + ((i + gapOfFirstDayOfMonth) / 7) * squareSize).toFloat() + squareSize * 2

                val dateForAdding =
                    DateSquare(PointF(x, y), i + 1, isActive = isActive, isSelected = isSelected)

                squareList.add(dateForAdding)

                if (isSelected) {
                    selectedDates.add(dateForAdding)
                }
            }
        }

        return squareList
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.widthMeasureSpec = widthMeasureSpec
        this.heightMeasureSpec = heightMeasureSpec

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        mWidth = widthSize

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)


        val desiredWidth = screenWidth
        var desiredHeight = 100

        if (squareSize == 0) {
            applyDependentSizes()
        }

        datePositionsWithData?.let {
            if (it.isNotEmpty()) {
                desiredHeight = (it[it.size - 1].position.y + squareSize * 0.75).toInt()
            }
        }


        val width: Int
        val height: Int

        //Measure Width
        width = when (widthMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                widthSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                min(desiredWidth, widthSize)
            else -> //Be whatever you want
                desiredWidth
        }

        //Measure Height
        height = when (heightMode) {
            MeasureSpec.EXACTLY -> //Must be this size
                heightSize
            MeasureSpec.AT_MOST -> //Can't be bigger than...
                min(desiredHeight, heightSize)
            else -> //Be whatever you want
                desiredHeight
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height)
    }

    private fun applyDependentSizes() {
        squareSize = mWidth / 7
        initOffset = squareSize / 2
        initSquareSelectionRadius = initOffset * initSquareSelectionRadiusMulitplier
        yPositionForTextWithOffset = initOffset - textVerticalOffsetToBeDrawnInCenter

        daysInWeekNamesPositions = getPositionsForWeekDayNames()
        datePositionsWithData = getPositionsForDates()
    }

    constructor(ctx: Context) : super(ctx) {
        Log.d("", "")
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initComputation(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initComputation(attrs)
    }

    private fun getDayNamesForWeekArray(): Array<String> {
        val formatLetterDay = SimpleDateFormat(WEEK_NAME_FORMAT, locale)

        return Array(7) {
            calendar.set(Calendar.DAY_OF_WEEK, it)
            formatLetterDay.format(calendar.time)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mHeight = h
    }

    private fun getCurrentScreenWidth(): Int {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.x
    }

    interface OnElementSelectedListener {
        fun onElementSelect(isElementSelected: Boolean, date: String)
    }
}


package proj.stocks.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import proj.stocks.AppMain
import proj.stocks.R
import proj.stocks.databinding.FragmentCurrencyInfoBinding
import proj.stocks.util.*

class ItemListDialogFragment(
    private var currency: CurrencyDataCBR?,
    private var dateRange2: String?
) : BottomSheetDialogFragment() {

    constructor() : this(null, null)

    private var scope = CoroutineScope(Dispatchers.Default)
    private var dateRange1: String? = null
    private var nowDynamicPeriodType: DynamicPeriod = DynamicPeriod.UNKNOWN
    private var isClicked = false
    private var currencyDynamicList = ArrayList<GraphDynamicPoint>()
    private lateinit var binding: FragmentCurrencyInfoBinding
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrencyInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) return

        currency = savedInstanceState.getParcelable(CURRENCY)
        dateRange2 = savedInstanceState.getString(DATE_RANGE2)
        nowDynamicPeriodType = savedInstanceState.getParcelable(NOW_DYNAMIC_PERIOD)!!

    }

    override fun onStart() {
        super.onStart()
        if (dateRange2 == null) dateRange2 = LocalDate.now().format(dateFormatter)

        setTitleView()

        with(binding.dynamicButtonPanel.buttonGroup) {
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) makeClick(checkedId)
            }

            check(minusTypeMap[nowDynamicPeriodType]!!.bindId)
        }
        if (!isClicked) makeClick(minusTypeMap[nowDynamicPeriodType]!!.bindId)

        behavior = (dialog as BottomSheetDialog).behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


    private fun makeClick(checkedId: Int) {
        isClicked = true
        nowDynamicPeriodType =
            minusTypeMap.filterValues { it.bindId == checkedId }.keys.first()

        if (nowDynamicPeriodType != DynamicPeriod.UNKNOWN) {
            binding.standardBottomSheet.charCode.clearAnimation()
            binding.standardBottomSheet.charCode.visibility = View.INVISIBLE
            binding.loadingProgressbar.visibleLayout.visibility = View.VISIBLE
            getCurrencyDynamic()
        }
        isClicked = false
    }


    private fun getCurrencyDynamic() {
        if (scope.isActive) {
            scope.cancel()
            scope = CoroutineScope(Dispatchers.Default)
        }

        scope.launch {
            currencyDynamicList = ArrayList()
            try {
                dateRange1 = getMinusDate()
                val currencyDynamic = AppMain.responseService.getCurrencyDynamic(
                    dateRange1!!,
                    dateRange2!!,
                    currency!!.currId
                )

                for (currencyData in currencyDynamic.list!!) currencyDynamicList.add(
                    GraphDynamicPoint(
                        currencyData.date!!,
                        stringToFloat(currencyData.value!!) / stringToFloat(currencyData.nominal!!)
                    )
                )

            } catch (e: Exception) {
                Log.d("NetworkError", e.toString())
            } finally {
                withContext(Dispatchers.Main) {
                    drawGraph(currencyDynamicList)
                    binding.loadingProgressbar.visibleLayout.visibility = View.INVISIBLE
                }
            }
        }
    }


    private fun drawGraph(currencyDynamicList: ArrayList<GraphDynamicPoint>) {
        val graph = binding.graph
        val xAxis = graph.xAxis
        val yLeftAxis = graph.axisLeft

        // Draw xAxis background
        with(xAxis) {
            valueFormatter = MyAxisFormatter()
            enableGridDashedLine(10f, 10f, 0f)
            labelRotationAngle = 45f
            textColor = MaterialColors.getColor(context, R.attr.colorOnPrimary, null)
        }

        // Show dynamic of currency
        if (currencyDynamicList.isNotEmpty()) drawDeltaRange(
            currencyDynamicList.first(),
            currencyDynamicList.last()
        )

        // Get Entry list
        var counter = 0
        val entryGraph = ArrayList<Entry>()
        for (i in currencyDynamicList) entryGraph.add(Entry((counter++).toFloat(), i.value))


        // Draw yAxis with limit_line (now currency value)
        with(yLeftAxis) {
            removeAllLimitLines()
            if (currencyDynamicList.isNotEmpty()) {
                val lLine = LimitLine(currencyDynamicList.last().value)
                lLine.enableDashedLine(20f, 10f, 0f)
                addLimitLine(lLine)
            }
            enableGridDashedLine(10f, 10f, 0f)
            textColor = MaterialColors.getColor(context, R.attr.colorOnPrimary, null)
        }

        // Draw graph and make little dots on this graph
        val graphSet = LineDataSet(entryGraph, "")
        with(graphSet) {
            circleRadius = 1f
            circleHoleRadius = 0.5f
            setDrawCircleHole(false)
            setDrawHorizontalHighlightIndicator(false)
            setDrawVerticalHighlightIndicator(false)
        }


        // Make last graph options
        with(graph) {
            description.isEnabled = false
            axisRight.isEnabled = false
            setDrawBorders(true)
            setBorderColor(Color.GRAY)
            data = LineData(graphSet)
            data.setDrawValues(false)
            isScaleYEnabled = false
            invalidate()
        }
    }

    private fun getMinusDate(): String =
        LocalDate.parse(dateRange2, dateFormatter).minus(
            minusTypeMap[nowDynamicPeriodType]!!.count,
            minusTypeMap[nowDynamicPeriodType]!!.minusType
        ).format(dateFormatter)


    private fun setTitleView() {
        with(binding.standardBottomSheet) {
            name.text = CURRENCY_NAME_CODE.format(currency!!.name, currency!!.charCode)
            charCode.text = currency!!.charCode
        }
    }


    private fun drawDeltaRange(first: GraphDynamicPoint, last: GraphDynamicPoint) {
        val delta = -100f + last.value * 100f / first.value
        val inputString = CURRENCY_DELTA_RANGE.format(dateRange1, dateRange2, delta) + "%"
        with(binding.standardBottomSheet.charCode) {
            if (delta > 0) setTextColor(colorFromRes(R.color.plus_delta))
            else setTextColor(colorFromRes(R.color.minus_delta))
            text = inputString
            visibility = View.VISIBLE
            this.startAnimation(getAlphaAnimation())
        }
    }

    private fun colorFromRes(id: Int): Int = ResourcesCompat.getColor(resources, id, null)


    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)
        with(outState) {
            putParcelable(CURRENCY, currency)
            putParcelable(NOW_DYNAMIC_PERIOD, nowDynamicPeriodType)
            putString(DATE_RANGE2, dateRange2)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    inner class MyAxisFormatter : IndexAxisValueFormatter() {

        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            val index = value.toInt()
            return if (index < currencyDynamicList.size) {
                currencyDynamicList[index].date
            } else {
                String()
            }
        }
    }

}
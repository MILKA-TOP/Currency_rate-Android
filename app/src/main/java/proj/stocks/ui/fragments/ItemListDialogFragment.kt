package proj.stocks.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
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
import proj.stocks.R
import proj.stocks.databinding.FragmentCurrencyInfoBinding
import proj.stocks.util.*
import proj.stocks.view_model.ItemListDialogFragmentViewModel
import proj.stocks.view_model.ItemListDialogFragmentViewModelFactory

class ItemListDialogFragment(
    private var mCurrency: CurrencyDataCBR?,
    private var mDateRange2: String?
) : BottomSheetDialogFragment() {

    constructor() : this(null, null)

    private var nowDynamicPeriodType: DynamicPeriod = DynamicPeriod.UNKNOWN

    private lateinit var binding: FragmentCurrencyInfoBinding
    private lateinit var behavior: BottomSheetBehavior<FrameLayout>
    private lateinit var fragViewModel: ItemListDialogFragmentViewModel

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

        if (savedInstanceState != null) reInitArgs(savedInstanceState)

        fragViewModel = ViewModelProvider(
            this,
            ItemListDialogFragmentViewModelFactory(mCurrency!!, mDateRange2)
        )[ItemListDialogFragmentViewModel::class.java]

        observeInit()

    }

    private fun reInitArgs(savedInstanceState: Bundle) {
        mCurrency = savedInstanceState.getParcelable(CURRENCY)
        mDateRange2 = savedInstanceState.getString(DATE_RANGE2)
        nowDynamicPeriodType = savedInstanceState.getParcelable(NOW_DYNAMIC_PERIOD)!!
    }

    private fun observeInit() {
        fragViewModel.getDynamicResult().observe(this) {
            when (it.status) {
                Result.Status.LOADING -> loading()
                else -> {

                    if (Result.Status.ERROR == it.status) showErrorToast()
                    drawGraph(it.data)
                    binding.loadingProgressbar.visibleLayout.visibility = View.INVISIBLE
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()

        setTitleView()

        with(binding.dynamicButtonPanel.buttonGroup) {
            addOnButtonCheckedListener { _, checkedId, isChecked ->
                if (isChecked) makeClick(checkedId)
            }

            check(minusTypeMap[nowDynamicPeriodType]!!.bindId)
        }

        behavior = (dialog as BottomSheetDialog).behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun makeClick(checkedId: Int) {
        nowDynamicPeriodType =
            minusTypeMap.filterValues { it.bindId == checkedId }.keys.first()
        fragViewModel.updateDynamicResult(nowDynamicPeriodType)
    }

    private fun loading() {
        binding.standardBottomSheet.charCode.clearAnimation()
        binding.standardBottomSheet.charCode.visibility = View.INVISIBLE
        binding.loadingProgressbar.visibleLayout.visibility = View.VISIBLE
    }

    private fun drawGraph(currencyDynamic: CurrencyListDynamicCBR?) {
        val currencyDynamicList = fragViewModel.getDynamicPointList(currencyDynamic)
        val graph = binding.graph
        val xAxis = graph.xAxis
        val yLeftAxis = graph.axisLeft

        // Draw xAxis background
        with(xAxis) {
            valueFormatter = MyAxisFormatter(currencyDynamicList)
            enableGridDashedLine(10f, 10f, 0f)
            labelRotationAngle = 45f
            textColor = MaterialColors.getColor(context, R.attr.colorOnPrimary, null)
        }

        // Show dynamic of currency
        if (currencyDynamicList.isNotEmpty()) {
            val delta = fragViewModel.drawDeltaRange(
                currencyDynamicList.first(),
                currencyDynamicList.last()
            )
            setDeltaDynamic(delta, currencyDynamic!!.dateRange1!!, currencyDynamic.dateRange2!!)
        } else binding.standardBottomSheet.charCode.visibility = View.INVISIBLE

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

    private fun setTitleView() {
        binding.standardBottomSheet.name.text =
            CURRENCY_NAME_CODE.format(mCurrency!!.name, mCurrency!!.charCode)
    }

    private fun setDeltaDynamic(delta: Float, dateRange1: String, dateRange2: String) {
        with(binding.standardBottomSheet.charCode) {
            if (delta > 0) setTextColor(colorFromRes(R.color.plus_delta, resources))
            else setTextColor(colorFromRes(R.color.minus_delta, resources))

            val outputText = CURRENCY_DELTA_RANGE.format(dateRange1, dateRange2, delta) + "%"
            text = outputText
            visibility = View.VISIBLE
            this.startAnimation(getAlphaAnimation())
        }
    }

    private fun showErrorToast() {
        Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show()
    }


    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)
        with(outState) {
            putParcelable(CURRENCY, mCurrency)
            putParcelable(NOW_DYNAMIC_PERIOD, nowDynamicPeriodType)
            putString(DATE_RANGE2, mDateRange2)
        }
    }

    inner class MyAxisFormatter(private var currencyDynamicList: ArrayList<GraphDynamicPoint>) :
        IndexAxisValueFormatter() {

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
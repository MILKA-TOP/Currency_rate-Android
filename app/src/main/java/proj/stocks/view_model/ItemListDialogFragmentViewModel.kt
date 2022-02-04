package proj.stocks.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import org.threeten.bp.LocalDate
import proj.stocks.model.repositories.CurrencyDynamicRepository
import proj.stocks.util.*

class ItemListDialogFragmentViewModel(
    currencyDataCBR: CurrencyDataCBR,
    private var dateRange2: String?
) :
    ViewModel() {

    private var currencyDynamicResult: LiveData<Result<CurrencyListDynamicCBR>>
    private var mRepository: CurrencyDynamicRepository = CurrencyDynamicRepository(currencyDataCBR)
    private var scope = CoroutineScope(Dispatchers.Default)

    init {
        currencyDynamicResult = mRepository.getCurrencyDynamicLiveData()
        if (dateRange2 == null) dateRange2 = LocalDate.now().format(dateFormatter)
    }

    fun getDynamicResult() = currencyDynamicResult

    fun updateDynamicResult(nowDynamicPeriodType: DynamicPeriod) {
        reloadScope()
        scope.launch {
            val dataRange1 = getMinusDate(nowDynamicPeriodType)
            mRepository.downloadDynamic(dataRange1, dateRange2!!)
        }
    }

    fun getDynamicPointList(currencyDynamic: CurrencyListDynamicCBR?): ArrayList<GraphDynamicPoint> {
        val currencyDynamicList = ArrayList<GraphDynamicPoint>()
        if (currencyDynamic == null) return currencyDynamicList
        for (currencyData in currencyDynamic.list!!) currencyDynamicList.add(
            GraphDynamicPoint(
                currencyData.date!!,
                stringToFloat(currencyData.value!!) / stringToFloat(currencyData.nominal!!)
            )
        )
        return currencyDynamicList
    }

    fun drawDeltaRange(
        first: GraphDynamicPoint,
        last: GraphDynamicPoint,
    ): Float {
        return -100f + last.value * 100f / first.value
    }

    private fun reloadScope() {
        if (scope.isActive) {
            scope.cancel()
            scope = CoroutineScope(Dispatchers.Default)
        }
    }

    private fun getMinusDate(nowDynamicPeriodType: DynamicPeriod): String =
        LocalDate.parse(dateRange2, dateFormatter).minus(
            minusTypeMap[nowDynamicPeriodType]!!.count,
            minusTypeMap[nowDynamicPeriodType]!!.minusType
        ).format(dateFormatter)
}

class ItemListDialogFragmentViewModelFactory(
    private val currencyDataCBR: CurrencyDataCBR,
    private val dateRange2: String?
) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ItemListDialogFragmentViewModel(currencyDataCBR, dateRange2) as T
    }

}
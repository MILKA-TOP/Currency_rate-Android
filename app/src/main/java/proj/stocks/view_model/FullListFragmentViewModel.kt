package proj.stocks.view_model

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import proj.stocks.AppMain
import proj.stocks.model.database.AppDatabase
import proj.stocks.model.repositories.CurrencyListRepository
import proj.stocks.util.*


class FullListFragmentViewModel(
    private val sp: SharedPreferences,
    navType: NavigationType,
    context: Context
) : ViewModel() {


    private var currencyListResult: LiveData<Result<CurrencyListDailyCBR>>
    private var mRepository: CurrencyListRepository =
        CurrencyListRepository(AppDatabase.getDatabase(context).responseDao(), navType)

    init {
        currencyListResult = mRepository.getCurrencyListLiveData()
        downloadCurrencyList("")
    }

    fun getCurrencyList() = currencyListResult

    fun getNowDate(): String = currencyListResult.value?.data?.date ?: String()

    fun downloadCurrencyList(nowEditText: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val lang = sp.getString(SHARE_LANGUAGE, "ru")
            mRepository.downloadDailyCurrency(lang!!, nowEditText)
        }

    }

    fun updateCurrencyItem(curr: CurrencyDataCBR, filterString: String) {
        viewModelScope.launch(Dispatchers.Default) {
            mRepository.updateCurrency(curr, filterString)
        }
    }

    fun updateCurrencyList(nowEditText: String) {
        viewModelScope.launch(Dispatchers.Default) {
            mRepository.processFilter(nowEditText)
        }
    }

}

class FullListFragmentViewModelFactory(
    private val sp: SharedPreferences,
    private val navType: NavigationType,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FullListFragmentViewModel(sp, navType, context) as T
    }

}
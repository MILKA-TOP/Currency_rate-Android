package proj.stocks.model.repositories

import androidx.lifecycle.MutableLiveData
import proj.stocks.AppMain
import proj.stocks.util.CurrencyDataCBR
import proj.stocks.util.CurrencyListDynamicCBR
import proj.stocks.util.Result

class CurrencyDynamicRepository(private val currency: CurrencyDataCBR) {

    private var currencyMutableLiveData = MutableLiveData<Result<CurrencyListDynamicCBR>>()
    fun getCurrencyDynamicLiveData() = currencyMutableLiveData


    /**
     * Отправляет запрос к ЦБР, запрашивая динамику цен валюты currency c dataRange1 до dataRange2.
     * */
    suspend fun downloadDynamic(
        dataRange1: String,
        dataRange2: String,
    ) {
        currencyMutableLiveData.postValue(Result.loading())

        try {
            val currencyDynamic =
                AppMain.responseService.getCurrencyDynamic(dataRange1, dataRange2, currency.currId)
            currencyMutableLiveData.postValue(
                Result.success(currencyDynamic)
            )
        } catch (e: Exception) {
            currencyMutableLiveData.postValue(Result.error())
        }
    }
}
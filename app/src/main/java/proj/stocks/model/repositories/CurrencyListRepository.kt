package proj.stocks.model.repositories

import androidx.lifecycle.MutableLiveData
import proj.stocks.AppMain
import proj.stocks.model.database.ResponseCBR_DAO
import proj.stocks.util.CurrencyDataCBR
import proj.stocks.util.CurrencyListDailyCBR
import proj.stocks.util.NavigationType
import proj.stocks.util.Result


class CurrencyListRepository(
    private val currencyDAO: ResponseCBR_DAO,
    private val navType: NavigationType
) {

    private var currencyMutableLiveData = MutableLiveData<Result<CurrencyListDailyCBR>>()

    fun getCurrencyListLiveData() = currencyMutableLiveData


    /**
     * Нажатие на кнопку "Favourite" соответствующему curr: CurrencyDataCBR,
     * после чего его значение isFavourite изменяется на противоположное.
     * */
    fun updateCurrency(
        curr: CurrencyDataCBR, filterString: String
    ) {
        try {
            val currencyDaily = currencyMutableLiveData.value!!.data!!

            currencyDAO.updateFavourite(curr.currId, !curr.isFavourite)
            currencyDaily.list = getFilterList(filterString)
            currencyMutableLiveData.postValue(Result.success(currencyDaily))

        } catch (_: Exception) {
        }
    }


    /**
     * Получение данных от ЦБР и отправка их в currencyMutableLiveData.
     * */
    suspend fun downloadDailyCurrency(
        lang: String,
        filterString: String
    ) {
        currencyMutableLiveData.postValue(Result.loading())
        try {
            val favouriteList = currencyDAO.getFavouriteList()
            var allCurrencyDaily = getCurrenciesLang(lang)
            allCurrencyDaily = makeListWithFavourite(favouriteList, allCurrencyDaily)

            currencyDAO.deleteAll()
            currencyDAO.insertAll(allCurrencyDaily.list!!)

            allCurrencyDaily.list = getFilterList(filterString)

            currencyMutableLiveData.postValue(
                Result.success(allCurrencyDaily)
            )
        } catch (e: Exception) {
            currencyMutableLiveData.postValue(Result.error())
        }

    }


    /**
     * Обновление списка по соответствующему параметру фильтрации filterString.
     * */
    fun processFilter(filterString: String) {
        try {
            val currencyDaily = currencyMutableLiveData.value!!.data!!
            currencyDaily.list = getFilterList(filterString)
            currencyMutableLiveData.postValue(Result.success(currencyDaily))
        } catch (_: Exception) {
        }
    }

    /**
     * Поиск в полученном списке такие валют, которые соответствуют filterString,
     * после чего возвращает отфильтрованный список.
     * */
    private fun makeFilterList(
        filterString: String,
        nowFullCurrencyList: ArrayList<CurrencyDataCBR>
    ): ArrayList<CurrencyDataCBR> {
        val tempContacts = ArrayList<CurrencyDataCBR>()
        for (contact in nowFullCurrencyList) {
            if (contact.sameEditText(filterString)) tempContacts.add(contact)
        }
        return tempContacts
    }


    private fun getFilterList(nowEditText: String): ArrayList<CurrencyDataCBR> =
        makeFilterList(nowEditText, ArrayList(getCurrenciesNavType()))


    /**
     * Обновление списка получаемого списка, изменяя значения isFavourite на те, которые
     * соответствуют БД на устройстве (в случае, если такие есть).
     * */
    private fun makeListWithFavourite(
        favouriteCurrencies: List<CurrencyDataCBR>?,
        allCurrencies: CurrencyListDailyCBR
    ): CurrencyListDailyCBR {
        if (favouriteCurrencies != null) {
            for (currency in favouriteCurrencies)
                allCurrencies.list?.find { currency.currId == it.currId }?.isFavourite = true
        }
        return allCurrencies
    }

    /**
     * Возвращает либо список "Избранного", либо общий список курса валют, в зависимости от того,
     * в какой фрагменте находится пользователь.
     * */
    private fun getCurrenciesNavType(): List<CurrencyDataCBR> {
        return when (navType) {
            NavigationType.FAVOURITE -> currencyDAO.getFavouriteList()
            else -> currencyDAO.getAll()
        }
    }

    /**
     * Производит запрос в сеть к ЦБР на том языке, какой запрашивается.
     * (Вообще ЦБР позволяет получить данные только на английском и русском языках,
     * но вдруг они когда-нибудь добавят новый язык, поэтому и добавлен комментарий ниже)
     * */
    private suspend fun getCurrenciesLang(lang: String): CurrencyListDailyCBR {
        return when (lang) {
            "en" -> AppMain.responseService.getAllCurrenciesEN()
            "ru" -> AppMain.responseService.getAllCurrenciesRU()
            //  "LANG_CODE" ->  AppMain.responseService.getAllCurrencies[LANG_CODE]
            else -> AppMain.responseService.getAllCurrenciesRU()
        }
    }


}
package proj.stocks.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import proj.stocks.AppMain
import proj.stocks.R
import proj.stocks.databinding.FragmentFullListBinding
import proj.stocks.util.*


class FullListFragment : Fragment() {

    private lateinit var fragmentBinding: FragmentFullListBinding
    private lateinit var mAdapter: CurrencyAdapter
    private lateinit var navigationType: NavigationType
    private var dialogFragment: ItemListDialogFragment? = null
    private var scope = CoroutineScope(Dispatchers.Default)
    private var checkedData: String? = null
    private var startLoading = false
    private var currencyList: ArrayList<CurrencyDataCBR> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentBinding = FragmentFullListBinding.inflate(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navigationType = savedInstanceState?.getParcelable(NAVIGATION_TYPE) ?: getNavigationType()

        if (savedInstanceState == null) return

        checkedData = savedInstanceState.getString(CHECKED_DATA)
        val todayArrayList =
            savedInstanceState.getParcelableArrayList<CurrencyDataCBR>(CURRENCY_LIST)
        if (todayArrayList != null) currencyList = todayArrayList
        if (savedInstanceState.getBoolean(IS_LOADING)) startLoading = true
    }

    override fun onStart() {
        super.onStart()
        if (currencyList.size == 0 || startLoading) downloadData()
        else updateCurrencyList()

        fragmentBinding.refresh.setOnRefreshListener {
            downloadData(true)
        }

        fragmentBinding.toolbarList.editText.addTextChangedListener(NowWatcher())

    }

    private fun updateCurrencyList() {
        scope.launch {
            val allCurrencies: ArrayList<CurrencyDataCBR> = when (navigationType) {
                NavigationType.FAVOURITE -> ArrayList(
                    AppMain.dataBaseInfo.responseDao()!!.getFavouriteList()
                )
                NavigationType.FULL -> ArrayList(AppMain.dataBaseInfo.responseDao()!!.getAll())
                else -> {
                    return@launch
                }
            }
            currencyList = allCurrencies
            withContext(Dispatchers.Main) {
                recyclerDraw(RecyclerDrawType.REDRAW)
            }
        }
    }

    private fun recyclerDraw(
        recyclerDrawType: RecyclerDrawType,
        indexUpdate: Int = -1,
        currId: String? = null
    ) {
        if (!::mAdapter.isInitialized) adapterInitialization()
        when (recyclerDrawType) {
            RecyclerDrawType.FIRST_DRAW -> adapterConnect()
            RecyclerDrawType.CHANGE_OBJECT -> {
                if (navigationType == NavigationType.FULL) mAdapter.notifyItemChanged(indexUpdate)
                else if (navigationType == NavigationType.FAVOURITE) mAdapter.removeByCurrencyId(
                    currId!!,
                    indexUpdate
                )
            }
            RecyclerDrawType.REDRAW -> {
                adapterConnect()
                mAdapter.setList(currencyList)
            }
        }
    }

    private fun adapterConnect() {
        val myRecyclerView = fragmentBinding.fullStocksRecycler
        val viewManager = LinearLayoutManager(context)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = mAdapter
        }
    }

    private fun adapterInitialization() {
        mAdapter =
            CurrencyAdapter(currencyList) { it: CurrencyDataCBR?, a: OnClickRecyclerTouch, view: View? ->
                onRecyclerClick(it, a, view)
            }
    }

    private fun onRecyclerClick(
        curData: CurrencyDataCBR?,
        clickType: OnClickRecyclerTouch,
        curItem: View?
    ) {
        curItem?.startAnimation(
            AnimationUtils.loadAnimation(
                context,
                R.anim.alpha
            )
        )
        when (clickType) {
            OnClickRecyclerTouch.ADD_TO_FAVOURITE -> addToFavouriteClicked(curData)
            OnClickRecyclerTouch.OPEN_CURRENCY_INFO -> openDialogFragment(curData)
        }
    }

    private fun openDialogFragment(curData: CurrencyDataCBR?) {
        if (curData == null) return
        dialogFragment = ItemListDialogFragment(curData, checkedData)
        dialogFragment?.show(parentFragmentManager, "q")
    }

    private fun addToFavouriteClicked(curData: CurrencyDataCBR?) {
        if (curData == null) return

        scope.launch {
            try {
                with(AppMain.dataBaseInfo.responseDao()) {
                    val currency = this?.getCurrency(curData.currId)!!
                    val currencyIndex = currencyList.indexOf(currency)
                    currency.isFavourite = !currency.isFavourite
                    this.updateCurrency(currency)
                    val adapterIndex = mAdapter.getIndexByCurrencyId(currency.currId)
                    if (navigationType == NavigationType.FAVOURITE) currencyList.removeAt(
                        currencyIndex
                    ) else if (navigationType == NavigationType.FULL) currencyList[currencyIndex].isFavourite =
                        currency.isFavourite

                    withContext(Dispatchers.Main) {
                        recyclerDraw(RecyclerDrawType.CHANGE_OBJECT, adapterIndex, currency.currId)
                    }
                }
            } catch (e: Exception) {
                Log.d("ERROR_HERE", e.toString())
            }
        }

    }

    private fun downloadData(byUser: Boolean = false) {
        if (!byUser) fragmentBinding.progressBarLayout.visibleLayout.visibility =
            View.VISIBLE

        startLoading = true
        scope.launch {
            Log.d("DOWNLOAD_CURRENCIES", "start process")
            try {
                val currenciesData = getCurrencies()
                var allCurrenciesList = currenciesData.list!!
                checkedData = currenciesData.date

                allCurrenciesList = markFavouriteCurrencies(allCurrenciesList)

                with(AppMain.dataBaseInfo.responseDao()!!) {
                    deleteAll()
                    insertAll(allCurrenciesList)
                }
                updateCurrencyList()
                Log.d("DOWNLOAD_CURRENCIES", "finish process")
            } catch (e: Exception) {
                Log.d("DOWNLOAD_CURRENCIES", e.toString())
            } finally {
                withContext(Dispatchers.Main) {
                    fragmentBinding.refresh.isRefreshing = false
                    fragmentBinding.progressBarLayout.visibleLayout.visibility =
                        View.INVISIBLE
                }
                startLoading = false
            }
        }
    }

    private fun markFavouriteCurrencies(allCurrencies: ArrayList<CurrencyDataCBR>): ArrayList<CurrencyDataCBR> {
        val favouriteCurrencies =
            AppMain.dataBaseInfo.responseDao()?.getFavouriteList()

        if (favouriteCurrencies != null) {
            for (currency in favouriteCurrencies)
                allCurrencies.find { currency.currId == it.currId }?.isFavourite = true
        }
        return allCurrencies
    }

    private fun getNavigationType(): NavigationType {
        when (findNavController().currentDestination?.id) {
            R.id.full_list_fragment -> return NavigationType.FULL
            R.id.favourite_fragment -> return NavigationType.FAVOURITE
        }
        return NavigationType.UNKNOWN
    }

    private suspend fun getCurrencies(): CurrencyListDailyCBR {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return when (pref.getString(SHARE_LANGUAGE, "ru")) {
            "en" -> AppMain.responseService.getAllCurrenciesEN()
            else -> AppMain.responseService.getAllCurrenciesRU()
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("START_RESTORE", "RESTORE")

        with(outState) {
            putParcelableArrayList(CURRENCY_LIST, ArrayList(currencyList))
            putParcelable(NAVIGATION_TYPE, navigationType)
            putBoolean(IS_LOADING, startLoading)
        }
    }

    override fun onPause() {
        fragmentBinding.toolbarList.editText.text.clear()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    inner class NowWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (currencyList.isNullOrEmpty()) return

            if (s.isNullOrEmpty()) recyclerDraw(RecyclerDrawType.REDRAW)
            else {
                val sl = s.toString().lowercase()
                val tempContacts = ArrayList<CurrencyDataCBR>()
                for (contact in currencyList) {
                    if (contact.sameEditText(sl)) tempContacts.add(contact)
                }
                mAdapter.setList(tempContacts)
            }
            Log.d("ADAPTER_SIZE", mAdapter.itemCount.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

}
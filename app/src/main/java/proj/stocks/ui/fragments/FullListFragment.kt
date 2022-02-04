package proj.stocks.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import proj.stocks.R
import proj.stocks.databinding.FragmentFullListBinding
import proj.stocks.util.*
import proj.stocks.view_model.FullListFragmentViewModel
import proj.stocks.view_model.FullListFragmentViewModelFactory


class FullListFragment : Fragment() {

    private lateinit var mFragmentBinding: FragmentFullListBinding
    private lateinit var mAdapter: CurrencyAdapter
    private lateinit var mNavigationType: NavigationType
    private var dialogFragmentOld: ItemListDialogFragment? = null
    private var nowEditText = String()

    private lateinit var fragViewModel: FullListFragmentViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentBinding = FragmentFullListBinding.inflate(inflater, container, false)
        return mFragmentBinding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nowEditText = savedInstanceState?.getString(EDIT_TEXT) ?: String()

        mNavigationType = getNavigationType(findNavController().currentDestination?.id)

        fragViewModel = ViewModelProvider(
            this,
            FullListFragmentViewModelFactory(
                PreferenceManager.getDefaultSharedPreferences(context),
                    mNavigationType, requireActivity().baseContext
            )
        )[FullListFragmentViewModel::class.java]

        observeInit()
    }

    private fun observeInit() {
        fragViewModel.getCurrencyList().observe(this) {
            when (it.status) {
                Result.Status.SUCCESS -> adapterUpdate(it.data!!.list!!)
                Result.Status.ERROR -> {
                    Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {}
            }

            if (it.status != Result.Status.LOADING) {
                mFragmentBinding.refresh.isRefreshing = false
                mFragmentBinding.progressBarLayout.visibleLayout.visibility = View.INVISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!::mAdapter.isInitialized) {
            mFragmentBinding.progressBarLayout.visibleLayout.visibility =
                View.VISIBLE
            adapterUpdate(ArrayList())
        } else connectAdapterToRecycler()

        mNavigationType = getNavigationType(findNavController().currentDestination?.id)

        fragViewModel.updateCurrencyList(nowEditText)

        mFragmentBinding.refresh.setOnRefreshListener {
            fragViewModel.downloadCurrencyList(nowEditText)
        }

        mFragmentBinding.toolbarList.editText.addTextChangedListener(NowWatcher())
    }


    private fun adapterUpdate(list: ArrayList<CurrencyDataCBR>) {
        adapterInitialization(list)
        mAdapter.setList(list)
    }

    private fun adapterInitialization(li: ArrayList<CurrencyDataCBR>?) {
        if (!::mAdapter.isInitialized) {
            mAdapter =
                CurrencyAdapter(li!!) { it: CurrencyDataCBR?, a: OnClickRecyclerTouch, view: View? ->
                    onRecyclerClick(it, a, view)
                }
            connectAdapterToRecycler()
        }
    }

    private fun connectAdapterToRecycler() {
        mFragmentBinding.fullStocksRecycler.adapter = mAdapter
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
        if (curData == null) return
        when (clickType) {
            OnClickRecyclerTouch.ADD_TO_FAVOURITE -> addToFavouriteClicked(curData)
            OnClickRecyclerTouch.OPEN_CURRENCY_INFO -> openDialogFragment(curData)
        }
    }

    private fun addToFavouriteClicked(curData: CurrencyDataCBR) {
        fragViewModel.updateCurrencyItem(curData, nowEditText)
    }

    private fun openDialogFragment(curData: CurrencyDataCBR) {
        dialogFragmentOld = ItemListDialogFragment(curData, fragViewModel.getNowDate())
        dialogFragmentOld?.show(parentFragmentManager, "CurrencyInfo")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EDIT_TEXT, nowEditText)
    }

    inner class NowWatcher : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            nowEditText = s?.toString() ?: String()
            fragViewModel.updateCurrencyList(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }


}
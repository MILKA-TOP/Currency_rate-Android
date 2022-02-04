package proj.stocks.util

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import proj.stocks.R
import proj.stocks.databinding.CurrencyItemBinding
import java.util.*

class CurrencyAdapter(
    private var currencies: ArrayList<CurrencyDataCBR>,
    private val onClick: (CurrencyDataCBR?, OnClickRecyclerTouch, View?) -> Unit,
) : RecyclerView.Adapter<CurrencyAdapter.CurrentViewHolder>() {

    private lateinit var itemPersonBinding: CurrencyItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        itemPersonBinding = CurrencyItemBinding.inflate(layoutInflater, parent, false)
        val currencyHolder = CurrentViewHolder(itemPersonBinding)
        itemPersonBinding.favouriteAdd.setOnClickListener {
            if (currencyHolder.absoluteAdapterPosition != -1) onClick(
                currencies[currencyHolder.absoluteAdapterPosition],
                OnClickRecyclerTouch.ADD_TO_FAVOURITE, null
            )
        }

        itemPersonBinding.currencyTem.setOnClickListener {
            onClick(
                currencies[currencyHolder.absoluteAdapterPosition],
                OnClickRecyclerTouch.OPEN_CURRENCY_INFO, it
            )
        }

        return currencyHolder

    }

    override fun onBindViewHolder(holder: CurrentViewHolder, position: Int) =
        holder.bind(currencies[position])

    override fun getItemCount() = currencies.size

    fun setList(list: ArrayList<CurrencyDataCBR>) {
        val productDiffUtilCallback =
            ContactDiffUtilCallback(currencies, list)
        val productDiffResult = DiffUtil.calculateDiff(productDiffUtilCallback)
        this.currencies = list
        productDiffResult.dispatchUpdatesTo(this)
    }

    inner class CurrentViewHolder(private val currencyItem: CurrencyItemBinding) :
        RecyclerView.ViewHolder(currencyItem.root) {

        fun bind(currency: CurrencyDataCBR) {
            currencyItem.curId.text = currency.currId
            currencyItem.curName.text = currency.name
            currencyItem.curValue.text = currency.value
            val currencySymbol = Currency.getInstance(currency.charCode).getSymbol(Locale.US)
            val currencyValues =
                CURRENCY_VALUE.format(
                    currency.nominal,
                    currencySymbol,
                    currency.value!!.replace(',', '.').toDouble()
                )

            currencyItem.curValue.text = currencyValues

            if (currency.isFavourite) currencyItem.favouriteAdd.setImageResource(R.drawable.ic_baseline_star_24)
            else currencyItem.favouriteAdd.setImageResource(R.drawable.ic_baseline_star_border_24)
        }
    }
}
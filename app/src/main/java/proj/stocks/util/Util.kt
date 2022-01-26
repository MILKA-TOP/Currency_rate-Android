package proj.stocks.util

import android.content.Context
import android.os.Parcelable
import android.util.Log
import android.view.animation.AlphaAnimation
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import kotlinx.parcelize.Parcelize
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import proj.stocks.R
import java.util.*

@Parcelize
enum class NavigationType : Parcelable {
    FULL, FAVOURITE, UNKNOWN
}

enum class RecyclerDrawType {
    FIRST_DRAW, CHANGE_OBJECT, REDRAW
}

@Parcelize
enum class DynamicPeriod : Parcelable {
    WEEK1, WEEK2, MONTH, YEAR1, YEAR5, UNKNOWN
}

enum class OnClickRecyclerTouch {
    ADD_TO_FAVOURITE, OPEN_CURRENCY_INFO
}

class ContactDiffUtilCallback(
    private val oldList: ArrayList<CurrencyDataCBR>,
    private val newList: ArrayList<CurrencyDataCBR>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition] == newList[newItemPosition]

}


fun stringToFloat(s: String): Float {
    return try {
        s.replace(',', '.').toFloat()
    } catch (_: Exception) {
        0f
    }
}

fun getAlphaAnimation(): AlphaAnimation {
    val alphaAnimation = AlphaAnimation(0f, 1f)
    alphaAnimation.duration = 1000
    alphaAnimation.repeatCount = AlphaAnimation.INFINITE
    alphaAnimation.repeatMode = AlphaAnimation.REVERSE
    return alphaAnimation
}

fun getUserTheme(context: Context) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)

    when (pref.getString("theme", "base")) {
        "base" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
    Log.d("THEME", pref.getString("theme", "base").toString())
}

fun getUserLanguage(context: Context) {
    val pref = PreferenceManager.getDefaultSharedPreferences(context)

    val localeCode = if (pref.getString("language", "ru") != null)
        pref.getString("language", "ru")!!
    else "ru"

    val locale = Locale(localeCode)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}


val minusTypeMap = mapOf(
    DynamicPeriod.WEEK1 to DynamicPeriodMinus(1, ChronoUnit.WEEKS, R.id.week1),
    DynamicPeriod.UNKNOWN to DynamicPeriodMinus(1, ChronoUnit.WEEKS, R.id.week1),
    DynamicPeriod.WEEK2 to DynamicPeriodMinus(2, ChronoUnit.WEEKS, R.id.week2),
    DynamicPeriod.MONTH to DynamicPeriodMinus(1, ChronoUnit.MONTHS, R.id.month),
    DynamicPeriod.YEAR1 to DynamicPeriodMinus(1, ChronoUnit.YEARS, R.id.year1),
    DynamicPeriod.YEAR5 to DynamicPeriodMinus(5, ChronoUnit.YEARS, R.id.year5)
)

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

const val CURRENCY_NAME_CODE = "%s (%s)"
const val CURRENCY_DELTA_RANGE = "(%s - %s): %.2f"
const val CURRENCY_VALUE = "%s %s = %.2f руб."
const val DATABASE_NAME = "INFO_DATABASE"
const val NOW_DYNAMIC_PERIOD = "NOW_DYNAMIC_PERIOD"
const val DATE_RANGE2 = "DATE_RANGE2"
const val CURRENCY = "CURRENCY"
const val IS_LOADING = "IS_LOADING"
const val CURRENCY_LIST = "CURRENCY_LIST"
const val CHECKED_DATA = "CHECKED_DATA"
const val NAVIGATION_TYPE = "NAVIGATION_TYPE"
const val SHARE_LANGUAGE = "language"
const val SHARE_THEME = "theme"
const val SCRIPT_CBR_LINK = "https://www.cbr.ru/scripts/"
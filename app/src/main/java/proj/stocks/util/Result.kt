package proj.stocks.util


data class Result<out T>(
    val status: Status,
    val data: T?,
) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T?): Result<T> {
            return Result(Status.SUCCESS, data)
        }

        fun <T> error(): Result<T> {
            return Result(Status.ERROR, null)
        }

        fun <T> loading(data: T? = null): Result<T> {
            return Result(Status.LOADING, data)
        }
    }

    override fun toString(): String {
        return "Result(status=$status, data=$data)"
    }
}
package util

class Timer(val time: Long) {
    var timeStarted = System.currentTimeMillis()

    fun done() = System.currentTimeMillis() - timeStarted > time
    fun reset(): Timer {
        timeStarted = System.currentTimeMillis()
        return this
    }
    fun finish(): Timer {
        timeStarted = Long.MIN_VALUE
        return this
    }
}

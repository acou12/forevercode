package util

class Timer(val time: Long) {
    var timeStarted: Long = 0

    fun elapsed() = System.currentTimeMillis() - timeStarted

    fun done() = elapsed() >= time

    fun reset(): Timer {
        timeStarted = System.currentTimeMillis()
        return this
    }

    fun finish(): Timer {
        timeStarted = System.currentTimeMillis() - time
        return this
    }
}

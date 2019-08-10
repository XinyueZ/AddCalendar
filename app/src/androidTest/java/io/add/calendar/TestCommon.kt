package io.add.calendar

import java.util.Random

const val TEST_CASE_1 = "July 20, 1969, 20:17"
const val TEST_CASE_2 = "July 20, 1969, at 20:17"

fun getRandomInt(from: Int = Int.MIN_VALUE, to: Int = Int.MAX_VALUE) =
    Random().nextInt(to - from) + from

fun getRandomBoolean(): Boolean {
    val randInt = getRandomInt(1)
    return when {
        randInt % 2 == 0 -> true
        else -> false
    }
}
package io.add.calendar

import java.util.Random

const val TEST_CASE_1 = "June 20, 1969, 20:17"
const val TEST_CASE_2 = "June 20, 1969, at 20:17"
const val TEST_CASE_3 = "1969年6月20日, 20：17"
const val TEST_CASE_4 = "20. Juni 1969, 20:17"
const val TEST_CASE_5 = "20 июня 1969 года, 20:17"
const val TEST_CASE_6 = "20. Juli 1969, 20:17"
const val TEST_CASE_7 = "20. Oktober 1969"
const val TEST_CASE_8 = "10. Januar 1969"
const val TEST_CASE_BAD = "asfdldlghjflgh"
const val TEST_CASE_FLAG = "asfdldlghjflghasfdasdf"
const val TEST_CASE_TRIM = "    ,;# abc,;#  "

fun getRandomInt(from: Int = Int.MIN_VALUE, to: Int = Int.MAX_VALUE) =
    Random().nextInt(to - from) + from

fun getRandomBoolean(): Boolean {
    val randInt = getRandomInt(1)
    return when {
        randInt % 2 == 0 -> true
        else -> false
    }
}

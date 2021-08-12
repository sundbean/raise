package com.sundbean.raise

import android.content.Context
import android.content.res.Resources
import com.google.common.math.DoubleMath.roundToInt
import kotlin.math.roundToInt

fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

fun dpFromPx(context: Context, px: Int): Int {
    return ((px / context.getResources().getDisplayMetrics().density).toDouble()).roundToInt()
}
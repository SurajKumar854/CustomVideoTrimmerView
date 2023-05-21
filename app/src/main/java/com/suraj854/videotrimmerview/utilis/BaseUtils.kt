package com.suraj854.videotrimmerview.utilis

import android.content.Context
import java.lang.ref.WeakReference

object BaseUtils {
    private const val ERROR_INIT = "Initialize BaseUtils with invoke init()"
    private var mWeakReferenceContext: WeakReference<Context>? = null

    /**
     * init in Application
     */
    fun init(ctx: Context) {
        mWeakReferenceContext = WeakReference(ctx)
        //something to do...
    }

    val context: Context
        get() {
            requireNotNull(mWeakReferenceContext) { ERROR_INIT }
            return mWeakReferenceContext!!.get()!!.applicationContext
        }
}

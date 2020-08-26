package com.simform.risetestapp.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * This is base activity of all activities
 */
abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity(), View.OnClickListener {

    protected lateinit var bindObject: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindObject = DataBindingUtil.setContentView(this, getLayoutResId())
    }

    /**
     * This function is used to get layout resource id for all the screens.
     */
    @LayoutRes
    abstract fun getLayoutResId(): Int
}
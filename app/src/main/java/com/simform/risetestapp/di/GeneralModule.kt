package com.simform.risetestapp.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val generalModule = module {

    single {
        androidContext().resources
    }

    single {
        androidContext().getSharedPreferences(androidContext().packageName, Context.MODE_PRIVATE)
    }
}
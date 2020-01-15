package com.example.android.kotlincoroutines.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <T: ViewModel, A> singleArgViewModelFactory(constuctor: (A)-> T):
        (A) -> ViewModelProvider.NewInstanceFactory {
    return {arg: A ->
        object : ViewModelProvider.NewInstanceFactory() {
            override fun <V : ViewModel?> create(modelClass: Class<V>): V {
                return constuctor(arg) as V
            }
        }
    }
}
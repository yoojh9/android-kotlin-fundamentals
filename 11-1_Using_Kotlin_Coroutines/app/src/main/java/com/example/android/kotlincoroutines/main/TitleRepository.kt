package com.example.android.kotlincoroutines.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.kotlincoroutines.util.BACKGROUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TitleRepository (val network: MainNetwork, val titleDao: TitleDao) {

    val title: LiveData<String?> = titleDao.titleLiveData.map {it?.title}


    suspend fun refreshTitle() {
        // interact with *blocking* network and IO calls from a coroutine
        try {
            val result = network.fetchNextTitle()
            titleDao.insertTitle(Title(result))
        } catch (cause: Throwable) {
            throw TitleRefreshError("Unable to refresh title", cause)
        }
    }
}

class TitleRefreshError(message: String, cause: Throwable?): Throwable(message, cause)

interface TitleRefreshCallback {
    fun onCompleted()
    fun onError(cause: Throwable)
}
package com.example.android.kotlincoroutines.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.kotlincoroutines.util.BACKGROUND
import kotlinx.coroutines.delay

class TitleRepository (val network: MainNetwork, val titleDao: TitleDao) {

    val title: LiveData<String?> = titleDao.titleLiveData.map {it?.title}

    fun refreshTitleWIthCallbacks(titleRefreshCallback: TitleRefreshCallback) {
        BACKGROUND.submit{
            try {
                val result = network.fetchNetxtTitle().execute()
                if(result.isSuccessful){
                    titleDao.insertTitle(Title(result.body()!!))
                    titleRefreshCallback.onCompleted()
                } else {
                    titleRefreshCallback.onError(
                        TitleRefreshError("Unable to refresh title", null)
                    )
                }
            } catch(cause: Throwable) {
                titleRefreshCallback.onError(
                    TitleRefreshError("Unable to refresh title", cause)
                )
            }
        }
    }

    suspend fun refreshTitle() {
        delay(500)
    }
}

class TitleRefreshError(message: String, cause: Throwable?): Throwable(message, cause)

interface TitleRefreshCallback {
    fun onCompleted()
    fun onError(cause: Throwable)
}
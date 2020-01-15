package com.example.android.kotlincoroutines.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.android.kotlincoroutines.util.BACKGROUND
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TitleRepository (val network: MainNetwork, val titleDao: TitleDao) {

    val title: LiveData<String?> = titleDao.titleLiveData.map {it?.title}

    fun refreshTitleWIthCallbacks(titleRefreshCallback: TitleRefreshCallback) {
        BACKGROUND.submit{
            try {
                val result = network.fetchNextTitle().execute()
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
        // interact with *blocking* network and IO calls from a coroutine
        withContext(Dispatchers.IO) {
            val result = try {
                // Make network request using a blocking call
                network.fetchNextTitle().execute()
            } catch (cause: Throwable) {
                // If the network throws an exception, inform the caller
                throw TitleRefreshError("Unable to refresh title", cause)
            }

            if (result.isSuccessful) {
                // Save it to database
                titleDao.insertTitle(Title(result.body()!!))
            } else {
                // If it's not successful, inform the callback of the error
                throw TitleRefreshError("Unable to refresh title", null)
            }
        }
    }
}

class TitleRefreshError(message: String, cause: Throwable?): Throwable(message, cause)

interface TitleRefreshCallback {
    fun onCompleted()
    fun onError(cause: Throwable)
}
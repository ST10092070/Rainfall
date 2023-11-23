package com.corne.rainfall.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.corne.rainfall.data.model.PrefModel
import com.corne.rainfall.data.preference.IRainfallPreference
import com.corne.rainfall.data.storage.IRainRepository
import com.corne.rainfall.data.storage.RainfallRemoteRepo
import com.corne.rainfall.data.task.IRainTaskManager
import com.corne.rainfall.di.LocalRainfallRepository
import com.corne.rainfall.di.RemoteRainfallRepository
import com.corne.rainfall.utils.NetworkResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Date

@HiltWorker
class RainSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    @RemoteRainfallRepository private val remoteRepo: IRainRepository,
    @LocalRainfallRepository private val localRepo: IRainRepository,
    private val rainfallPreference: IRainfallPreference,
    private val rainTaskManager: IRainTaskManager,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return syncRainData()
    }


    private suspend fun syncRainData(): Result {
        return try {
            val lastUpdated = rainfallPreference.lastUpdatedDateFlow.first()
            if (lastUpdated == null) {
                // We have never synced before
                // Here we want to get all the data from the remote source and save it to the local source

                if (remoteRepo is RainfallRemoteRepo) {
                    remoteRepo.downloadPreferences().collect {
                        if (it is NetworkResult.Success) {
                            rainfallPreference.setDarkMode(it.data.darkMode)
                            rainfallPreference.setLanguageMode(it.data.language)
                            rainfallPreference.setOfflineMode(it.data.offline)
                            rainfallPreference.setDefaultLocation(it.data.location)
                            rainfallPreference.setDefaultGraph(it.data.graphType)
                            rainfallPreference.setLastUpdatedDate(it.data.lastUpdated!!)
                        }
                    }
                }
                // Add all the location to the local source
                remoteRepo.getAllLocations().collect { loc ->
                    if (loc is NetworkResult.Success) loc.data.forEach { localRepo.addLocation(it) }
                }
                // Add all the rain data to the local sources
                remoteRepo.getAllRainfallData().collect { rain ->
                    if (rain is NetworkResult.Success) rain.data.forEach { localRepo.addRainData(it) }
                }

                // Add all the rain data to the local source
                return Result.success()
            }

            // We have synced before so we want to push all our local data to remote
            if (remoteRepo is RainfallRemoteRepo) {
                val darkMode = rainfallPreference.uiModeFlow.first()
                val language = rainfallPreference.languageModeFlow.first()
                val offline = rainfallPreference.offlineModeFlow.first()
                val location = rainfallPreference.defaultLocationFlow.first()
                val graphType = rainfallPreference.defaultGraphFlow.first()
                remoteRepo.uploadPreferences(
                    PrefModel(darkMode, language, offline, location, graphType, Date().time)
                )
            }

            // TODO: Bulk upload data
            localRepo.getAllRainfallData().collect { rain ->
                if (rain is NetworkResult.Success) rain.data.forEach { remoteRepo.addRainData(it) }
            }
            localRepo.getAllLocations().collect { loc ->
                if (loc is NetworkResult.Success) loc.data.forEach { remoteRepo.addLocation(it) }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
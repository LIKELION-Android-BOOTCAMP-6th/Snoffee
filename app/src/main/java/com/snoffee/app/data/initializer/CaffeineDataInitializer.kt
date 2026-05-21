package com.snoffee.app.data.initializer

import com.snoffee.app.data.datasource.local.DrinkLocalDataSource
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSource
import com.snoffee.app.data.mapper.DrinkMapper
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class CaffeineDataInitializer @Inject constructor(
    private val localDataSource: DrinkLocalDataSource,
    private val remoteDataSource: DrinkRemoteDataSource,
    private val mapper: DrinkMapper
) {
    suspend fun initializeIfEmpty() {
        if (localDataSource.getCount() == 0) {
            val remoteList = remoteDataSource.fetchDrinkList()
            localDataSource.insertDrinks(remoteList.map { mapper.mapToEntity(it) })
        }
    }
}
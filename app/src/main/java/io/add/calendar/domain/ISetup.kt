package io.add.calendar.domain

import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel
import java.util.Locale

interface ISetup {

    suspend fun downloadModels(
        forceDownload: Boolean,
        vararg langNames: String
    )

    suspend fun createRemoteModel(
        forceDownload: Boolean,
        langId: Int
    ): FirebaseRemoteModel

    suspend fun getModels()
}

abstract class AbstractSetup : ISetup {
    override suspend fun getModels() {
        downloadModels(
            true,
            Locale.getDefault().language,
            "en"
        )
        downloadModels(
            false,
            "de"
        )
    }
}
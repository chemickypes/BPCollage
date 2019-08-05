package com.hooloovoochimico.badpiccollage

import android.graphics.Bitmap
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET


object ImageServiceManager {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.imgflip.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getImages() = retrofit.create(ImageService::class.java).getImages()


    private interface ImageService {
        @GET("get_memes")
        fun getImages(): Call<ImageBean>
    }

}


class ImageVolatileStorage {

    var memeSelected: Bitmap? = null
}
package com.example.safecross

interface IOnLocationListener {
    fun onLocationLoadSuccess(latLngs:List<MyLatlng>)
    fun onLocationLoadFailed(message:String)

}
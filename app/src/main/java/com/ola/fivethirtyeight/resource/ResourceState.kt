package com.ola.fivethirtyeight.resource

sealed class ResourceState<T> {
    class Loading<T> : ResourceState<T>()
    data class Success<T>(val data: T) : ResourceState<T>()
    data class Error<T>(val error:String, val data: T? = null) : ResourceState<T>()
}

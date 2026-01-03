package com.example.smartcloset_frontend.network

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class BaseUrlOverrideInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val override = ServerUrlHolder.overrideBaseUrl
        if (override.isNullOrBlank()) {
            return chain.proceed(chain.request())
        }

        val newBase = override.toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())

        val oldUrl = chain.request().url

        val newUrl = oldUrl.newBuilder()
            .scheme(newBase.scheme)
            .host(newBase.host)
            .port(newBase.port)
            .build()

        val newRequest = chain.request().newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
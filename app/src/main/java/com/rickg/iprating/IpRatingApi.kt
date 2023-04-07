package com.rickg.iprating

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.*
import java.io.IOException

data class CfIpInfo(
    val ip: String,
    val location: String,
    val warp: String,
    val error: String = ""
)

@Serializable
data class IpInfoIpInfo(
    val ip: String? = null,
    val hostname: String? = null,
    val anycast: Boolean = false,
    val city: String? = null,
    val region: String? = null,
    val country: String? = null,
    val loc: String? = null,
    val org: String? = null,
    val postal: String? = null,
    val timezone: String? = null,
    val bogon: Boolean = false
)

@Serializable
data class IpInfoError(
    val status: Int,
    val error: IpInfoErrorMessage
) {
    @Serializable
    data class IpInfoErrorMessage(
        val title: String,
        val message: String
    )
}

data class IpError(
    val message: String
)

sealed class Either<out L, out R> {
    data class Left<out L>(val value: L) : Either<L, Nothing>()
    data class Right<out R>(val value: R) : Either<Nothing, R>()

    val isLeft get() = this is Left<L>
    val isRight get() = this is Right<R>

    fun <T> fold(left: (L) -> T, right: (R) -> T): T = when (this) {
        is Left -> left(value)
        is Right -> right(value)
    }
}

fun CfGetIpInfo(callback: (Either<IpError, CfIpInfo>) -> Unit) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://www.cloudflare.com/cdn-cgi/trace")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val fields = response.body?.string()?.split("\n")
            var ip = ""
            var location = ""
            var warp = ""

            for (field in fields ?: emptyList()) {
                val parts = field.split("=")
                if (parts.size == 2) {
                    when (parts[0]) {
                        "ip" -> ip = parts[1]
                        "loc" -> location = parts[1]
                        "warp" -> warp = parts[1]
                    }
                }
            }

            if (ip.isNotEmpty() && location.isNotEmpty() && warp.isNotEmpty()) {
                callback(Either.Right(CfIpInfo(ip, location, warp)))
            } else {
                callback(Either.Left(IpError("null")))
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            callback(Either.Left(IpError(e.toString())))
        }
    })
}

fun IpInfoGetIpInfo(ipAddress: String, token: String?, callback: (Either<IpError, IpInfoIpInfo>) -> Unit) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://ipinfo.io/$ipAddress?token=$token")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            callback(Either.Left(IpError(e.message ?: "Unknown error")))
        }

        override fun onResponse(call: Call, response: Response) {
            val responseData = response.body?.string()
            if (responseData == null) {
                callback(Either.Left(IpError("Response data error")))
                return
            }
            try {
                val ipinfo = Json.decodeFromString<IpInfoIpInfo>(responseData)
                callback(Either.Right(ipinfo))
            } catch (e: Exception) {
                try {
                    val error = Json.decodeFromString<IpInfoError>(responseData)
                    callback(Either.Left(IpError(error.error.message)))
                } catch (e: Exception) {
                    callback(Either.Left(IpError(e.message ?: "JSON parse error")))
                }
            }
        }
    })
}

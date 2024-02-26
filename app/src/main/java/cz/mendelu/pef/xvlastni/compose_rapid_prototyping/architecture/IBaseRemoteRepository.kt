package cz.mendelu.pef.xvlastni.compose_rapid_prototyping.architecture

import com.google.gson.JsonIOException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface IBaseRemoteRepository {
    suspend fun <T: Any>processResponse(response: suspend () -> Response<T>): CommunitationResult<T> {
        try {
            val responseVal = response.invoke()

            if (responseVal.isSuccessful) {
                if (responseVal.body() != null) {
                    //vsecko ok
                    return CommunitationResult.Success(responseVal.body()!!)
                }
                else {
                    //neco je nahovno
                    return CommunitationResult.Error(
                        Error(
                            responseVal.code(),
                            responseVal.errorBody().toString()
                        )
                    )
                }
            } else {
                return CommunitationResult.Error(
                    Error(
                        responseVal.code(),
                        responseVal.errorBody().toString()
                    )
                )
            }
        }
        catch (ex: UnknownHostException) {
            print(ex.stackTraceToString())
            return CommunitationResult.CommunicationError()
        }
        catch (ex: SocketTimeoutException) {
            print(ex.stackTraceToString())
            return CommunitationResult.CommunicationError()
        }
        catch (ex: IOException) {
            print(ex.stackTraceToString())
            return CommunitationResult.Exception(ex)
        }
        catch (ex: Exception) {
            print(ex.stackTraceToString())
            return CommunitationResult.Exception(ex)
        }
    }
}
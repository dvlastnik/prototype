package cz.mendelu.pef.xvlastni.prototype.classes

import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

interface IBaseRemoteRepository {
    suspend fun <T: Any>processResponse(response: suspend () -> Response<T>): CommunicationResult<T> {
        try {
            val responseVal = response.invoke()

            if (responseVal.isSuccessful) {
                if (responseVal.body() != null) {
                    //vsecko ok
                    return CommunicationResult.Success(responseVal.body()!!)
                }
                else {
                    //neco je nahovno
                    return CommunicationResult.Error(
                        Error(
                            responseVal.code(),
                            responseVal.errorBody().toString()
                        )
                    )
                }
            } else {
                return CommunicationResult.Error(
                    Error(
                        responseVal.code(),
                        responseVal.errorBody().toString()
                    )
                )
            }
        }
        catch (ex: UnknownHostException) {
            print(ex.stackTraceToString())
            return CommunicationResult.CommunicationError()
        }
        catch (ex: SocketTimeoutException) {
            print(ex.stackTraceToString())
            return CommunicationResult.CommunicationError()
        }
        catch (ex: IOException) {
            print(ex.stackTraceToString())
            return CommunicationResult.Exception(ex)
        }
        catch (ex: Exception) {
            print(ex.stackTraceToString())
            return CommunicationResult.Exception(ex)
        }
    }
}
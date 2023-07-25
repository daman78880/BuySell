package com.buysell.utils

//introduce state of api||aplicaton_flow
sealed class ApiState{
    object Loading:ApiState()
    class Failure(val msg:Throwable):ApiState()
    class Success(val data:Any):ApiState()
    object Empty:ApiState()
}


package com.example.persistencia.Herramientas

import android.content.Context
import android.util.Log
import com.redsys.tpvvinapplibrary.ErrorResponse
import com.redsys.tpvvinapplibrary.IPaymentResult
import com.redsys.tpvvinapplibrary.ResultResponse
import com.redsys.tpvvinapplibrary.TPVV
import com.redsys.tpvvinapplibrary.TPVVConstants
import java.util.UUID

object RedsysPaymentHelper {

    fun startDirectPayment(
        context: Context,
        amount: Double,
        description: String,
        onSuccess: (ResultResponse) -> Unit,
        onError: (ErrorResponse) -> Unit
    ) {
        val orderCode = System.currentTimeMillis().toString().takeLast(12)

        TPVV.doDirectPayment(
            context,
            orderCode,
            amount,
            TPVVConstants.PAYMENT_TYPE_NORMAL,
            null,  // sin referencia
            description,
            null,  // extraParams
            object : IPaymentResult {
                override fun paymentResultOK(response: ResultResponse) {
                    onSuccess(response)
                }
                override fun paymentResultKO(error: ErrorResponse) {
                    Log.e("REDSYS", "CODE=${error.code} DESC=${error.desc}")
                    onError(error)
                }
            }
        )
    }
}
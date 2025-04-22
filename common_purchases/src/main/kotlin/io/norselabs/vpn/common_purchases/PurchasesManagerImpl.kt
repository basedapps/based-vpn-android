package io.norselabs.vpn.common_purchases

import android.app.Activity
import android.content.Context
import arrow.core.Either
import arrow.core.flatMap
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.logInWith
import com.revenuecat.purchases.models.StoreTransaction
import io.norselabs.vpn.common.activity.ActivityHolder
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import timber.log.Timber

class PurchasesManagerImpl(
  private val apiKey: String,
  private val appContext: Context,
  private val activityHolder: ActivityHolder,
) : PurchasesManager {

  override fun setup() {
    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(
      PurchasesConfiguration.Builder(appContext, apiKey).build(),
    )
  }

  override suspend fun login(userId: String): Either<PurchaseError, String> {
    return suspendCoroutine { continuation ->
      Purchases.sharedInstance.logInWith(
        appUserID = userId,
        onSuccess = { customerInfo, _ ->
          continuation.resume(
            Either.Right(customerInfo.originalAppUserId),
          )
        },
        onError = { error -> continuation.resume(error.toLeft()) },
      )
    }
  }

  override suspend fun getCustomerData(): Either<PurchaseError, CustomerData> {
    return suspendCoroutine { continuation ->
      Purchases.sharedInstance.getCustomerInfo(
        object : ReceiveCustomerInfoCallback {
          override fun onReceived(customerInfo: CustomerInfo) {
            Timber.tag(TAG).d("Customer Info: $customerInfo")
            continuation.resume(
              Either.Right(
                CustomerData(customerInfo.activeSubscriptions.isNotEmpty()),
              ),
            )
          }

          override fun onError(error: PurchasesError) {
            continuation.resume(error.toLeft())
          }
        },
      )
    }
  }

  override suspend fun getOffering(): Either<PurchaseError?, CurrentOffering> {
    return getOfferings()
      .map { offering ->
        CurrentOffering(
          identifier = offering.identifier,
          packages = offering.availablePackages.map { pcg ->
            AvailablePackage(
              identifier = pcg.identifier,
              period = pcg.product.period?.iso8601.orEmpty(),
              localizedPriceString = pcg.product.price.formatted,
            )
          },
        )
      }
  }

  override suspend fun purchaseSubscription(subscriptionId: String): Either<PurchaseError?, StoreTransactionResult> {
    return getOfferings()
      .flatMap { offering ->
        val pcg = offering.availablePackages
          .find { it.identifier == subscriptionId }
        val activity = activityHolder.getActivity()
        if (pcg == null || activity == null) {
          return@flatMap Either.Left(null)
        } else {
          purchase(activity, pcg)
        }
      }
  }

  private suspend fun getOfferings(): Either<PurchaseError?, Offering> {
    return suspendCoroutine { continuation ->
      Purchases.sharedInstance.getOfferingsWith(
        onSuccess = { offerings ->
          continuation.resume(
            offerings.current
              ?.let { Either.Right(it) }
              ?: Either.Left(null),
          )
        },
        onError = { error -> continuation.resume(error.toLeft()) },
      )
    }
  }

  private suspend fun purchase(activity: Activity, pcg: Package): Either<PurchaseError?, StoreTransactionResult> {
    return suspendCoroutine { continuation ->
      Purchases.sharedInstance.purchase(
        purchaseParams = PurchaseParams.Builder(activity, pcg).build(),
        callback = object : PurchaseCallback {
          override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
            val instant = Instant.ofEpochMilli(storeTransaction.purchaseTime)
            val dateFormatter = DateTimeFormatter
              .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
              .withZone(ZoneOffset.UTC)
            val purchaseDate = dateFormatter.format(instant)
            continuation.resume(
              Either.Right(
                StoreTransactionResult(
                  isCancelled = false,
                  transaction = StoreTransaction(
                    productIdentifier = pcg.identifier,
                    purchaseDate = purchaseDate,
                    transactionIdentifier = storeTransaction.orderId.orEmpty(),
                  ),
                ),
              ),
            )
          }

          override fun onError(error: PurchasesError, userCancelled: Boolean) {
            continuation.resume(
              if (userCancelled) {
                Either.Right(StoreTransactionResult(isCancelled = true, transaction = null))
              } else {
                error.toLeft()
              },
            )
          }
        },
      )
    }
  }

  override suspend fun restorePurchases(): Either<PurchaseError, CustomerData> {
    return suspendCoroutine { continuation ->
      Purchases.sharedInstance.restorePurchases(
        object : ReceiveCustomerInfoCallback {
          override fun onReceived(customerInfo: CustomerInfo) {
            continuation.resume(
              Either.Right(
                CustomerData(customerInfo.activeSubscriptions.isNotEmpty()),
              ),
            )
          }

          override fun onError(error: PurchasesError) {
            continuation.resume(error.toLeft())
          }
        },
      )
    }
  }

  private fun PurchasesError.toLeft(): Either.Left<PurchaseError> {
    val code = this.code.code
    val description = this.code.description
    Timber.tag(TAG).e("Purchases login error: $code – $description")
    return Either.Left(PurchaseError(code, description))
  }

  companion object {
    const val TAG = "PurchasesManager"
  }
}

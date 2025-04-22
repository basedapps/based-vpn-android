package io.norselabs.vpn.common_purchases

import arrow.core.Either

interface PurchasesManager {

  fun setup()
  suspend fun login(userId: String): Either<PurchaseError, String>
  suspend fun getCustomerData(): Either<PurchaseError, CustomerData>
  suspend fun getOffering(): Either<PurchaseError?, CurrentOffering>
  suspend fun purchaseSubscription(subscriptionId: String): Either<PurchaseError?, StoreTransactionResult>
  suspend fun restorePurchases(): Either<PurchaseError, CustomerData>
}

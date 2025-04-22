package io.norselabs.vpn.common_purchases

import arrow.core.Either

class PurchasesManagerStub : PurchasesManager {

  override fun setup() {}

  override suspend fun login(userId: String): Either<PurchaseError, String> {
    return Either.Right("hello")
  }

  override suspend fun getCustomerData(): Either<PurchaseError, CustomerData> {
    return Either.Right(
      CustomerData(
        isSubscribed = true,
      ),
    )
  }

  override suspend fun getOffering(): Either<PurchaseError?, CurrentOffering> {
    return Either.Right(
      CurrentOffering(
        identifier = "",
        packages = emptyList(),
      ),
    )
  }

  override suspend fun purchaseSubscription(subscriptionId: String): Either<PurchaseError?, StoreTransactionResult> {
    return Either.Right(
      StoreTransactionResult(
        isCancelled = false,
        transaction = null,
      ),
    )
  }

  override suspend fun restorePurchases(): Either<PurchaseError, CustomerData> {
    return Either.Right(
      CustomerData(
        isSubscribed = true,
      ),
    )
  }
}

package io.norselabs.vpn.common_purchases

data class CurrentOffering(
  val identifier: String,
  val packages: List<AvailablePackage>,
)

data class AvailablePackage(
  val identifier: String,
  val period: String,
  val localizedPriceString: String,
)

data class PurchaseError(
  val code: Int,
  val description: String,
)

data class StoreTransactionResult(
  val isCancelled: Boolean,
  val transaction: StoreTransaction?,
)

data class StoreTransaction(
  val productIdentifier: String,
  val purchaseDate: String,
  val transactionIdentifier: String,
)

data class CustomerData(
  val isSubscribed: Boolean,
)

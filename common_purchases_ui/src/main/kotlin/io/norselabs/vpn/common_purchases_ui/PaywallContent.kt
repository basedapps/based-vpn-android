package io.norselabs.vpn.common_purchases_ui

import androidx.compose.runtime.Composable
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

@Composable
fun PaywallContent(
  entitlementId: String,
  onPurchaseCompleted: () -> Unit,
  onPurchaseError: (error: String) -> Unit,
  onPurchaseCancelled: () -> Unit,
  onRestoreCompleted: (isSubscribed: Boolean) -> Unit,
  onRestoreError: (error: String) -> Unit,
  onDismiss: () -> Unit,
) {
  PaywallDialog(
    PaywallDialogOptions.Builder()
      .setRequiredEntitlementIdentifier(entitlementId)
      .setDismissRequest(onDismiss)
      .setListener(
        object : PaywallListener {
          override fun onPurchaseCompleted(
            customerInfo: CustomerInfo,
            storeTransaction: StoreTransaction,
          ) {
            onPurchaseCompleted()
          }

          override fun onPurchaseError(error: PurchasesError) {
            onPurchaseError(error.message)
          }

          override fun onPurchaseCancelled() {
            onPurchaseCancelled()
          }

          override fun onRestoreCompleted(customerInfo: CustomerInfo) {
            onRestoreCompleted(customerInfo.activeSubscriptions.isNotEmpty())
          }

          override fun onRestoreError(error: PurchasesError) {
            onRestoreError(error.message)
          }
        },
      )
      .build(),
  )
}

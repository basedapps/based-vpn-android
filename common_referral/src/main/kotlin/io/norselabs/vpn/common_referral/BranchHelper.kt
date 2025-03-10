package io.norselabs.vpn.common_referral

import android.app.Activity
import android.content.Context
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.LinkProperties
import timber.log.Timber

class BranchHelper(
  private val metadataCallback: (Map<String, String>) -> Unit,
) {

  companion object {

    fun setup(appContext: Context) {
      Branch.enableLogging()
      Branch.getAutoInstance(appContext)
    }

    fun shareReferralLink(
      activity: Activity,
      canonicalIdentifier: String,
      customMetadata: Map<String, String> = emptyMap(),
      sharingTitle: String = "",
      sharingSubject: String = "",
    ) {
      val buo = BranchUniversalObject()
        .setCanonicalIdentifier(canonicalIdentifier)
      customMetadata.forEach { data ->
        buo.contentMetadata.customMetadata[data.key] = data.value
      }
      Branch.getInstance().share(
        /* activity = */ activity,
        /* buo = */ buo,
        /* linkProperties = */ LinkProperties(),
        /* callback = */ null,
        /* title = */ sharingTitle,
        /* subject = */ sharingSubject,
      )
    }
  }

  fun initializeBranch(activity: Activity) {
    logD("branch init")
    Branch.sessionBuilder(activity).withCallback { branchUniversalObject, _, error ->
      if (error != null) {
        logE("branch init failed. Caused by ${error.message}")
      } else {
        logD("branch init complete!")
        if (branchUniversalObject != null) {
          metadataCallback(branchUniversalObject.contentMetadata.customMetadata)
        }
      }
    }.withData(activity.intent.data).init()
  }

  private fun logD(message: String?) = Timber.tag("Referral").d(message)
  private fun logE(message: String?) = Timber.tag("Referral").e(message)
}

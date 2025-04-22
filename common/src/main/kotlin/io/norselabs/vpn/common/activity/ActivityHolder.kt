package io.norselabs.vpn.common.activity

import android.app.Activity
import java.lang.ref.WeakReference

class ActivityHolder {

  private var activityRef: WeakReference<Activity> = WeakReference(null)

  fun setActivity(activity: Activity) {
    activityRef = WeakReference(activity)
  }

  fun getActivity(): Activity? {
    return activityRef.get()
  }
}

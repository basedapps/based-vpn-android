package io.norselabs.vpn.common.permissions

import android.content.SharedPreferences
import io.norselabs.vpn.common.preferences.delegate
import io.norselabs.vpn.common.preferences.getValue
import io.norselabs.vpn.common.preferences.setValue

class NotificationPromptStorage(
  prefs: SharedPreferences,
  key: String = DEFAULT_KEY,
) {

  var wasPromptShown: Boolean by prefs.delegate(key, false)

  private companion object {
    const val DEFAULT_KEY = "notification_prompt_shown"
  }
}

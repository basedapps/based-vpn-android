package com.v2ray.ang

import android.content.Context
import com.tencent.mmkv.MMKV

object V2RayInitializer {

  fun init(appContext: Context) {
    MMKV.initialize(appContext)
  }
}

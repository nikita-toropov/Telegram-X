package org.thunderdog.challegram.util.samsung;

import androidx.annotation.CheckResult;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

public final class SemFloatingFeature {
  private static final SemFloatingFeature INSTANCE = new SemFloatingFeature();

  private static final String DEFAULT_STRING_VALUE = "";
  private static final boolean DEFAULT_BOOLEAN_VALUE = false;

  private @Nullable Method getInstance;
  private @Nullable Method getString;
  private @Nullable Method getBoolean;

  private SemFloatingFeature () {
    try {
      Class<?> clazz = Class.forName("com.samsung.android.feature.SemFloatingFeature");
      getInstance = clazz.getMethod("getInstance");
      getString = clazz.getMethod("getString", String.class);
      getBoolean = clazz.getMethod("getBoolean", String.class);
    } catch (Throwable ignored) {
    }
  }

  @CheckResult
  public static SemFloatingFeature getInstance () {
    return INSTANCE;
  }

  @CheckResult
  public String getString (String key) {
    if (key == null || getInstance == null || getString == null) return DEFAULT_STRING_VALUE;
    try {
      Object instance = getInstance.invoke(null);
      String value = (String) getString.invoke(instance, key);
      return value != null ? value : DEFAULT_STRING_VALUE;
    } catch (Throwable ignored) {
      return DEFAULT_STRING_VALUE;
    }
  }

  @CheckResult
  public boolean getBoolean (String key) {
    if (key == null || getInstance == null || getBoolean == null) return DEFAULT_BOOLEAN_VALUE;
    try {
      Object instance = getInstance.invoke(null);
      Boolean value = (Boolean) getBoolean.invoke(instance, key);
      return value != null ? value : DEFAULT_BOOLEAN_VALUE;
    } catch (Throwable ignored) {
      return DEFAULT_BOOLEAN_VALUE;
    }
  }
}

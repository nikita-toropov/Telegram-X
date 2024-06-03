package org.thunderdog.challegram.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;

import androidx.annotation.CheckResult;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.thunderdog.challegram.config.Device;
import org.thunderdog.challegram.tool.Screen;
import org.thunderdog.challegram.util.samsung.SemFloatingFeature;

public final class PunchHole {
  private final Context context;
  private final PointF position = new PointF();
  private final PointF size = new PointF();
  private @Nullable String type = null;

  public PunchHole (Context context) {
    this.context = context;
  }

  public void reset () {
    position.set(Float.NaN, Float.NaN);
    size.set(Float.NaN, Float.NaN);
    type = null;
  }

  @CheckResult
  public boolean initialize () {
    reset();
    SemFloatingFeature features = SemFloatingFeature.getInstance();
    String punchHoleViInfo = features.getString("SEC_FLOATING_FEATURE_LOCKSCREEN_CONFIG_PUNCHHOLE_VI");
    if (TextUtils.isEmpty(punchHoleViInfo)) {
      return false;
    }
    boolean isSupportDualDisplay = features.getBoolean("SEC_FLOATING_FEATURE_FRAMEWORK_SUPPORT_FOLDABLE_TYPE_FOLD");
    if (isSupportDualDisplay) {
      return false;
    }
    String[] properties = TextUtils.split(punchHoleViInfo, ",");
    for (String property : properties) {
      if (property.startsWith("pos:")) {
        String[] split = TextUtils.split(property, ":");
        if (split.length != 3) {
          return false;
        }
        try {
          position.set(Float.parseFloat(split[1]), Float.parseFloat(split[2]));
        } catch (NumberFormatException ignored) {
          return false;
        }
      } else if (property.startsWith("size:")) {
        String[] split = TextUtils.split(property, ":");
        if (split.length != 3) {
          return false;
        }
        try {
          size.set(Float.parseFloat(split[1]), Float.parseFloat(split[2]));
        } catch (NumberFormatException ignored) {
          return false;
        }
      } else if (property.startsWith("type:")) {
        String[] split = TextUtils.split(property, ":");
        if (split.length < 2) {
          return false;
        }
        type = split[1];
      }
    }
    return hasPosition() && hasSize() && hasType();
  }

  @CheckResult
  public boolean hasPosition () {
    return !Float.isNaN(position.x) && !Float.isNaN(position.y);
  }

  @CheckResult
  public boolean hasSize () {
    return !Float.isNaN(size.x) && !Float.isNaN(size.y);
  }

  @CheckResult
  public boolean hasType () {
    return !TextUtils.isEmpty(type);
  }

  @CheckResult
  public boolean isCircle () {
    return "circle".equals(type);
  }

  /** @noinspection unused*/
  @CheckResult
  public Rect getCircleRect () {
    Rect rect = new Rect();
    getCircleRect(rect);
    return rect;
  }

  public void getCircleRect (Rect outRect) {
    if (!isCircle() || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      outRect.setEmpty();
      return;
    }
    Display display = ContextCompat.getDisplayOrDefault(context);
    android.view.DisplayCutout cutout = display.getCutout();
    if (cutout == null) {
      outRect.setEmpty();
      return;
    }
    if (hasType() && hasPosition()) {
      Point realSize = new Point();
      display.getRealSize(realSize);
      switch (display.getRotation()) {
        case Surface.ROTATION_0: {
          final int centerX = (int) (realSize.x * position.x);
          final int centerY = (int) (realSize.y * position.y);
          final int radius = Math.round(Math.min(realSize.x * size.x, realSize.y * size.y) / 2f);
          outRect.set(centerX, centerY, centerX, centerY);
          outRect.inset(-radius, -radius);
          Rect bounds = cutout.getBoundingRectTop();
          if (Math.abs(outRect.centerX() - bounds.centerX()) > 1) {
            outRect.setEmpty();
          } else if (!bounds.isEmpty() && bounds.width() < outRect.width()) {
            int inset = (outRect.width() - bounds.width()) / 2;
            outRect.inset(inset, inset);
          }
          break;
        }
        case Surface.ROTATION_90: {
          final float width = realSize.y * size.x;
          final float height = realSize.x * size.y;
          outRect.left = (int) (realSize.x * position.y - width / 2f);
          outRect.top = (int) (realSize.y * (1.0f - position.x) - height / 2f);
          outRect.right = (int) (outRect.left + width);
          outRect.bottom = (int) (outRect.top + height);
          Rect bounds = cutout.getBoundingRectLeft();
          if (Math.abs(outRect.centerY() - bounds.centerY()) > 1) {
            outRect.setEmpty();
          } else if (!bounds.isEmpty() && bounds.height() < outRect.height()) {
            int inset = (outRect.height() - bounds.height()) / 2;
            outRect.inset(inset, inset);
          }
          break;
        }
        case Surface.ROTATION_270: {
          final float width = realSize.y * size.x;
          final float height = realSize.x * size.y;
          outRect.left = (int) (realSize.x * (1.0f - position.y) - width / 2f);
          outRect.top = (int) (realSize.y * position.x - height / 2f);
          outRect.right = (int) (outRect.left + width);
          outRect.bottom = (int) (outRect.top + height);
          Rect bounds = cutout.getBoundingRectRight();
          if (Math.abs(outRect.centerY() - bounds.centerY()) > 1) {
            outRect.setEmpty();
          } else if (!bounds.isEmpty() && bounds.height() < outRect.height()) {
            int inset = (outRect.height() - bounds.height()) / 2;
            outRect.inset(inset, inset);
          }
          break;
        }
        case Surface.ROTATION_180: {
          outRect.setEmpty();
          break;
        }
      }
    } else {
      int cameraMargin = Screen.getStatusBarCameraTopMargin();
      if (cameraMargin < 0) {
        outRect.setEmpty();
        return;
      }
      switch (display.getRotation()) {
        case Surface.ROTATION_0: {
          outRect.set(cutout.getBoundingRectTop());
          if (!outRect.isEmpty()) {
            outRect.top += cameraMargin;
            outRect.bottom = outRect.top + outRect.width();
          }
          break;
        }
        case Surface.ROTATION_90: {
          outRect.set(cutout.getBoundingRectLeft());
          if (!outRect.isEmpty()) {
            outRect.left = cameraMargin;
            outRect.right = outRect.left + outRect.height();
          }
          break;
        }
        case Surface.ROTATION_270: {
          outRect.set(cutout.getBoundingRectRight());
          if (!outRect.isEmpty()) {
            outRect.right -= cameraMargin;
            outRect.left = outRect.right - outRect.height();
          }
          break;
        }
        case Surface.ROTATION_180: {
          outRect.setEmpty();
          break;
        }
      }
    }
  }

  @CheckResult
  public static boolean isCircle (Context context) {
    if (!Device.IS_SAMSUNG || Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
      return false;
    }
    PunchHole punchHole = new PunchHole(context);
    return punchHole.initialize() && punchHole.isCircle();
  }
}
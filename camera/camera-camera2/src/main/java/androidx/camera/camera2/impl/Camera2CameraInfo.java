/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.camera2.impl;

import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraInfoInternal;
import androidx.camera.core.CameraOrientationUtil;
import androidx.camera.core.ImageOutputConfig.RotationValue;
import androidx.camera.core.LensFacing;
import androidx.core.util.Preconditions;
import androidx.lifecycle.LiveData;

/**
 * Implementation of the {@link CameraInfoInternal} interface that exposes parameters through
 * camera2.
 */
final class Camera2CameraInfo implements CameraInfoInternal {

    private static final String TAG = "Camera2CameraInfo";
    private final CameraCharacteristics mCameraCharacteristics;
    private final ZoomControl mZoomControl;

    Camera2CameraInfo(@NonNull CameraCharacteristics cameraCharacteristics,
            @NonNull ZoomControl zoomControl) {
        Preconditions.checkNotNull(cameraCharacteristics, "Camera characteristics map is missing");
        mCameraCharacteristics = cameraCharacteristics;
        mZoomControl = zoomControl;

        logDeviceInfo();
    }

    @Nullable
    @Override
    public Integer getLensFacing() {
        Integer lensFacing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
        Preconditions.checkNotNull(lensFacing);
        switch (lensFacing) {
            case CameraCharacteristics.LENS_FACING_FRONT:
                return LensFacing.FRONT;
            case CameraCharacteristics.LENS_FACING_BACK:
                return LensFacing.BACK;
            default:
                return null;
        }
    }

    @Override
    public int getSensorRotationDegrees(@RotationValue int relativeRotation) {
        Integer sensorOrientation = getSensorOrientation();
        int relativeRotationDegrees =
                CameraOrientationUtil.surfaceRotationToDegrees(relativeRotation);
        // Currently this assumes that a back-facing camera is always opposite to the screen.
        // This may not be the case for all devices, so in the future we may need to handle that
        // scenario.
        final Integer lensFacing = getLensFacing();
        boolean isOppositeFacingScreen = (lensFacing != null && LensFacing.BACK == lensFacing);
        return CameraOrientationUtil.getRelativeImageRotation(
                relativeRotationDegrees,
                sensorOrientation,
                isOppositeFacingScreen);
    }

    int getSensorOrientation() {
        Integer sensorOrientation =
                mCameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Preconditions.checkNotNull(sensorOrientation);
        return sensorOrientation;
    }

    int getSupportedHardwareLevel() {
        Integer deviceLevel =
                mCameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        Preconditions.checkNotNull(deviceLevel);
        return deviceLevel;
    }

    @Override
    public int getSensorRotationDegrees() {
        return getSensorRotationDegrees(Surface.ROTATION_0);
    }

    private void logDeviceInfo() {
        // Extend by adding logging here as needed.
        logDeviceLevel();
    }

    private void logDeviceLevel() {
        String levelString;

        int deviceLevel = getSupportedHardwareLevel();
        switch (deviceLevel) {
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                levelString = "INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY";
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL:
                levelString = "INFO_SUPPORTED_HARDWARE_LEVEL_EXTERNAL";
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                levelString = "INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED";
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                levelString = "INFO_SUPPORTED_HARDWARE_LEVEL_FULL";
                break;
            case CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                levelString = "INFO_SUPPORTED_HARDWARE_LEVEL_3";
                break;
            default:
                levelString = "Unknown value: " + deviceLevel;
                break;
        }
        Log.i(TAG, "Device Level: " + levelString);
    }

    @Override
    public boolean hasFlashUnit() {
        Boolean hasFlashUnit = mCameraCharacteristics.get(
                CameraCharacteristics.FLASH_INFO_AVAILABLE);
        Preconditions.checkNotNull(hasFlashUnit);
        return hasFlashUnit;
    }

    @NonNull
    @Override
    public LiveData<Integer> getTorchState() {
        // TODO(b/143514107): implement #getTorchState and return a functional LiveData
        throw new UnsupportedOperationException("Not implement");
    }

    @NonNull
    @Override
    public LiveData<Float> getZoomRatio() {
        return mZoomControl.getZoomRatio();
    }

    @NonNull
    @Override
    public LiveData<Float> getMaxZoomRatio() {
        return mZoomControl.getMaxZoomRatio();
    }

    @NonNull
    @Override
    public LiveData<Float> getMinZoomRatio() {
        return mZoomControl.getMinZoomRatio();
    }

    @NonNull
    @Override
    public LiveData<Float> getLinearZoom() {
        return mZoomControl.getLinearZoom();
    }
}

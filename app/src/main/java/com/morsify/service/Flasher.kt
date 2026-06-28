package com.morsify.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log

/**
 * Wraps the camera flashlight using CameraManager#setTorchMode.
 * Does NOT require opening a Camera device or holding a Camera session.
 *
 * Note: torch is only available on devices that have a back camera with FLASH_UNIT_READY
 * support. On unsupported devices the [on]/[off] calls become no-ops (the transmitter
 * still continues, just silently for the flash part).
 */
class Flasher(private val context: Context) {

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var backCameraId: String? = findBackCameraWithFlash()

    val isAvailable: Boolean get() = backCameraId != null

    private val lock = Any()
    @Volatile
    private var currentOn: Boolean = false

    val isOn: Boolean get() = currentOn

    @Throws(CameraAccessException::class)
    fun on() {
        val id = backCameraId ?: return
        synchronized(lock) {
            try {
                cameraManager?.setTorchMode(id, true)
                currentOn = true
            } catch (e: CameraAccessException) {
                Log.w(TAG, "Torch on failed: ${e.message}")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Torch on failed: ${e.message}")
            }
        }
    }

    @Throws(CameraAccessException::class)
    fun off() {
        val id = backCameraId ?: return
        synchronized(lock) {
            try {
                cameraManager?.setTorchMode(id, false)
                currentOn = false
            } catch (e: CameraAccessException) {
                Log.w(TAG, "Torch off failed: ${e.message}")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Torch off failed: ${e.message}")
            }
        }
    }

    /** Forcibly release the torch. Safe to call from any state. */
    fun release() {
        synchronized(lock) {
            if (currentOn) off()
        }
    }

    private fun findBackCameraWithFlash(): String? {
        val mgr = cameraManager ?: return null
        return try {
            mgr.cameraIdList.firstOrNull { id ->
                val c = mgr.getCameraCharacteristics(id)
                val facing = c.get(CameraCharacteristics.LENS_FACING)
                val hasFlash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                facing == CameraCharacteristics.LENS_FACING_BACK && hasFlash
            }
        } catch (e: CameraAccessException) {
            null
        }
    }

    companion object {
        private const val TAG = "Morsify.Flasher"
    }
}

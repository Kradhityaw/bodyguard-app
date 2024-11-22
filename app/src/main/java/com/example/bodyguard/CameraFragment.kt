package com.example.bodyguard

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.example.bodyguard.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class CameraFragment : Fragment() {
    lateinit var bind: FragmentCameraBinding
    // Deklarasi variabel untuk tampilan dan kamera
    private lateinit var preview: PreviewView
    // Deklarasi variabel untuk fitur kamera
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var mediaPlayer: MediaPlayer? = null

    private var leftEyeClosedStartTime: Long = 0 // Untuk hitung berapa lama mata kiri tertutup
    private var rightEyeClosedStartTime: Long = 0 // Untuk hitung berapa lama mata kanan tertutup
    private var EYE_CLOSED_THRESHOLD: Long = 300 // 0.5 detik

    @OptIn(ExperimentalGetImage::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        bind = FragmentCameraBinding.inflate(layoutInflater, container, false)

        bind.cameraToolbar.setNavigationOnClickListener {
            fragmentManager?.popBackStack()
        }

        preview = bind.main
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.alarm)

        cameraProviderFuture.addListener(kotlinx.coroutines.Runnable {
            val cameraProvider = cameraProviderFuture.get()
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(preview.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
                startImageAnalysis()
            } catch (e: Exception) {
                Log.e("CameraX", "Error binding camera", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))

        return bind.root
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun startImageAnalysis() {
        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), { imageProxy ->
                    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

                        // Panggil deteksi wajah
                        detectFace(image, imageProxy)
                    }
                })
            }

        // Set to front camera
        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        try {
            cameraProviderFuture.get().bindToLifecycle(
                this, cameraSelector, imageAnalyzer
            )
        } catch (e: Exception) {
            Log.e("CameraX", "Error binding image analysis", e)
        }
    }

    // Fungsi untuk deteksi wajah
    private fun detectFace(image: InputImage, imageProxy: ImageProxy) {
        // Konfigurasi deteksi wajah
        val options = FaceDetectorOptions.Builder()
            // akurasi saat deteksi wajah
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            // deteksi wajah dan landmark
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            // deteksi ekspresi wajah
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()

        val detector = FaceDetection.getClient(options)

        detector.process(image)
            .addOnSuccessListener { faces ->
                for (face in faces) {
                    val leftEyeOpenProbability = face.leftEyeOpenProbability
                    val rightEyeOpenProbability = face.rightEyeOpenProbability

                    val currentTime = System.currentTimeMillis()

                    // Jika mata kiri tertutup
                    if (leftEyeOpenProbability != null && leftEyeOpenProbability < 0.3) {
                        if (leftEyeClosedStartTime == 0L) {
                            leftEyeClosedStartTime = currentTime
                        }
                    } else {
                        leftEyeClosedStartTime = 0L // Mata terbuka reset
                    }

                    // Jika mata kanan tertutup
                    if (rightEyeOpenProbability != null && rightEyeOpenProbability < 0.3) {
                        if (rightEyeClosedStartTime == 0L) {
                            rightEyeClosedStartTime = currentTime
                        }
                    } else {
                        rightEyeClosedStartTime = 0L // Mata terbuka reset
                    }

                    // periksa mata tertutup lebih dari 1 detik
                    if (leftEyeClosedStartTime != 0L && rightEyeClosedStartTime != 0L) {
                        val leftEyeClosedDuration = currentTime - leftEyeClosedStartTime
                        val rightEyeClosedDuration = currentTime - rightEyeClosedStartTime

                        if (leftEyeClosedDuration > EYE_CLOSED_THRESHOLD && rightEyeClosedDuration > EYE_CLOSED_THRESHOLD) {
                            Log.d("DriverMonitoring", "Pengemudi mengantuk!")
                            // Trigger alert di sini
//                            triggerVibration()
                            triggerSound()
                        }
                    }

                    if (leftEyeOpenProbability != null) {
                        if (rightEyeOpenProbability != null) {
                            if (leftEyeOpenProbability >= 0.3 && rightEyeOpenProbability >= 0.3) {
                                stopSound()
                            }
                        }
                    }

//                    if (leftEyeOpenProbability != null && rightEyeOpenProbability != null) {
//                        if (leftEyeOpenProbability < 0.3 && rightEyeOpenProbability < 0.3) {
//                            Log.d("DriverMonitoring", "Pengemudi kemungkinan mengantuk!")
//                            // Trigger alert di sini
//                            triggerVibration()
//                        }
//                    }

                }
                // Tutup imageProxy setelah deteksi wajah selesai
                imageProxy.close()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Gagal deteksi wajah", Toast.LENGTH_SHORT).show()
                Log.e("DriverMonitoring", "Deteksi wajah gagal", e)

                // Tutup imageProxy meskipun terjadi kegagalan
                imageProxy.close()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Fungsi untuk getar hp
    private fun triggerVibration() {
        // Dapatkan instance Vibrator dari sistem
        val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        // Jika versi Android cukup baru, gunakan metode vibrate(long)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
    }

    private fun triggerSound() {
        mediaPlayer?.let {
            if (!it.isPlaying) it.start()
        }
    }

    private fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                it.seekTo(0)
            }
        }
    }
}
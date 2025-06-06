package com.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.module.annotations.ReactModule
import com.luck.lib.camerax.CameraImageEngine
import com.luck.lib.camerax.SimpleCameraX
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.config.SelectModeConfig
import com.luck.picture.lib.engine.CropEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnCameraInterceptListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.manager.PictureCacheManager
import com.luck.picture.lib.utils.SdkVersionUtils
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.Locale
import java.util.UUID


@ReactModule(name = ImagePickerModule.NAME)
class ImagePickerModule(reactContext: ReactApplicationContext) :
  NativeImagePickerSpec(reactContext) {

  override fun getName(): String {
    return NAME
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = "ImagePicker"
  }

  private val sySelectImageFailedCode = "0"
  private var selectList: MutableList<LocalMedia>? = ArrayList()
  private var mPickerCallback: Callback? = null

  private var mPickerPromise: Promise? = null

  private var cameraOptions: ReadableMap? = null

  private val mActivityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(
      activity: Activity,
      requestCode: Int,
      resultCode: Int,
      data: Intent?
    ) {
      if (resultCode == -1) {
        if (requestCode == PictureConfig.CHOOSE_REQUEST) {
          Thread {
            if (data != null) {
              onGetResult(data)
            }
          }.start()
        } else if (requestCode == PictureConfig.REQUEST_CAMERA) {
          if (data != null) {
            onGetVideoResult(data)
          }
        }
      } else {
        invokeError(resultCode)
      }
    }
  }

  init {
    reactContext.addActivityEventListener(mActivityEventListener)
  }
  private fun onGetResult(data: Intent) {
    val tmpSelectList: List<LocalMedia> = PictureSelector.obtainSelectorList(data)
    val options = cameraOptions
    if (options != null) {
      val isRecordSelected: Boolean = options.getBoolean("isRecordSelected")
      if (tmpSelectList.isNotEmpty() && isRecordSelected) {
        selectList = tmpSelectList.toMutableList()
      }

      val imageList: WritableArray = WritableNativeArray()
      val enableBase64: Boolean = options.getBoolean("enableBase64")

      for (media in tmpSelectList) {
        imageList.pushMap(getImageResult(media, enableBase64))
      }
      invokeSuccessWithResult(imageList)
    }
  }
  private fun onGetResult(result: ArrayList<LocalMedia>) {
    val options = cameraOptions
    if (options != null) {
      val isRecordSelected: Boolean = options.getBoolean("isRecordSelected")
      if (result.isNotEmpty() && isRecordSelected) {
        selectList = result.toMutableList()
      }

      val imageList: WritableArray = WritableNativeArray()
      val enableBase64: Boolean = options.getBoolean("enableBase64")

      for (media in result) {
        imageList.pushMap(getImageResult(media, enableBase64))
      }

      invokeSuccessWithResult(imageList)
    }
  }
  private fun onGetVideoResult(data: Intent) {
    val mVideoSelectList: List<LocalMedia> = PictureSelector.obtainSelectorList(data)
    val options = cameraOptions
    if (options != null) {
      val isRecordSelected: Boolean = options.getBoolean("isRecordSelected")
      if (mVideoSelectList.isNotEmpty() && isRecordSelected) {
        selectList = mVideoSelectList.toMutableList()
      }
      val videoList: WritableArray = WritableNativeArray()

      for (media in mVideoSelectList) {
        if (TextUtils.isEmpty(media.path)) {
          continue
        }

        val videoMap: WritableMap = WritableNativeMap()

        val isAndroidQ: Boolean = SdkVersionUtils.isQ()
        val isAndroidR: Boolean = SdkVersionUtils.isR()
        var filePath = media.path
        if (isAndroidQ) {
          filePath = media.path
        }
        if (isAndroidR) {
          filePath = media.realPath
        }

        videoMap.putString("uri", "file://$filePath")
        videoMap.putString("coverUri", "file://" + this.getVideoCover(filePath))
        videoMap.putString("fileName", File(media.path).name)
        videoMap.putDouble("size", File(media.path).length().toDouble())
        videoMap.putDouble("duration", media.duration / 1000.00)
        videoMap.putInt("width", media.width)
        videoMap.putInt("height", media.height)
        videoMap.putString("type", "video")
        videoMap.putString("mime", media.mimeType)
        videoList.pushMap(videoMap)
      }

      invokeSuccessWithResult(videoList)
    }
  }
  private fun invokeError(resultCode: Int) {
    var message = "取消"
    if (resultCode != 0) {
      message = resultCode.toString()
    }
    val callback = this.mPickerCallback
    val promise = this.mPickerPromise
    when {
      callback != null -> {
        callback.invoke(message)
        mPickerCallback = null
      }
      promise != null -> {
        promise.reject(sySelectImageFailedCode, message)
      }
    }

  }
  private fun getImageResult(media: LocalMedia, enableBase64: Boolean): WritableMap {
    val imageMap: WritableMap = WritableNativeMap()
    var path = media.path

    if (media.isCompressed || media.isCut) {
      path = media.compressPath
    }

    if (media.isCut) {
      path = media.cutPath
    }
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(path, options)
    imageMap.putDouble("width", options.outWidth.toDouble())
    imageMap.putDouble("height", options.outHeight.toDouble())
    imageMap.putString("type", "image")
    imageMap.putString("uri", path)
    imageMap.putString("original_uri", media.realPath)
    imageMap.putInt("size", File(path).length().toInt())

    if (enableBase64) {
      val encodeString: String = getBase64StringFromFile(media.realPath) ?: error("读取文件失败: $media.realPath")
      imageMap.putString("base64", encodeString)
    }

    return imageMap
  }
  private fun invokeSuccessWithResult(imageList: WritableArray) {
    val callback = this.mPickerCallback
    val promise = this.mPickerPromise
    when {
      callback != null -> {
        callback.invoke(null, imageList)
        mPickerCallback = null
      }
      promise != null -> {
        promise.resolve(imageList)
      }
    }
  }
  private fun getBase64StringFromFile(absoluteFilePath: String): String? {
    val inputStream: InputStream
    try {
      inputStream = FileInputStream(File(absoluteFilePath))
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
      return null
    }

    val bytes: ByteArray
    val buffer = ByteArray(8192)
    var bytesRead: Int
    val output = ByteArrayOutputStream()
    try {
      while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
        output.write(buffer, 0, bytesRead)
      }
    } catch (e: IOException) {
      e.printStackTrace()
    }
    bytes = output.toByteArray()
    if (absoluteFilePath.lowercase(Locale.getDefault()).endsWith("png")) {
      return "data:image/png;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
  }
  private fun getVideoCover(videoPath: String): String? {
    try {
      val retriever = MediaMetadataRetriever()
      retriever.setDataSource(videoPath)
      val bitmap = retriever.frameAtTime
      val outStream: FileOutputStream
      val uuid = "thumb-" + UUID.randomUUID().toString()
      val localThumb: String =
        reactApplicationContext.externalCacheDir?.absolutePath + "/" + uuid + ".jpg"
      outStream = FileOutputStream(File(localThumb))
      bitmap!!.compress(Bitmap.CompressFormat.JPEG, 30, outStream)
      outStream.close()
      retriever.release()

      return localThumb
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    } catch (e: IOException) {
      e.printStackTrace()
    } catch (err: Exception) {
      err.printStackTrace()
    }

    return null
  }
  private fun openImagePicker() {
    val imageCount = cameraOptions!!.getInt("imageCount")
    val isCamera = cameraOptions!!.getBoolean("isCamera")
    val isCrop = cameraOptions!!.getBoolean("isCrop")
    val CropW = cameraOptions!!.getInt("CropW")
    val CropH = cameraOptions!!.getInt("CropH")
    val isGif = cameraOptions!!.getBoolean("isGif")
    val showCropCircle = cameraOptions!!.getBoolean("showCropCircle")
    val showCropFrame = cameraOptions!!.getBoolean("showCropFrame")
    val showCropGrid = cameraOptions!!.getBoolean("showCropGrid")
    val compress = cameraOptions!!.getBoolean("compress")
    val freeStyleCropEnabled = cameraOptions!!.getBoolean("freeStyleCropEnabled")
    val rotateEnabled = cameraOptions!!.getBoolean("rotateEnabled")
    val scaleEnabled = cameraOptions!!.getBoolean("scaleEnabled")
    val minimumCompressSize = cameraOptions!!.getInt("minimumCompressSize")
    val quality = cameraOptions!!.getInt("quality")
    val isWeChatStyle = cameraOptions!!.getBoolean("isWeChatStyle")
    val showSelectedIndex = cameraOptions!!.getBoolean("showSelectedIndex")
    val compressFocusAlpha = cameraOptions!!.getBoolean("compressFocusAlpha")
    val modeValue = if (imageCount == 1) {
      1
    } else {
      2
    }

    val isAndroidQ: Boolean = SdkVersionUtils.isQ()

    val currentActivity = currentActivity
    PictureSelector.create(currentActivity)
      .openGallery(SelectMimeType.ofImage())
      .setImageEngine(GlideEngine.createGlideEngine())
      .setMaxSelectNum(imageCount) // 最大图片选择数量 int
      .setMinSelectNum(0) // 最小选择数量 int
      .setImageSpanCount(4) // 每行显示个数 int
      .setSelectionMode(modeValue) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
      .isPreviewImage(true)
      .isPreviewVideo(false)
      .isPreviewAudio(false)
      .isDisplayCamera(isCamera) // 是否显示拍照按钮 true or false
      .setCameraImageFormat(if (isAndroidQ) PictureMimeType.PNG_Q else PictureMimeType.PNG) // 拍照保存图片格式后缀,默认jpeg
      .isSelectZoomAnim(true)
      .isOriginalControl(true)
      .forResult(PictureConfig.CHOOSE_REQUEST)
  }

  /**
   * 打开相机
   */
  private fun openCamera() {
    val isCrop = cameraOptions!!.getBoolean("isCrop")
    val CropW = cameraOptions!!.getInt("CropW")
    val CropH = cameraOptions!!.getInt("CropH")
    val showCropCircle = cameraOptions!!.getBoolean("showCropCircle")
    val showCropFrame = cameraOptions!!.getBoolean("showCropFrame")
    val showCropGrid = cameraOptions!!.getBoolean("showCropGrid")
    val compress = cameraOptions!!.getBoolean("compress")
    val freeStyleCropEnabled = cameraOptions!!.getBoolean("freeStyleCropEnabled")
    val rotateEnabled = cameraOptions!!.getBoolean("rotateEnabled")
    val scaleEnabled = cameraOptions!!.getBoolean("scaleEnabled")
    val minimumCompressSize = cameraOptions!!.getInt("minimumCompressSize")
    val quality = cameraOptions!!.getInt("quality")
    val isWeChatStyle = cameraOptions!!.getBoolean("isWeChatStyle")
    val showSelectedIndex = cameraOptions!!.getBoolean("showSelectedIndex")
    val compressFocusAlpha = cameraOptions!!.getBoolean("compressFocusAlpha")

    val isAndroidQ: Boolean = SdkVersionUtils.isQ()

    val currentActivity = currentActivity
    PictureSelector.create(currentActivity)
      .openCamera(SelectMimeType.ofImage())
      .forResult(object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: ArrayList<LocalMedia>) {
          // 处理选择结果
          System.out.println(result.size);
          Thread {
            if (result != null) {
              onGetResult(result)
            }
          }.start()
        }

        override fun onCancel() {
          // 处理取消事件
          System.out.println("onCancel")
        }
      })

  }

  /**
   * 拍摄视频
   */
  private fun openVideo() {
    val quality = cameraOptions!!.getInt("quality")
    val MaxSecond = cameraOptions!!.getInt("MaxSecond")
    val MinSecond = cameraOptions!!.getInt("MinSecond")
    val recordVideoSecond = cameraOptions!!.getInt("recordVideoSecond")
    val imageCount = cameraOptions!!.getInt("imageCount")
    val currentActivity = currentActivity
    PictureSelector.create(currentActivity)
      .openCamera(SelectMimeType.ofVideo())
//      .loadImageEngine(GlideEngine.createGlideEngine())
//      .selectionMedia(selectList) // 当前已选中的图片 List
//      .openClickSound(false) // 是否开启点击声音 true or false
//      .maxSelectNum(imageCount) // 最大图片选择数量 int
//      .minSelectNum(0) // 最小选择数量 int
//      .imageSpanCount(4) // 每行显示个数 int
//      .selectionMode(PictureConfig.MULTIPLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
//      .previewVideo(true) // 是否可预览视频 true or false
//      .videoQuality(quality) // 视频录制质量 0 or 1 int
//      .videoMaxSecond(MaxSecond) // 显示多少秒以内的视频or音频也可适用 int
//      .videoMinSecond(MinSecond) // 显示多少秒以内的视频or音频也可适用 int
//      .recordVideoSecond(recordVideoSecond) //视频秒数录制 默认60s int
//      .forResult(PictureConfig.REQUEST_CAMERA) //结果回调onActivityResult code
  }

  /**
   * 选择视频
   */
  private fun openVideoPicker() {
    val quality = cameraOptions!!.getInt("quality")
    val MaxSecond = cameraOptions!!.getInt("MaxSecond")
    val MinSecond = cameraOptions!!.getInt("MinSecond")
    val recordVideoSecond = cameraOptions!!.getInt("recordVideoSecond")
    val videoCount = cameraOptions!!.getInt("imageCount")
    val isCamera = cameraOptions!!.getBoolean("allowTakeVideo")

    val currentActivity = currentActivity
    PictureSelector.create(currentActivity)
      .openGallery(SelectMimeType.ofVideo())
      .setImageEngine(GlideEngine.createGlideEngine())
//      .selectionMedia(selectList) // 当前已选中的视频 List
//      .openClickSound(false) // 是否开启点击声音 true or false
//      .isCamera(isCamera) // 是否显示拍照按钮 true or false
//      .maxSelectNum(videoCount) // 最大视频选择数量 int
//      .minSelectNum(1) // 最小选择数量 int
//      .imageSpanCount(4) // 每行显示个数 int
//      .selectionMode(PictureConfig.MULTIPLE) // 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
//      .previewVideo(true) // 是否可预览视频 true or false
//      .videoQuality(quality) // 视频录制质量 0 or 1 int
//      .videoMaxSecond(MaxSecond) // 显示多少秒以内的视频or音频也可适用 int
//      .videoMinSecond(MinSecond) // 显示多少秒以内的视频or音频也可适用 int
      .setRecordVideoMinSecond(1)
      .setRecordVideoMaxSecond(recordVideoSecond) //视频秒数录制 默认60s int
      .forResult(PictureConfig.REQUEST_CAMERA) //结果回调onActivityResult code
  }
  @ReactMethod
  override fun showImagePicker(options: ReadableMap?, callback: Callback?) {
    this.cameraOptions = options
    this.mPickerPromise = null
    this.mPickerCallback = callback
    this.openImagePicker()
  }

  @ReactMethod
  override fun openCamera(options: ReadableMap?, callback: Callback?) {
    this.cameraOptions = options
    this.mPickerPromise = null
    this.mPickerCallback = callback
    this.openCamera()
  }

  @ReactMethod
  override fun asyncOpenCamera(options: ReadableMap?, promise: Promise?) {
    this.cameraOptions = options
    this.mPickerPromise = promise
    this.mPickerCallback = null
    this.openCamera()
  }
  @ReactMethod
  override fun asyncShowImagePicker(options: ReadableMap?, promise: Promise?) {
    this.cameraOptions = options
    this.mPickerCallback = null
    this.mPickerPromise = promise
    this.openImagePicker()
  }

  @ReactMethod
  override fun deleteCache() {
    val currentActivity = currentActivity
    PictureCacheManager.deleteAllCacheDirFile(currentActivity)
  }

  @ReactMethod
  override fun removePhotoAtIndex(index: Double) {
    val posIndex = index.toInt()
    if (selectList != null && selectList!!.size > posIndex) {
      selectList!!.removeAt(posIndex)
    }
  }

  @ReactMethod
  override fun removeAllPhoto() {
    if (selectList != null) {
      selectList!!.clear();
      selectList = null
    }
  }
  @ReactMethod
  override fun openVideoPicker(options: ReadableMap?, callback: Callback?) {
    this.cameraOptions = options
    this.mPickerPromise = null
    this.mPickerCallback = callback
    this.openVideoPicker()
  }
}

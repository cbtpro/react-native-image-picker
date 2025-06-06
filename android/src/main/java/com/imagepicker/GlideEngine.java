package com.imagepicker;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.luck.picture.lib.engine.ImageEngine;
public class GlideEngine implements ImageEngine {

  private GlideEngine() {
  }

  private static GlideEngine instance;

  public static GlideEngine createGlideEngine() {
    if (null == instance) {
      synchronized (GlideEngine.class) {
        if (null == instance) {
          instance = new GlideEngine();
        }
      }
    }
    return instance;
  }
  @Override
  public void loadImage(@NonNull Context context, @NonNull ImageView imageView,
      @NonNull String url, int width, int height) {
    Glide.with(context)
        .load(url)
        .override(width, height)
        .centerCrop()
        .into(imageView);
  }

  @Override
  public void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
    /**
     * 加载普通图片
     * context：上下文环境
     * url：图片路径（可为本地或网络）
     * imageView：用于显示图片的控件
     */
    Glide.with(context)
        .load(url)
        .into(imageView);
  }

  @Override
  public void loadGridImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
    /**
     * 加载图片网格列表中的图片
     * context：上下文
     * url：图片地址
     * imageView：图片控件
     */
    Glide.with(context)
        .load(url)
        .override(200, 200)
        .centerCrop()
        .apply(new RequestOptions().placeholder(com.luck.picture.lib.R.drawable.ps_ic_placeholder))
        .into(imageView);
  }

  /**
   * 加载相册封面图
   *
   * @param context   上下文
   * @param url       图片路径
   * @param imageView 承载图片的 ImageView
   */
  @Override
  public void loadAlbumCover(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
    Glide.with(context)
        .asBitmap()
        .load(url)
        .override(180, 180)
        .centerCrop()
        .apply(new RequestOptions().placeholder(com.luck.picture.lib.R.drawable.ps_ic_placeholder))
        .into(imageView);
  }

  @Override
  public void pauseRequests(@NonNull Context context) {
    Glide.with(context).pauseRequests();
  }

  @Override
  public void resumeRequests(@NonNull Context context) {
    Glide.with(context).resumeRequests();
  }

}

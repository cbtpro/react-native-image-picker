import { Dimensions } from 'react-native';
import ImagePicker, {
  type ImagePickerOption,
  type SelectedPhoto,
} from './NativeImagePicker';

const { width } = Dimensions.get('window');

export function multiply(a: number, b: number): number {
  return ImagePicker.multiply(a, b);
}

/**
 * 默认参数
 */
const defaultOptions: ImagePickerOption = {
  /* 最大选择图片数目，默认6 */
  imageCount: 6,

  /* 是否已选图片 */
  isRecordSelected: false,

  /* 是否允许用户在内部拍照，默认true */
  isCamera: true,

  /* 是否允许裁剪，默认false, imageCount 为1才生效 */
  isCrop: false,

  /* 裁剪宽度，默认屏幕宽度60% */
  CropW: ~~(width * 0.6),

  /* 裁剪高度，默认屏幕宽度60% */
  CropH: ~~(width * 0.6),

  /* 是否允许选择GIF，默认false，暂无回调GIF数据 */
  isGif: false,

  /* 是否显示圆形裁剪区域，默认false */
  showCropCircle: false,

  /* 圆形裁剪半径，默认屏幕宽度一半 */
  circleCropRadius: ~~(width / 4),

  /* 是否显示裁剪区域，默认true */
  showCropFrame: true,

  /* 是否隐藏裁剪区域网格，默认false */
  showCropGrid: false,

  /* 裁剪框是否可拖拽 */
  freeStyleCropEnabled: false,

  /* 裁剪是否可旋转图片 */
  rotateEnabled: true,

  /* 裁剪是否可放大缩小图片 */
  scaleEnabled: true,

  compress: true,

  /* 压缩png保留通明度 */
  compressFocusAlpha: false,

  /* 小于100kb的图片不压缩 */
  minimumCompressSize: 100,

  /* 压缩质量 */
  quality: 90,

  /* 是否返回base64编码，默认不返回 */
  enableBase64: false,

  allowPickingOriginalPhoto: false,

  /* 可以多选视频/gif/图片，和照片共享最大可选张数maxImagesCount的限制 */
  allowPickingMultipleVideo: false,

  /* 视频最大拍摄时间，默认是10分钟，单位是秒 */
  videoMaximumDuration: 10 * 60,

  /* 是否是微信风格选择界面 Android Only */
  isWeChatStyle: false,

  /* 对照片排序，按修改时间升序，默认是YES。如果设置为NO,最新的照片会显示在最前面，内部的拍照按钮会排在第一个 */
  sortAscendingByModificationDate: true,

  /* 是否显示序号， 默认不显示 */
  showSelectedIndex: false,
};

export const showImagePicker = (
  options: ImagePickerOption,
  callback: (err: null | string, photos: SelectedPhoto[]) => void
) => {
  const optionObj = {
    ...defaultOptions,
    ...options,
  };
  ImagePicker.showImagePicker(optionObj, callback);
};
export const asyncShowImagePicker = (options: ImagePickerOption) => {
  const optionObj = {
    ...defaultOptions,
    ...options,
  };
  return ImagePicker.asyncShowImagePicker(optionObj);
};

export const openCamera = (
  options: ImagePickerOption,
  callback: (err: null | string, photos: Array<SelectedPhoto>) => void
) => {
  const optionObj = {
    ...defaultOptions,
    ...options,
  };
  ImagePicker.openCamera(optionObj, callback);
};

export const asyncOpenCamera = (options: ImagePickerOption) => {
  const optionObj = {
    ...defaultOptions,
    ...options,
  };
  return ImagePicker.asyncOpenCamera(optionObj);
};

export const deleteCache = () => {
  ImagePicker.deleteCache();
};

export const removePhotoAtIndex = (index: number) => {
  ImagePicker.removePhotoAtIndex(index);
};

export const removeAllPhoto = () => {
  ImagePicker.removeAllPhoto();
};

export const openVideoPicker = (
  options: ImagePickerOption,
  callback: (err: null | string, photos: Array<SelectedPhoto>) => void
) => {
  const imageCount = options.videoCount ? options.videoCount : 1;
  const optionObj = {
    ...defaultOptions,
    ...options,
    isCamera: false,
    allowPickingGif: false,
    allowPickingVideo: true,
    allowPickingImage: false,
    allowTakeVideo: true,
    allowPickingMultipleVideo: imageCount > 1,
    videoMaximumDuration: 20,
    MaxSecond: 60,
    MinSecond: 0,
    recordVideoSecond: 60,
    imageCount,
  };
  return ImagePicker.openVideoPicker(optionObj, callback);
};
export default {
  // ...ImagePicker,
  showImagePicker,
  asyncShowImagePicker,
  openCamera,
  asyncOpenCamera,
  openVideoPicker,
  deleteCache,
  removePhotoAtIndex,
  removeAllPhoto,
};

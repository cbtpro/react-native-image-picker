import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';

export interface ImagePickerOption {
  imageCount?: number; // 最大选择图片数目，默认6
  isRecordSelected?: boolean; // 是否已选图片
  isCamera?: boolean; // 是否允许用户在内部拍照，默认true
  isCrop?: boolean; // 是否允许裁剪，默认false, imageCount 为1才生效
  CropW?: number; // 裁剪宽度，默认屏幕宽度60%
  CropH?: number; // 裁剪高度，默认屏幕宽度60%
  isGif?: boolean; // 是否允许选择GIF，默认false，暂无回调GIF数据
  showCropCircle?: boolean; // 是否显示圆形裁剪区域，默认false
  circleCropRadius?: number; // 圆形裁剪半径，默认屏幕宽度一半
  showCropFrame?: boolean; // 是否显示裁剪区域，默认true
  showCropGrid?: boolean; // 是否隐藏裁剪区域网格，默认false
  freeStyleCropEnabled?: boolean; // 裁剪框是否可拖拽
  rotateEnabled?: boolean; // 裁剪是否可旋转图片
  scaleEnabled?: boolean; // 裁剪是否可放大缩小图片
  compress?: boolean;
  compressFocusAlpha?: boolean; //压缩png保留通明度
  minimumCompressSize?: number; // 小于100kb的图片不压缩
  quality?: number; // 压缩质量
  enableBase64?: boolean; // 是否返回base64编码，默认不返回
  recordVideoSecond?: number;
  allowPickingOriginalPhoto?: boolean;
  allowPickingMultipleVideo?: boolean; // 可以多选视频/gif/图片，和照片共享最大可选张数maxImagesCount的限制
  videoMaximumDuration?: number; // 视频最大拍摄时间，默认是10分钟，单位是秒
  isWeChatStyle?: boolean; // 是否是微信风格选择界面 Android Only
  sortAscendingByModificationDate?: boolean; // 对照片排序，按修改时间升序，默认是YES。如果设置为NO,最新的照片会显示在最前面，内部的拍照按钮会排在第一个
  videoCount?: number; // 视频个数
  MaxSecond?: number; // 选择视频最大时长，默认是180秒
  MinSecond?: number; // 选择视频最小时长，默认是1秒
  showSelectedIndex?: boolean; // 是否显示序号， 默认不显示
}
export interface SelectedPhoto {
  width: number; //图片宽度
  height: number; //图片高度
  uri: string; //图片路径
  original_uri: string; //图片原始路径，仅 Android
  type: string; //文件类型，仅 Android，当前只返回 image
  size: number; //图片大小，单位为字节 b
  base64: string; //图片的 base64 编码，如果 enableBase64 设置 false，则不返回该属性
}

export interface Spec extends TurboModule {
  multiply(a: number, b: number): number;
  asyncShowImagePicker(
    options: ImagePickerOption
  ): Promise<Array<SelectedPhoto>>;
  openCamera(
    options: ImagePickerOption,
    callback: (err: null | string, photos: Array<SelectedPhoto>) => void
  ): void;
  asyncOpenCamera(options: ImagePickerOption): Promise<Array<SelectedPhoto>>;
  showImagePicker(
    options: ImagePickerOption,
    callback: (err: null | string, photos: Array<SelectedPhoto>) => void
  ): void;
  openVideoPicker(
    options: ImagePickerOption,
    callback: (err: null | string, photos: Array<SelectedPhoto>) => void
  ): void;

  deleteCache(): void;
  removePhotoAtIndex(index: number): void;
  removeAllPhoto(): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('ImagePicker');

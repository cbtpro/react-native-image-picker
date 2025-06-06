import { useState } from 'react';
import {
  Button,
  Dimensions,
  Image,
  ScrollView,
  Text,
  View,
  StyleSheet,
  PermissionsAndroid,
} from 'react-native';
import ImagePicker, { multiply } from 'react-native-image-picker';
import type { SelectedPhoto } from '../../src/NativeImagePicker';

const { width } = Dimensions.get('window');

const result = multiply(3, 7);

export default function App() {
  const [photos, setPhotos] = useState<SelectedPhoto[]>([]);
  const handleAsyncSelectPhoto = async () => {
    // SYImagePicker.removeAllPhoto()
    try {
      const newPhotos = await ImagePicker.asyncShowImagePicker({
        // allowPickingOriginalPhoto: true,
        imageCount: 8,
        showSelectedIndex: false,
        isGif: true,
        enableBase64: true,
        isRecordSelected: false,
        isCamera: false,
        isCrop: false,
        CropW: 0,
        CropH: 0,
        showCropCircle: false,
        circleCropRadius: 0,
        showCropFrame: false,
        showCropGrid: false,
        freeStyleCropEnabled: false,
        rotateEnabled: false,
        scaleEnabled: false,
        compress: false,
        compressFocusAlpha: false,
        minimumCompressSize: 0,
        quality: 0,
        allowPickingOriginalPhoto: false,
        allowPickingMultipleVideo: false,
        videoMaximumDuration: 0,
        isWeChatStyle: false,
        sortAscendingByModificationDate: false,
        videoCount: 0,
        MaxSecond: 0,
        MinSecond: 0,
      });
      console.log('关闭', photos);
      // 选择成功
      setPhotos([...photos, ...newPhotos]);
    } catch (err) {
      console.log(err);
      // 取消选择，err.message为"取消"
    }
  };

  const requestPermission = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.CAMERA,
        {
          title: '酷炫相机权限请求',
          message:
            '酷炫相机应用需要访问你的相机，' + '以便你可以拍摄精彩的照片。',
          buttonNeutral: '稍后再问我',
          buttonNegative: '取消',
          buttonPositive: '确定',
        }
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        console.log('你可以使用相机了');
      } else {
        console.log('相机权限被拒绝');
      }
    } catch (err) {
      console.warn(err);
    }
  };
  const handleOpenImagePicker = () => {
    ImagePicker.showImagePicker(
      {
        imageCount: 1,
        isRecordSelected: true,
        isCrop: true,
        showCropCircle: true,
        quality: 90,
        compress: true,
        enableBase64: false,
      },
      (err, newPhotos) => {
        console.log('开启', err, newPhotos);
        if (!err) {
          setPhotos(photos);
        } else {
          console.log(err);
        }
      }
    );
  };
  const handlePromiseSelectPhoto = () => {
    ImagePicker.asyncShowImagePicker({
      imageCount: 3,
      isRecordSelected: false,
      isCamera: false,
      isCrop: false,
      CropW: 0,
      CropH: 0,
      isGif: false,
      showCropCircle: false,
      circleCropRadius: 0,
      showCropFrame: false,
      showCropGrid: false,
      freeStyleCropEnabled: false,
      rotateEnabled: false,
      scaleEnabled: false,
      compress: false,
      compressFocusAlpha: false,
      minimumCompressSize: 0,
      quality: 0,
      enableBase64: true,
      allowPickingOriginalPhoto: false,
      allowPickingMultipleVideo: false,
      videoMaximumDuration: 0,
      isWeChatStyle: false,
      sortAscendingByModificationDate: false,
      videoCount: 0,
      MaxSecond: 0,
      MinSecond: 0,
      showSelectedIndex: false,
    })
      .then((newPhotos) => {
        console.log('newPhotos:', newPhotos);
        const arr = newPhotos.map((v) => {
          return v;
        });
        // 选择成功
        setPhotos([...photos, ...arr]);
      })
      .catch((err) => {
        // 取消选择，err.message为"取消"
        console.error(err);
      });
  };
  const handleLaunchCamera = async () => {
    await requestPermission();
    ImagePicker.openCamera(
      {
        isCrop: true,
        showCropCircle: true,
        showCropFrame: false,
      },
      (err, newPhotos) => {
        console.log(err, newPhotos);
        if (!err) {
          setPhotos([...photos, ...newPhotos]);
        }
      }
    );
  };
  const handleDeleteCache = () => {
    ImagePicker.deleteCache();
  };
  const handleOpenVideoPicker = () => {
    ImagePicker.openVideoPicker(
      {
        allowPickingMultipleVideo: true,
      },
      (err, res) => {
        console.log(err, res);
        if (!err) {
          let newPhotos = [...photos];
          res.map((v) => {
            newPhotos.push({ ...v, uri: v.uri });
          });
          setPhotos(newPhotos);
        }
      }
    );
  };
  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <View>
        <Button title={'拍照'} onPress={handleLaunchCamera} />
        <Button title={'开启压缩'} onPress={handleOpenImagePicker} />
        <Button title={'关闭压缩'} onPress={handleAsyncSelectPhoto} />
        <Button
          title={'选择照片(Promise)带base64'}
          onPress={handlePromiseSelectPhoto}
        />
        <Button title={'缓存清除'} onPress={handleDeleteCache} />
        <Button title={'选择视频'} onPress={handleOpenVideoPicker} />
      </View>
      <View style={styles.scroll}>
        <ScrollView contentContainerStyle={styles.scroll}>
          {photos.map((photo, index) => {
            let source = { uri: photo.uri };
            if (photo.base64) {
              source = { uri: photo.base64 };
            }
            return (
              <Image
                key={`image-${index}-${photo.uri}`}
                style={styles.image}
                source={source}
                resizeMode={'contain'}
              />
            );
          })}
        </ScrollView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5FCFF',
    paddingTop: 40,
  },
  btn: {
    backgroundColor: '#FDA549',
    justifyContent: 'center',
    alignItems: 'center',
    height: 44,
    paddingHorizontal: 12,
    margin: 5,
    borderRadius: 22,
  },
  scroll: {
    padding: 5,
    flexWrap: 'wrap',
    flexDirection: 'row',
  },
  image: {
    margin: 10,
    width: (width - 80) / 3,
    height: (width - 80) / 3,
    backgroundColor: '#F0F0F0',
  },
});

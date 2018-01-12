# XDownload
多线程下载，断点续传，多情况回调终极依赖库
## 先说说原理吧
### 线程处理
![image](https://raw.githubusercontent.com/shuaijia/XDownload/master/imgs/db.png)
#### 一共分三个线程下载，获取文件总大小后计算每个线程下载的大小和开始结束位置，最后一段结束位置为文件总大小。
#### 再看看UML代码结构
![image](https://raw.githubusercontent.com/shuaijia/XDownload/master/imgs/uml.png)

## 使用
### step1
```java
allprojects {
  repositories {
    ...
    maven { url 'https://www.jitpack.io' }
  }
}

dependencies {
  compile 'com.github.shuaijia:XDownload:alpha1.0'
}
```
### step2
#### 该添权限添权限，不解释
```java
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<uses-permission android:name="android.permission.INTERNET"/>
```
### step3
#### 创建下载对象
```java
XDownload download = new XDownload();
```
### step4
#### 开始下载，处理回调
```java
download.download(context, "DOWNLOAD_URL", "SAVE_FILE_PATH", new DownloadListener() {
  @Override
  public void onPreDownload(HttpURLConnection connection) {
    Log.e(TAG, "onPreDownload: 准备好");
  }

  @Override
  public void onStart() {
    Log.e(TAG, "onStart: 开始下载");
  }

  @Override
  public void onProgress(int progress) {
    Log.e(TAG, "onProgress: 下载进度" + progress);
  }

  @Override
  public void onStop(long stopLocation) {
    Log.e(TAG, "onStop: 暂停下载" + stopLocation);
  }

  @Override
  public void onResume(long resumeLocation) {
    Log.e(TAG, "onResume: 恢复下载" + resumeLocation);
  }

  @Override
  public void onComplete() {
    Log.e(TAG, "onComplete: 下载完成");
  }

  @Override
  public void onCancel() {
    Log.e(TAG, "onCancel: 取消下载");
  }

  @Override
  public void onFail(int errorCode) {
    Log.e(TAG, "onFail: 下载失败");
  }
});
```
### 此外，你还可以取消下载和暂停下载
```java
download.stopDownload(true);
download.cancelDownload(true);
```
### 欢迎骚扰哦 819418850@qq.com

更多精彩内容，请关注我的微信公众号——Android机动车

![这里写图片描述](http://img.blog.csdn.net/20180110155733884?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvamlhc2h1YWk5NA==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)	

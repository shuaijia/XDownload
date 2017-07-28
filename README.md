# XDownload
多线程下载，断点续传，多情况回调终极依赖库
# 先说说原理吧
### 线程处理
![image](https://raw.githubusercontent.com/shuaijia/XDownload/master/imgs/db.png)
#### 一共分三个线程下载，获取文件总大小后计算每个线程下载的大小和开始结束位置，最后一段结束位置为文件总大小。

# 使用
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

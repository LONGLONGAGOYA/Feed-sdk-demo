# Android Feed SDK 接入文档

最新稳定版本：


* 2.1.4  最新稳定优化版本

* 最新信息请参考： https://github.com/LONGLONGAGOYA/Feed-sdk-demo



## SDK 概述

SDK兼容Android 14+, Demo 兼容 Android 14+。
提供功能：

* 信息流列表页视图

### SDK说明

SDK主要提供了以下类：
* VPFeed：信息流入口
    
      // 初始化方法，会校验 appId(用户申请的sdk的) 和 applicationId（app编译时的applicationid） 是否匹配
      public static void init(Context context, String appId,  String appSecret,
                                String userId, IVPInitCallback callback)
      public static final int FROM_APP = 1;
      public static final int FROM_SHORTCUT = 2;
      //重载init方法，根据不同入口方式（快捷方式/应用内）选择，默认为APP内进入
      public static void init(Context context, String appId,  String appSecret,
                              String userId, IVPInitCallback callback,int from)
      //默认为非debug模式，debug模式下使用的是测试广告，打印log日志信息，非debug模式下不打印log日志，使用appid对应的广告
      public static void init(Context context, String appId,  String appSecret,
                              String userId,boolean isDebug, IVPInitCallback callback , int from)

* android.app.Fragment VPFeed.getLandFragment() 
    获取落地页Fragment

* android.support.v4.app.Fragment VPFeed.getLandSupportFragment()
    获取落地页Fragment
 
## 接入流程

### 1. Gradle集成

* jcenter远程依赖

我们推荐通过 Gradle 集成我们的 SDK。

第一步，在project module下的 build.gradle 配置 repositories 。目前包还在 bintray 上，正在申请 link 到 jcenter，所以测试期间可以先用下面的个人仓库进行配置
```
allprojects {
    repositories {
        jcenter()
    }
}
```
第二步，在app module下的 build.gradle 中同时引入我们的 feed-sdk 的依赖即可
```
'com.videoup:feed-sdk:latest-version'
//如果需要我们默认的图形加载库的话，需要加上feed-load的依赖
'com.videoup:feed-load:latest-version'

```
* 第三方依赖说明

其中,  feed-data-sdk依赖了如下第三方库：
```
    implementation 'com.squareup.retrofit2:retrofit:2.3.0'
    implementation 'com.google.code.gson:gson:2.7'
```
feed-sdk依赖了如下第三方库：
```
    implementation "tv.danmaku.ijk.media:ijkplayer-java:0.8.2"
    implementation "tv.danmaku.ijk.media:ijkplayer-armv7a:0.8.2"
    implementation 'com.danikula:videocache:2.7.0'
    implementation 'com.google.android.gms:play-services-ads:11.8.0'

```
若您的App也依赖了这些第三方库，请确保您依赖的第三方库的版本与我们sdk依赖的第三方库版本兼容。

### 2. 初始化

#### 1.实现图片加载接口
 在自定义Application的OnCreate中添加以下代码，初始化我们的 sdk，首先需要设置图片加载器，有两种方式：
1. 通过引入Feed-load模块，直接使用我们的DefaultImageLoader (依赖Glide4.1.1的图形加载库,需要注意兼容问题)
    ```
            VPFeed.setVpLoadImage(new DefaultImageLoader());
    ```

2. 实现图片加载的接口 VPLoadImage
```
 //注意，此处为必须实现，否则sdk不能正常运行。
 //以下为示例代码，用户可以有自己的实现方式，请使用ARGP_8888的图片。  
    private void setImageLoader(){
                    VPFeed.setVpLoadImage(new VPLoadImage() {
    
                        @Override
                        public void clear(ImageView view) {
                            Glide.get(view.getContext()).clearMemory();
                        }
    
    
                        @Override
                        public void load(Context context, String url, int placeholder_id, boolean cache, final BitmapTransform bitmapTransform, ImageView target) {
                            if (context instanceof Activity && ((Activity) context).isFinishing()) {
                                return;
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                if (context instanceof Activity && ((Activity) context).isDestroyed()) {
                                    return;
                                }
                            }
                            if (bitmapTransform != null) {
                                Glide.with(context)
                                        .load(url)
                                        .apply(new RequestOptions()
                                                .placeholder(placeholder_id)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .optionalCenterCrop()
                                                .transform(new BitmapTransformation() {
                                                    @Override
                                                    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                                                        Bitmap rec;
                                                        try {
                                                            rec = pool.get(outWidth, outHeight, Bitmap.Config.ARGB_8888);
                                                        } catch (Exception e) {
                                                            rec = null;
                                                        }
                                                        Bitmap b = bitmapTransform.transform(toTransform, rec, Bitmap.Config.ARGB_8888, outWidth, outHeight);
                                                        return b;
                                                    }
    
                                                    @Override
                                                    public void updateDiskCacheKey(MessageDigest messageDigest) {
    
                                                    }
                                                })
                                        )
                                        .into(target);
                            } else {
                                Glide.with(context)
                                        .load(url)
                                        .apply(new RequestOptions()
                                                .placeholder(placeholder_id)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        )
                                        .into(target);
                            }
                        }
                    });
        }
```    
#### 2.设置一些必要的方法
       
       ```
       none
      
       ```
       

#### 3.执行初始化方法

        //可以使用测试用的 app_id 和 app_secret
        //app_id: test
        //app_secret: com.videoup.feed
        //user_id: 这个是接入方的用户 id， 用于个性化推荐
        VPFeed.init(getApplicationContext(), "app_id", "app_secret", "user_id", new IVPInitCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("TAG", "success");
                    }

                    @Override
                    public void onError(int i, Throwable throwable) {
                        Log.e("TAG", "error", throwable);
                    }
                });

#### 4.提供单条热点视频个性化推荐接口： 

        数据结构 FeedItem : title（视频标题）,thumbnail(封面图片url)，etc

         接口： VPFeed.getPushVideo(IVPFeedCallback callback);               

        //kotlin code (notice)
         Demo：VPFeed.getPushVideo(object : IVPFeedCallback {
                                override fun onError(throwable: Throwable?) {
                
                                }
                                //返回结果只有一条视频。
                                override fun onFeedLoad(result: Result<MutableList<FeedItem>>?) {
                                    result?.data?.get(0)?.let {
                                        val intent = Intent(this@PushService,MainActivity::class.java)
                                        intent.putExtra("videoId",it.id)
                                        val pendingIntent = PendingIntent.getActivity(this@PushService,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
                                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        val builder = NotificationCompat.Builder(this@PushService)
                                        val notification = builder
                                                .setContentTitle(it.title)
                                                .setContentText(it.description)
                                                .setWhen(System.currentTimeMillis())
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                                                .setContentIntent(pendingIntent)
                                                .build()
                                        notificationManager.notify(1, notification)
                                    }
                                }
                
                            })


         进行更新信息流，
         
         Demo:
           @Override
              protected void onNewIntent(Intent intent) {
                  super.onNewIntent(intent);
                  FeedItem feedItem = (FeedItem) intent.getSerializableExtra("feedItem");
                  
                  //通过该接口获取一个FeedItem，然后在初始化代码之后调用 (java code notice)
                  FeedItem feedItem = (FeedItem) intent.getSerializableExtra("feedItem");
                      if (feedItem != null) {
                      final ProgressDialog pd = new ProgressDialog(this);
                      //设置进度条风格，风格为圆形，旋转的
                      pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                      
                      // 设置ProgressDialog 标题
                      
                      // 设置ProgressDialog 提示信息
                      pd.setMessage("正在进入视频流");
                      
                      
                      // 设置ProgressDialog 进度条进度
                      pd.setProgress(100);
                      
                      // 设置ProgressDialog 的进度条是否不明确
                      pd.setIndeterminate(true);
                      
                      // 设置ProgressDialog 是否可以按退回按键取消
                      pd.setCancelable(false);
                      pd.show();
                      //这个方法将进行尝试进入全屏视频feed，并通过EnterFeedListener来告诉app是否能够进入
                      VPFeed.toNewActivityWithPush(this, feedItem, new VPFeed.EnterFeedListener() {
                      @Override
                       //ok  true：可以进入全屏播放  false：不可以进入全屏播放，将不会进入全屏视频流，并停留在当前页面
                      public void onDone(boolean ok, Throwable throwable) {
                      Log.d("gzy", "MainActivity->onDone: ok:"+ok);
                      pd.cancel();
                      }
                      });
                          
              }
              
              
          @Override
              protected void onCreate(Bundle savedInstanceState) {
                  ...
          
                  VPFeed.init(this, APP_ID_USE, SECRET_ID_USE, "user",DEBUG, new IVPInitCallback() {
                      @Override
                      public void onSuccess() {
                          Log.d("MainActivity", "success");
                      }
                      
                      @Override
                      public void onError(int i, Throwable throwable) {
                          Log.d("MainActivity", "error", throwable);
                      }
                  },VPFeed.FROM_APP);
                
                  FeedItem feedItem = (FeedItem) getIntent().getSerializableExtra("feedItem");
                          if (feedItem != null) {
                          
                          if (feedItem != null) {
                          final ProgressDialog pd = new ProgressDialog(this);
                          //设置进度条风格，风格为圆形，旋转的
                          pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                          
                          // 设置ProgressDialog 标题
                          
                          // 设置ProgressDialog 提示信息
                          pd.setMessage("正在进入视频流");
                          
                          
                          // 设置ProgressDialog 进度条进度
                          pd.setProgress(100);
                          
                          // 设置ProgressDialog 的进度条是否不明确
                          pd.setIndeterminate(true);
                          
                          // 设置ProgressDialog 是否可以按退回按键取消
                          pd.setCancelable(false);
                          pd.show();
                          //这个方法将进行尝试进入全屏视频feed，并通过EnterFeedListener来告诉app是否能够进入
                          VPFeed.toNewActivityWithPush(this, feedItem, new VPFeed.EnterFeedListener() {
                          @Override
                          //ok  true：可以进入全屏播放  false：不可以进入全屏播放，将不会进入全屏视频流，并停留在当前页面
                          public void onDone(boolean ok, Throwable throwable) {
                          Log.d("gzy", "MainActivity->onDone: ok:"+ok);
                          pd.cancel();
                          }
                          });
                    }
 
       
        
        
        
### 3. 使用

VPFeed 为 sdk 主入口，提供全局设置以及信息流 Fragment 的实例化调用。


* 提供信息流列表 Fragment

    ```
    1. android.app.Fragment VPFeed.getLandFragment() 
        获取落地页Fragment
    
    2. android.support.v4.app.Fragment VPFeed.getLandSupportFragment()
        获取落地页Fragment

    ```


### 4. 混淆

若您的App开启了混淆，请为我们的SDK添加下述混淆规则
    
    ```
      # google 广告库
      -dontwarn com.google.android.gms.**
      
      -keep class * extends com.videoup.data.model.BaseData { *; }
      
      -dontnote retrofit2.Platform
      # Platform used when running on Java 8 VMs. Will not be used at runtime.
      -dontwarn retrofit2.Platform$Java8
      # Retain generic type information for use by reflection by converters and adapters.
      -keepattributes Signature
      # Retain declared checked exceptions for use by a Proxy instance.
      -keepattributes Exceptions
      -keepattributes *Annotation*
      
      
      -dontwarn okhttp3.**
      -dontwarn okio.**
      
      -dontwarn tv.danmaku.ijk.media.player.**
      -keep class tv.danmaku.ijk.media.player.** { *; }
      -keep interface tv.danmaku.ijk.media.player.* { *; }
      
      
      -keep public class * implements com.bumptech.glide.module.GlideModule
      -keep public class * extends com.bumptech.glide.GeneratedAppGlideModule
      -keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
          **[] $VALUES;
          public *;
      }
      
      -keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
      -dontwarn com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool
      -dontwarn com.bumptech.glide.load.resource.bitmap.Downsampler
      -dontwarn com.bumptech.glide.load.resource.bitmap.HardwareConfigState
      -dontwarn com.bumptech.glide.manager.RequestManagerRetriever
      -dontwarn javax.annotation.**
      
      -keep class com.videoup.*  { *; }
      -keep class * extends com.videoup.data.model.BaseData { *; }

    ```

## 注意


## 备注

1. 信息流的设计是针对竖屏进行适配的
2. 目前视频 so 文件只带了 armv7a，如需其他 so，需要：
```
// 具体可参考： https://github.com/Bilibili/ijkplayer/blob/master/android/ijkplayer/ijkplayer-example/build.gradle
    implementation "tv.danmaku.ijk.media:ijkplayer-armv5:$rootProject.ijkPlayerVersion"
    implementation "tv.danmaku.ijk.media:ijkplayer-x86:$rootProject.ijkPlayerVersion"
```
3. 参考例子代码： https://github.com/LONGLONGAGOYA/Feed-sdk-demo

## 高级

1.
     ```   
     
     //为feed增加视频监听，当feed播放视频时和暂停播放视频时将执行如下回调。
     VPFeed.setVideoPlayListener(new VPFeed.VideoPlayListener() {
                @Override
                public void onPlay() {
                    Log.d("TAG", "MainActivity->onPlay: ");
                }
    
                @Override
                public void onPause() {
                    Log.d("TAG", "MainActivity->onPause: ");
                }
            });
            
     ```

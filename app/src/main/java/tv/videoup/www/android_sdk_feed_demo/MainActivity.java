package tv.videoup.www.android_sdk_feed_demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.videoup.IVPInitCallback;
import com.videoup.VPFeed;
import com.videoup.data.model.FeedItem;
import com.videoup.feed.load.BitmapTransform;
import com.videoup.feed.load.VPLoadImage;
import com.videoup.load.DefaultImageLoader;

import java.security.MessageDigest;

public class MainActivity extends AppCompatActivity {

    public static final boolean DEBUG = true;
    public static final boolean USE_DEV = false;

    public static final String APP_ID = "0ccb0dd4";
    public static final String SECRET_ID = "oKQ_U1aLlcwJfVWU*l!dbwByQ4yVg9%$";
    public static final String APP_ID_DEV = "2a387e19";
    public static final String SECRET_ID_DEV = "BuI1alL0rClJsw1!@9XE*rjY$CFW!InC";


    public static final String APP_ID_USE = USE_DEV ? APP_ID_DEV : APP_ID;
    public static final String SECRET_ID_USE = USE_DEV ? SECRET_ID_DEV : SECRET_ID;

    private android.support.v4.app.Fragment fragment;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
            VPFeed.toNewActivityWithPush(this, feedItem, new VPFeed.EnterFeedListener() {
                @Override
                public void onDone(boolean ok, Throwable throwable) {
                    Log.d("gzy", "MainActivity->onDone: ok:"+ok);
                    pd.cancel();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (!VPFeed.onBackPressed()) {
            super.onBackPressed();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initState();
        setContentView(R.layout.activity_main);


        VPFeed.init(this, APP_ID_USE, SECRET_ID_USE, "user", DEBUG, new IVPInitCallback() {
            @Override
            public void onSuccess() {
                Log.d("MainActivity", "success");
                Intent intent = new Intent(MainActivity.this, PushService.class);
                startService(intent);
            }

            @Override
            public void onError(int i, Throwable throwable) {
                Log.d("MainActivity", "error", throwable);
            }
        }, VPFeed.FROM_APP);
        setUpdateListener();


        FeedItem feedItem = (FeedItem) getIntent().getSerializableExtra("feedItem");
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
            VPFeed.toNewActivityWithPush(this, feedItem, new VPFeed.EnterFeedListener() {
                @Override
                public void onDone(boolean ok, Throwable throwable) {
                    Log.d("gzy", "MainActivity->onDone: ok");
                    pd.cancel();
                }
            });
        }else {

        }

        fragment = VPFeed.getLandSupportFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.feed_container, fragment).commitAllowingStateLoss();


        VPFeed.setVpLoadImage(new DefaultImageLoader());
        VPFeed.setVideoPlayListener(new VPFeed.VideoPlayListener() {
            @Override
            public void onPlay() {
                Log.d("gzy", "MainActivity->onPlay: ");
            }

            @Override
            public void onPause() {
                Log.d("gzy", "MainActivity->onPause: ");
            }
        });
//        setImageLoader();

    }

    private void setImageLoader() {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("gzy", "MainActivity->onDestroy: finish");
    }

    private void setUpdateListener() {
//        VPFeed.addOnUpdateListener(new OnUpdateDataListener() {
//            @Override
//            public void onSuccess(int count) {
//                Log.d("gzy", "MainActivity->onSuccess: count:"+count);
//            }
//
//            @Override
//            public void onError(int code, String message) {
//                Log.d("gzy", "MainActivity->onError: code:"+code+"    message:"+message);
//            }
//        });
    }

    /**
     * 沉浸式状态栏
     */
    private void initState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }


}



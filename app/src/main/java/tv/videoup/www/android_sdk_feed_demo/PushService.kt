package tv.videoup.www.android_sdk_feed_demo

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.widget.Toast
import android.graphics.BitmapFactory
import android.support.v7.app.NotificationCompat
import com.videoup.IVPFeedCallback
import com.videoup.VPFeed
import com.videoup.data.model.FeedItem
import com.videoup.data.model.Result


/**
 * Created by gaozhaoya on 2018/3/2.
 */


class PushService : Service(){

    var serviceHandler:ServiceHandler? = null
    val PUSH_VIDEO = 1
    val START_SERVICE = 2

    inner class ServiceHandler(looper:Looper) : Handler(looper) {
        override fun handleMessage(msg: Message?) {
            when (msg?.arg1){
                PUSH_VIDEO -> {
                    pushVideo()
                }
                START_SERVICE -> {
                    startService(msg.data.getString("data"))
                }
            }
        }

        fun pushVideo(){

        }

        fun startService(str:String){
            Toast.makeText(this@PushService,"启动了[$str]线程",Toast.LENGTH_SHORT).show()

            post( {
                sendNotify()
            })

        }

        private fun sendNotify() {
            VPFeed.getPushVideo(object : IVPFeedCallback {
                override fun onError(throwable: Throwable?) {

                }

                override fun onFeedLoad(result: Result<MutableList<FeedItem>>?) {
                    result?.data?.get(0)?.let {
                        val intent = Intent(this@PushService,MainActivity::class.java)
                        intent.putExtra("feedItem",it)
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


        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val thread = HandlerThread("ServicerStartArgument",Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        serviceHandler = ServiceHandler(thread.looper)
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val message = serviceHandler?.obtainMessage()
        message?.arg1 = START_SERVICE
        message?.data = Bundle()
        message?.data?.putString("data",""+startId)
        serviceHandler?.sendMessage(message)

        return START_STICKY

    }

}
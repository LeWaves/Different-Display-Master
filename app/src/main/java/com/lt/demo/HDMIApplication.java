package com.lt.demo;

import android.app.Application;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.display.DisplayManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by lt on 2016/10/25.
 */

public class HDMIApplication extends Application {

    private MediaRouter mediaRouter = null;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        this.mediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, simpleCallback);
        UpdatePresent();
    }


    private MediaRouter.SimpleCallback simpleCallback = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            super.onRouteSelected(router, type, info);
            HDMIApplication.this.UpdatePresent();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            super.onRouteUnselected(router, type, info);
            HDMIApplication.this.UpdatePresent();
        }

        @Override
        public void onRouteChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            super.onRouteChanged(router, info);
            HDMIApplication.this.UpdatePresent();
        }
    };

    private MyPresentation myPresentation = null;

    private void UpdatePresent() {
        DisplayManager mDisplayManager;// 屏幕管理类
        mDisplayManager = (DisplayManager) this
                .getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        if (myPresentation != null && myPresentation.getDisplay() != displays[displays.length - 1]) {
            myPresentation.dismiss();
            myPresentation = null;
        }

        // Show a new presentation if the previous one has been dismissed and a
        // route has been selected.
        Log.i("lt","获取屏幕是否为空："+(displays[displays.length - 1] != null));
        if (myPresentation == null && displays[displays.length - 1] != null) {
            // Initialize a new Presentation for the Display
            myPresentation = new MyPresentation(this, displays[displays.length - 1]);
            myPresentation.setOnDismissListener(
                    new DialogInterface.OnDismissListener() {
                        // Listen for presentation dismissal and then remove it
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (dialog == myPresentation) {
                                myPresentation = null;
                            }
                        }
                    });

            // Try to show the presentation, this might fail if the display has
            // gone away in the meantime
            try {
                this.myPresentation.getWindow().getAttributes().type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
                myPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                // Couldn't show presentation - display was already removed
                myPresentation = null;
            }
        }
    }



    public MyPresentation getPresentation() {
        return this.myPresentation;
    }

    /**
     * 创建一个presention的子类
     */
    public final class MyPresentation extends Presentation {
        private VideoView videoView;
        private String videoFile = "";
        private Uri uri = null;

        public MyPresentation(Context outerContext, Display display) {
            super(outerContext, display);
        }


        /**
         * 添加布局
         *
         * @param savedInstanceState
         */
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.play_videoview);
            videoView = (VideoView) findViewById(R.id.videoView);
        }

        /**
         * 添加方法来播放视频
         *
         * @param filePath 文件的路径
         */
        public void startVideo(String filePath) {
            MyPresentation.this.videoFile = filePath;
            this.uri = Uri.fromFile(new File(this.videoFile));
            this.videoView.setVideoURI(this.uri);
            this.videoView.requestFocus();
            this.videoView.start();
            /**
             * 设置重播
             */
            this.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MyPresentation.this.videoView.start();
                }
            });
        }
    }
}

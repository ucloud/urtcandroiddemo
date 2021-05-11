package com.urtcdemo.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cmcc.sdkengine.CMCCRtcEngine;
import com.cmcc.sdkengine.CMCCEnvHelper;
import com.cmcc.sdkengine.define.CMCCSurfaceViewRenderer;
import com.cmcc.sdkengine.define.CMCCAudioDevice;
import com.cmcc.sdkengine.define.CMCCAuthInfo;
import com.cmcc.sdkengine.define.CMCCCaptureMode;
import com.cmcc.sdkengine.define.CMCCErrorCode;
import com.cmcc.sdkengine.define.CMCCMediaServiceStatus;
import com.cmcc.sdkengine.define.CMCCMediaType;
import com.cmcc.sdkengine.define.CMCCMixProfile;
import com.cmcc.sdkengine.define.CMCCNetWorkQuality;
import com.cmcc.sdkengine.define.CMCCRecordType;
import com.cmcc.sdkengine.define.CMCCChannelProfile;
import com.cmcc.sdkengine.define.CMCCScaleType;
import com.cmcc.sdkengine.define.CMCCStreamStatus;
import com.cmcc.sdkengine.define.CMCCStreamInfo;
import com.cmcc.sdkengine.define.CMCCClientRole;
import com.cmcc.sdkengine.define.CMCCStreamType;
import com.cmcc.sdkengine.define.CMCCSurfaceViewGroup;
import com.cmcc.sdkengine.define.CMCCTrackType;
import com.cmcc.sdkengine.define.CMCCVideoProfile;
import com.cmcc.sdkengine.listener.ICMCCRecordListener;
import com.cmcc.sdkengine.listener.ICMCCRtcEngineEventHandler;
import com.cmcc.sdkengine.openinterface.CMCCDataProvider;
import com.cmcc.sdkengine.openinterface.CMCCDataReceiver;
import com.cmcc.sdkengine.openinterface.CMCCFirstFrameRendered;
import com.cmcc.sdkengine.openinterface.CMCCScreenShot;
import com.urtcdemo.R;
import com.urtcdemo.adpter.RemoteVideoAdapter;
import com.urtcdemo.utils.CommonUtils;
import com.urtcdemo.utils.ToastUtils;
import com.urtcdemo.utils.UiHelper;
import com.urtcdemo.utils.VideoListener;
import com.urtcdemo.utils.VideoPlayer;
import com.urtcdemo.view.CustomerClickListener;
import com.urtcdemo.view.SteamScribePopupWindow;
import com.urtcdemo.view.URTCVideoViewInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ucloud.record.MediaRecorderBase;
import org.webrtc.ucloud.record.URTCRecordManager;
import org.webrtc.ucloud.record.model.MediaObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import core.renderer.SurfaceViewGroup;
import tv.danmaku.ijk.media.player.IMediaPlayer;

import static com.cmcc.sdkengine.define.CMCCErrorCode.NET_ERR_CODE_OK;
import static com.cmcc.sdkengine.define.CMCCMediaType.MEDIA_TYPE_SCREEN;
import static com.cmcc.sdkengine.define.CMCCMediaType.MEDIA_TYPE_VIDEO;
import static com.urtcdemo.activity.RoomActivity.BtnOp.OP_LOCAL_RECORD;

//import com.ucloudrtclib.sdkengine.define.UcloudRtcSdkRecordProfile;
//import com.ucloudrtclib.sdkengine.openinterface.UcloudRTCSceenShot;


public class RoomActivity extends AppCompatActivity implements VideoListener {
    private static final String TAG = "RoomActivity";

    private String mUserid = "test001";
    private String mRoomid = "urtc1";
    private String mRoomToken = "test token";
    private String mAppid = "";
    private String mBucket = "urtc-test";
    private String mRegion = "cn-bj";
    private boolean mIsRecording = false;
    private boolean mIsMixing = false;
    private boolean mAtomOpStart = false;
    private boolean mIsPublished = false;
    private boolean mMixAddOrDel = true;

    TextView title = null;
//    UCloudRtcSdkSurfaceVideoView localrenderview = null;
    CMCCSurfaceViewRenderer localrenderview = null;
    ProgressBar localprocess = null;

    final int COL_SIZE_P = 3;
    final int COL_SIZE_L = 6;
    private GridLayoutManager gridLayoutManager;
    private RemoteVideoAdapter mVideoAdapter;
    RecyclerView mRemoteGridView = null;
    CMCCRtcEngine sdkEngine = null;
    ImageButton mPublish = null;
    ImageButton mHangup = null;
    ImageButton mSwitchcam = null;
    ImageButton mMuteMic = null;
    ImageButton mLoudSpkeader = null;
    ImageButton mMuteCam = null;
    TextView mOpBtn = null;
    TextView mAddDelBtn = null;
    CheckBox  mCheckBoxMirror = null;
    private SteamScribePopupWindow mSpinnerPopupWindowScribe;
    private View mStreamSelect;
    private TextView mTextStream;
    //int mCaptureMode;
    int mVideoProfile;
    @CommonUtils.PubScribeMode
    int mPublishMode;
    @CommonUtils.PubScribeMode
    int mScribeMode;
    CMCCClientRole mRole;
    CMCCChannelProfile mClass;
    boolean isScreenCaptureSupport;
    boolean mCameraEnable;
    boolean mMicEnable;
    boolean mScreenEnable;
    private List<CMCCStreamInfo> mSteamList;
    private CMCCStreamInfo mLocalStreamInfo;
    private boolean mRemoteVideoMute;
    private boolean mRemoteAudioMute;
    private CMCCSurfaceViewGroup mMuteView = null;
    Chronometer timeshow;
    private int mPictureFlag = 0;
    private boolean mPFlag = false;
    private ArrayBlockingQueue<RGBSourceData> mQueue = new ArrayBlockingQueue(2);
    // 定义一个nv21 的
     private ArrayBlockingQueue<NVSourceData> mQueueNV = new ArrayBlockingQueue(2);
    private Thread mCreateImgThread;
    private Timer mTimerCreateImg = new Timer("createPicture");
    private boolean startCreateImg = true;
    private AtomicInteger memoryCount = new AtomicInteger(0);
    private List<String> userIds = new ArrayList<>();
    private boolean mLocalRecordStart = false;
    private CMCCMediaType mPublishMediaType;
    private VideoPlayer mVideoPlayer ;
    private CMCCSurfaceViewRenderer mRemoteRenderView;
    private boolean bigVolume = true;
    private FrameLayout testT ,testB;
    private AppCompatSeekBar mSeekBar;

    /**
     * SDK视频录制对象
     */
    private MediaRecorderBase mMediaRecorder;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {

    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onSeekComplete(IMediaPlayer iMediaPlayer) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {

    }

    enum BtnOp{
        OP_LOCAL_RECORD,
        OP_REMOTE_RECORD,
        OP_SEND_MSG,
        OP_LOCAL_RESAMPLE,
        OP_MIX,
        OP_MIX_MANUAL
    }
    class RGBSourceData{
        Bitmap srcData;
        int width;
        int height;
        int type;

        public RGBSourceData(Bitmap srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public Bitmap getSrcData() {
            return srcData;
        }

        public void setSrcData(Bitmap srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    class NVSourceData{
        ByteBuffer srcData;
        int width;
        int height;
        int type;

        public NVSourceData(ByteBuffer srcData, int width, int height,int type) {
            this.srcData = srcData;
            this.width = width;
            this.height = height;
            this.type = type;
        }

        public ByteBuffer getSrcData() {
            return srcData;
        }

        public void setSrcData(ByteBuffer srcData) {
            this.srcData = srcData;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getType() {
            return type;
        }
    }

    private CMCCDataProvider mCMCCDataProvider = new CMCCDataProvider() {
        private ByteBuffer cacheBuffer;
        private RGBSourceData rgbSourceData;
        private NVSourceData nvSourceData;

        @Override
        public ByteBuffer provideRGBData(List<Integer> params) {
            return null;
        }

        public void releaseBuffer(){
            if(rgbSourceData != null && !rgbSourceData.getSrcData().isRecycled()){
                rgbSourceData.getSrcData().recycle();
                rgbSourceData.srcData = null;
                rgbSourceData = null;
            }
            if(cacheBuffer != null){
                sdkEngine.getNativeOpInterface().releaseNativeByteBuffer(cacheBuffer);
            }
        }
    };

    private CMCCDataReceiver mDataReceiver = new CMCCDataReceiver() {
        private int limit = 0;
        private ByteBuffer cache;

        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/yuvrgba"+ limit+".jpg";
            if (limit++ < 5) {
                File file = new File(name);
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getType() {
            return CMCCDataReceiver.I420_TO_ABGR;
        }

        @Override
        public ByteBuffer getCacheBuffer() {
            if(cache == null){
                //根据需求来，设置最大的可能用到的buffersize，后续回调会复用这块内存
                int size = 4096*2160*4;
                cache = sdkEngine.getNativeOpInterface().
                        createNativeByteBuffer(4096*2160*4);
            }
            cache.clear();
            return cache;
        }

        @Override
        public void releaseBuffer() {
            if(cache != null)
            sdkEngine.getNativeOpInterface().releaseNativeByteBuffer(cache);
            cache = null;
        }
    };

    private CMCCFirstFrameRendered mFirstFrameRendered = new CMCCFirstFrameRendered() {
        @Override
        public void onFirstFrameRender(CMCCStreamInfo info, View view) {

        }
    };

//    private View.OnClickListener mSwapRemoteLocalListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (v instanceof UCloudRtcSdkSurfaceVideoView) {
//                String key = ((URTCVideoViewInfo) v.getTag(R.id.index)).getKey();
//                if (mVideoAdapter.checkCanSwap(key)) {
//                    boolean state = mVideoAdapter.checkState(key);
//                    if (!state) {
//                        UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) v.getTag();
//                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                        if (mLocalStreamInfo != null) {
//                            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), (UCloudRtcSdkSurfaceVideoView) v,null, null);
//                            v.setTag(R.id.swap_info, mLocalStreamInfo);
//                        }
//                        sdkEngine.startRemoteView(remoteStreamInfo, (UCloudRtcSdkSurfaceVideoView) localrenderview,null,null);
//                        ((UCloudRtcSdkSurfaceVideoView) v).refreshRemoteOp(View.INVISIBLE);
//                        ((UCloudRtcSdkSurfaceVideoView) localrenderview).refreshRemoteOp(View.VISIBLE);
//                        localrenderview.setTag(R.id.swap_info, remoteStreamInfo);
//                        if (mClass == UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_LARGE) {
//                            localrenderview.setVisibility(View.VISIBLE);
//                            localrenderview.setTag(R.id.view_info, v);
//                            localrenderview.setBackgroundColor(Color.TRANSPARENT);
//                            v.setVisibility(View.INVISIBLE);
                            //和本地view截图功能触发重叠，App使用者可以另行定义触发
//                            localrenderview.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    if (v instanceof UCloudRtcSdkSurfaceVideoView) {
//                                        localrenderview.setVisibility(View.INVISIBLE);
//                                        UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) v.getTag(R.id.swap_info);
//                                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                                        UCloudRtcSdkSurfaceVideoView view = (UCloudRtcSdkSurfaceVideoView) v.getTag(R.id.view_info);
//                                        view.setVisibility(View.VISIBLE);
//                                        view.refreshRemoteOp(View.VISIBLE);
//                                        sdkEngine.startRemoteView(remoteStreamInfo, view);
//                                        mVideoAdapter.reverseState(key);
//                                    }
//                                }
//                            });
//                        }
//                    } else {
//                        //有交换过
//                        UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) v.getTag();
//                        //停止交换过的大窗渲染远端
//                        sdkEngine.stopRemoteView(remoteStreamInfo);
//                        //停止本地视频渲染
//                        sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                        sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
//                        sdkEngine.startRemoteView(remoteStreamInfo, (UCloudRtcSdkSurfaceVideoView) v,null,null);
//                        ((UCloudRtcSdkSurfaceVideoView) v).refreshRemoteOp(View.VISIBLE);
//                        ((UCloudRtcSdkSurfaceVideoView) localrenderview).refreshRemoteOp(View.INVISIBLE);
//                        v.setTag(R.id.swap_info, null);
//                        localrenderview.setTag(R.id.swap_info, null);
//                    }
//                    mVideoAdapter.reverseState(key);
//                } else {
//                    ToastUtils.shortShow(RoomActivity.this, "其它窗口已经交换过，请先交换回来");
//                }
//            }
//        }
//    };

    private View.OnClickListener mScreenShotOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             addScreenShotCallBack(v);
        }
    };

    private View.OnClickListener mLocalChangeRenderMode =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            localrenderview.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
        }
    };

    private CMCCSurfaceViewGroup.RemoteOpTrigger mOnRemoteOpTrigger = new CMCCSurfaceViewGroup.RemoteOpTrigger() {
        @Override
        public void onRemoteVideo(View v, SurfaceViewGroup parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                CMCCStreamInfo swapStreamInfo = (CMCCStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteVideoStream(swapStreamInfo.getUId(), !mRemoteVideoMute);
            } else if (parent.getTag() != null) {
                CMCCStreamInfo streamInfo = (CMCCStreamInfo) parent.getTag();
                sdkEngine.muteRemoteVideoStream(streamInfo.getUId(), !mRemoteVideoMute);
            }
            mMuteView = (CMCCSurfaceViewGroup)parent;
        }

        @Override
        public void onRemoteAudio(View v, SurfaceViewGroup parent) {
            if (parent.getTag(R.id.swap_info) != null) {
                CMCCStreamInfo swapStreamInfo = (CMCCStreamInfo) parent.getTag(R.id.swap_info);
                sdkEngine.muteRemoteAudioStream(swapStreamInfo.getUId(), !mRemoteAudioMute);
            } else if (parent.getTag() != null) {
                CMCCStreamInfo streamInfo = (CMCCStreamInfo) parent.getTag();
                sdkEngine.muteRemoteAudioStream(streamInfo.getUId(), !mRemoteAudioMute);
            }
            mMuteView = (CMCCSurfaceViewGroup)parent;
        }
    };

    private RemoteVideoAdapter.RemoveRemoteStreamReceiver mRemoveRemoteStreamReceiver = new RemoteVideoAdapter.RemoveRemoteStreamReceiver() {
        @Override
        public void onRemoteStreamRemoved(boolean swaped) {
            if (swaped) {
//                if (mClass == UCloudRtcSdkRoomType.UCLOUD_RTC_SDK_ROOM_SMALL) {
//                    sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
//                    sdkEngine.startPreview(mLocalStreamInfo, localrenderview,null,null);
//                } else if (localrenderview.getTag(R.id.swap_info) != null) {
//                    UCloudRtcSdkStreamInfo remoteStreamInfo = (UCloudRtcSdkStreamInfo) localrenderview.getTag(R.id.swap_info);
//                    sdkEngine.stopRemoteView(remoteStreamInfo);
//                }
            }
        }
    };

    private void refreshStreamInfoText() {
        if (mSteamList == null || mSteamList.isEmpty()) {
            mTextStream.setText("当前没有流可以订阅");
        } else {
            mTextStream.setText(String.format("当前有%d路流可以订阅", mSteamList.size()));
        }
    }

    ICMCCRecordListener mLocalRecordListener = new ICMCCRecordListener() {
        @Override
        public void onLocalRecordStart(String path, int code,String msg) {
            Log.d(TAG, "onLocalRecordStart: " + path + " code: "+ code + " msg: " + msg);
        }

        @Override
        public void onLocalRecordStop(String path, long fileLength, int code) {
            Log.d(TAG, "onLocalRecordStop: " + path + "fileLength: "+ fileLength + "code: "+ code);
        }

        @Override
        public void onRecordStatusCallBack(long duration, long fileSize) {
            Log.d(TAG, "onRecordStatusCallBack duration: " + duration + " fileSize: "+ fileSize);
        }
    };

    ICMCCRtcEngineEventHandler eventListener = new ICMCCRtcEngineEventHandler() {
        @Override
        public void onServerDisconnect() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerDisconnect: ");
                    ToastUtils.shortShow(RoomActivity.this, " 服务器已断开");
                    stopTimeShow();
                    onMediaServerDisconnect();
                }
            });
        }

        @Override
        public void onJoinChannelSuccess(String joinChannel, String userId) {
            runOnUiThread(() -> {
                ToastUtils.shortShow(RoomActivity.this, " 加入房间成功");
                startTimeShow();
            });
        }

        @Override
        public void onError(int error, String msg) {
            //to do leave room
            runOnUiThread(() -> {
                ToastUtils.shortShow(RoomActivity.this, " 加入房间失败 " +
                        error + " errmsg " + msg);
                Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
                onMediaServerDisconnect();
                startActivity(intent);
                finish();
            });
        }

        @Override
        public void onLeaveChannel(int code, String msg, String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 离开房间 " +
                            code + " errmsg " + msg);
//                    Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
                    onMediaServerDisconnect();
                    System.gc();
//                    startActivity(intent);
//                    finish();
                }
            });
        }

        @Override
        public void onConnectionLost(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "rejoining room");
                    ToastUtils.shortShow(RoomActivity.this, " 服务器重连中…… ");
                    stopTimeShow();
                }
            });
        }

        @Override
        public void onRejoinChannelSuccess(String roomid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, "服务器重连成功");
                    startTimeShow();
                }
            });
        }

        @Override
        public void onLocalPublish(int code, String msg, CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
//                        ToastUtils.shortShow(RoomActivity.this, "发布视频成功");
                        mPublish.setImageResource(R.drawable.unpublish);
                        mIsPublished = true;
                        int mediatype = info.getMediaType().ordinal();
                        mPublishMediaType = CMCCMediaType.matchValue(mediatype);
                        if (mediatype == MEDIA_TYPE_VIDEO.ordinal()) {
                            if (!sdkEngine.isAudioOnlyMode()) {
                                localrenderview.setVisibility(View.VISIBLE);
                                localrenderview.setBackgroundColor(Color.TRANSPARENT);
//                                localrenderview.setScalingType(UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT);
                                sdkEngine.setupLocalVideo(info,
                                        localrenderview, CMCCScaleType.SCALE_ASPECT_FILL,null);

//                                UCloudRtcRenderView renderView = new UCloudRtcRenderView(RoomActivity.this);
//                                FrameLayout frameLayout = findViewById(R.id.local_parent);
//                                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(150,150);
//                                frameLayout.addView(renderView,0,layoutParams);
//                                renderView.init();
//                                sdkEngine.startPreview(info.getMediaType(),
//                                        renderView,UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FILL,null);
                                mLocalStreamInfo = info;
                                localrenderview.setTag(mLocalStreamInfo);
//                                localrenderview.refreshRemoteOp(View.INVISIBLE);
                                localrenderview.setOnClickListener(mScreenShotOnClickListener);
//                                localrenderview.setOnClickListener(mLocalChangeRenderMode);
                            }

                        } else if (mediatype == CMCCMediaType.MEDIA_TYPE_SCREEN.ordinal()) {
                            //if (mCaptureMode == CommonUtils.screen_capture_mode) {
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
//                                localrenderview.setVisibility(View.VISIBLE);
                                sdkEngine.setupLocalVideo(info, localrenderview, CMCCScaleType.SCALE_ASPECT_FILL,null);
                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this,
                                "发布视频失败 " + code + " errmsg " + msg);
                    }

                }
            });
        }

        @Override
        public void onLocalUnPublish(int code, String msg, CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        mIsPublished = false;
                        if (info.getMediaType() == MEDIA_TYPE_VIDEO) {
                            if (localrenderview != null) {
//                                localrenderview.refresh();
                            }
                        } else if (info.getMediaType() == CMCCMediaType.MEDIA_TYPE_SCREEN) {
                            //if (mCaptureMode == CommonUtils.screen_capture_mode) {
                            if (mScreenEnable && !mCameraEnable && !mMicEnable) {
//                                if (localrenderview != null) {
//                                    localrenderview.refresh();
//                                }
                            }
                        }
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频成功");
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "取消发布视频失败 "
                                + code + " errmsg " + msg);
                    }
                }
            });
        }

        @Override
        public void onUserJoined(String uid) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 用户 "
                            + uid + " 加入房间 ");
                }
            });
        }

        @Override
        public void onUserOffline(String uid) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "remote user " + uid);
                    onUserLeave(uid);
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                            uid);
                }
            });
        }

        @Override
        public void onRemotePublish(CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //特殊情况下，譬如客户端在断网情况下离开房间，服务端可能还持有流，并没有超时，客户端就会收到自己的userid,
                    // 如果客户端是固定userid就可以过滤掉，如果不是，等待服务端超时也会删除流
                    Log.d(TAG, "onRemotePublish: " + info.getUId() + " me : " + mUserid);
                    if(!mUserid.equals(info.getUId())){
                        mSteamList.add(info);
                        if (!sdkEngine.isAutoSubscribe()) {
                            sdkEngine.subscribe(info);
                        } else {
                            mSpinnerPopupWindowScribe.notifyUpdate();
                            refreshStreamInfoText();
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteUnPublish(CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, " onRemoteUnPublish " + info.getMediaType() + " " + info.getUId());
                    ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                            info.getUId() + " 取消媒体流 " + info.getMediaType());
                    String mkey = info.getUId() + info.getMediaType().toString();
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(mkey);
                    }

                    mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                    refreshStreamInfoText();
                }
            });
        }

        @Override
        public void onSubscribeResult(int code, String msg, CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        URTCVideoViewInfo vinfo = new URTCVideoViewInfo();
//                        UCloudRtcRenderView videoView = null;
                        CMCCSurfaceViewGroup videoView = null;
                        Log.d(TAG, " subscribe info: " + info);
                        if (info.isHasVideo()) {
                             //外部扩展输出，和默认输出二选一
//                            UCloudRtcSdkSurfaceVideoView videoViewCallBack = new UCloudRtcSdkSurfaceVideoView(getApplicationContext());
//                            videoViewCallBack.setFrameCallBack(mUcloudRTCDataReceiver);
//                            videoViewCallBack.init(false);
//                            sdkEngine.startRemoteView(info, videoViewCallBack);

//                             UCloudRtcSdkSurfaceVideoView 定义的viewgroup,内含UcloudRtcRenderView
                            videoView = new CMCCSurfaceViewGroup(getApplicationContext());
                            CMCCSurfaceViewRenderer cmccSurfaceViewRenderer = new CMCCSurfaceViewRenderer(getApplicationContext());
                            videoView.init(false, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio},cmccSurfaceViewRenderer);
                            videoView.setTag(info);
                            videoView.setId(R.id.video_view);
                            //设置交换
//                            videoView.setOnClickListener(mSwapRemoteLocalListener);
//                            //远端截图
                            videoView.setOnClickListener(mScreenShotOnClickListener);

                            //自定义的surfaceview
//                            videoView = new UCloudRtcRenderView(getApplicationContext());
//                            videoView.init();
//                            videoView.setTag(info);
//                            videoView.setOnClickListener(mScreenShotOnClickListener);
//                            mRemoteRenderView = new UCloudRtcRenderView(getApplicationContext());
//                            mRemoteRenderView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
//                            testT.addView(mRemoteRenderView);
//                            mRemoteRenderView.init();
//                            sdkEngine.startRemoteView(info, mRemoteRenderView,UCloudRtcSdkScaleType.UCLOUD_RTC_SDK_SCALE_ASPECT_FIT,null);
//                            mRemoteRenderView.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    testT.removeAllViews();
//                                    mRemoteRenderView.setLayoutParams(new ViewGroup.LayoutParams(-1,-1));
//                                    testB.addView(mRemoteRenderView);
//                                    mRemoteRenderView.resetSurface();
//                                }
//                            });
//                            mRemoteRenderView.setTag(info);
//                            mRemoteRenderView.setOnClickListener(mScreenShotOnClickListener);
                        }
                        vinfo.setmRenderview(videoView);
                        vinfo.setmUid(info.getUId());
                        vinfo.setmMediatype(info.getMediaType());
                        vinfo.setmEanbleVideo(info.isHasVideo());
                        vinfo.setEnableAudio(info.isHasAudio());
                        String mkey = info.getUId() + info.getMediaType().toString();
                        vinfo.setKey(mkey);
                        //默认输出，和外部输出代码二选一
                        if (mVideoAdapter != null) {
                            mVideoAdapter.addStreamView(mkey, vinfo, info);
                        }

                        if (vinfo != null && videoView != null) {
                            sdkEngine.setupRemoteVideo(info, videoView, CMCCScaleType.SCALE_ASPECT_FILL,null);
//                            videoView.refreshRemoteOp(View.VISIBLE);
                        }
                        //如果订阅成功就删除待订阅列表中的数据
                        mSpinnerPopupWindowScribe.removeStreamInfoByUid(info.getUId());
                        refreshStreamInfoText();
                    } else {
                        ToastUtils.shortShow(RoomActivity.this, " 订阅用户  " +
                                info.getUId() + " 流 " + info.getMediaType() + " 失败 " +
                                " code " + code + " msg " + msg);
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(int code, String msg, CMCCStreamInfo info) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.shortShow(RoomActivity.this, " 取消订阅用户 " +
                            info.getUId() + " 类型 " + info.getMediaType());
                    if (mVideoAdapter != null) {
                        mVideoAdapter.removeStreamView(info.getUId() + info.getMediaType().toString());
                    }
                    //取消订阅又变成可订阅
                    mSpinnerPopupWindowScribe.addStreamInfo(info, true);
                }
            });
        }

        @Override
        public void onLocalStreamMuteRsp(int code, String msg, CMCCMediaType mediaType, CMCCTrackType trackType, boolean mute) {
            Log.d(TAG, " code " + code + " mediatype " + mediaType + " ttype " + trackType + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        if (mediaType == MEDIA_TYPE_VIDEO) {
                            if (trackType == CMCCTrackType.TRACK_TYPE_AUDIO) {
                                onMuteMicResult(mute);
                            } else if (trackType == CMCCTrackType.TRACK_TYPE_VIDEO) {
                                onMuteCamResult(mute);
                            }
                        } else if (mediaType == CMCCMediaType.MEDIA_TYPE_SCREEN) {
                            onMuteCamResult(mute);
                        }
                    }
                }
            });
        }

        @Override
        public void onRemoteStreamMuteRsp(int code, String msg, String uid, CMCCMediaType mediatype, CMCCTrackType tracktype, boolean mute) {
            Log.d(TAG, " code " + code + " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 0) {
                        String mkey = uid + mediatype.toString();
                        Log.d(TAG, " onRemoteStreamMuteRsp " + mkey + " " + mVideoAdapter);
                        if (tracktype == CMCCTrackType.TRACK_TYPE_AUDIO) {
                            mRemoteAudioMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteAudio(mute);
                            }
//                            if (mMuteView == localrenderview) {
//                                int position = mVideoAdapter.getPositionByKey(mkey);
//                                View view = mRemoteGridView.getChildAt(position);
//                                UCloudRtcSdkSurfaceVideoView videoView = view.findViewById(R.id.video_view);
//                                videoView.refreshRemoteAudio(mute);
//                            } else {
//                                localrenderview.refreshRemoteAudio(mute);
//                            }
                        } else if (tracktype == CMCCTrackType.TRACK_TYPE_VIDEO) {
                            mRemoteVideoMute = mute;
                            if (mMuteView != null) {
                                mMuteView.refreshRemoteVideo(mute);
                            }
//                            if (mMuteView == localrenderview) {
//                                int position = mVideoAdapter.getPositionByKey(mkey);
//                                View view = mRemoteGridView.getChildAt(position);
//                                UCloudRtcSdkSurfaceVideoView videoView = view.findViewById(R.id.video_view);
//                                videoView.refreshRemoteVideo(mute);
//                            } else {
//                                localrenderview.refreshRemoteVideo(mute);
//                            }
                        }

                    } else {
                        ToastUtils.shortShow(RoomActivity.this, "mute " + mediatype + "failed with code: " + code);
                    }
                }
            });
        }

        @Override
        public void onRemoteTrackNotify(String uid, CMCCMediaType mediatype, CMCCTrackType tracktype, boolean mute) {
            Log.d(TAG, " uid " + uid + " mediatype " + mediatype + " ttype " + tracktype + " mute " + mute);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediatype == MEDIA_TYPE_VIDEO) {
                        String cmd = mute ? "关闭" : "打开";
                        if (tracktype == CMCCTrackType.TRACK_TYPE_AUDIO) {
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 麦克风");
                        } else if (tracktype == CMCCTrackType.TRACK_TYPE_VIDEO) {
                            ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                    uid + cmd + " 摄像头");
                        }

                    } else if (mediatype == CMCCMediaType.MEDIA_TYPE_SCREEN) {
                        String cmd = mute ? "关闭" : "打开";
                        ToastUtils.shortShow(RoomActivity.this, " 用户 " +
                                uid + cmd + " 桌面流");
                    }
                }
            });
        }

        @Override
        public void onSendStreamStatus(CMCCStreamStatus streamStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onRemoteStreamStatus(CMCCStreamStatus rtstats) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onLocalAudioLevel(int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localprocess.setProgress(volume);
                }
            });
        }

        @Override
        public void onRemoteAudioLevel(String uid, int volume) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mVideoAdapter != null) {
                        String mkey = uid + MEDIA_TYPE_VIDEO.toString();
                    }
                }
            });
        }

        @Override
        public void onKickoff(int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.longShow(RoomActivity.this, " 被踢出会议 code " +
                            code);
                    Log.d(TAG, " user kickoff reason " + code);
                    Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
                    onMediaServerDisconnect();
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onWarning(int warn) {

        }

        @Override
        public void onError(int error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (error == CMCCErrorCode.NET_ERR_SDP_SWAP_FAIL.ordinal()) {
                        ToastUtils.shortShow(RoomActivity.this, "sdp swap failed");
                    }
                }
            });
        }

        @Override
        public void onQueryMix(int code, String msg, int type, String mixId, String fileName) {
            Log.d(TAG, "onQueryMix: "+ code + " msg: "+ msg + " type: "+ type);
        }

        @Override
        public void onRecordStatusNotify(CMCCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String fileName) {
            if(status == CMCCMediaServiceStatus.RECORD_STATUS_START){
                String videoPath = "http://"+ mBucket + "."+ mRegion +".ufileos.com/" + fileName;
                Log.d(TAG,"remote record path: " +  videoPath+".mp4");
                ToastUtils.longShow(RoomActivity.this, "观看地址: " +videoPath );
                mIsRecording = true;
                mOpBtn.setText("stop record");
                if(mAtomOpStart)
                    mAtomOpStart = false;
            }else if(status == CMCCMediaServiceStatus.RECORD_STATUS_STOP_REQUEST_SEND){
                ToastUtils.longShow(RoomActivity.this, "录制结束: " + (code == NET_ERR_CODE_OK.ordinal()?"成功":"失败: "+ code));
                if(mIsRecording){
                    mIsRecording = false;
                    mOpBtn.setText("start record");
                }
            }else {
                ToastUtils.longShow(RoomActivity.this, "录制异常: 原因：" +code );
            }
        }

        @Override
        public void onRelayStatusNotify(CMCCMediaServiceStatus status, int code, String msg, String userId, String roomId, String mixId, String[] pushUrls) {
            if(status == CMCCMediaServiceStatus.RELAY_STATUS_START){
                mIsMixing = true;
                mOpBtn.setText("stop mix");
                if(mAtomOpStart)
                    mAtomOpStart = false;
            }else if(status == CMCCMediaServiceStatus.RELAY_STATUS_STOP_REQUEST_SEND){
                Log.d(TAG,"onMixStop: " + code + "msg: "+ msg + " pushUrl: "+ pushUrls);
                if(mIsMixing){
                    mIsMixing = false;
                    mOpBtn.setText("mix");
                }
            }else{
                ToastUtils.longShow(RoomActivity.this, "转推异常: 原因：" +code );
            }
        }

        @Override
        public void onAddStreams(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onAddStreams: "+ code + msg);
                }
            });
        }

        @Override
        public void onDelStreams(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onDelStreams: "+ code + msg);
                }
            });
        }

        @Override
        public void onLogOffUsers(int code, String msg) {

        }

        @Override
        public void onMessageNotify(int code, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onMsgNotify: code: " + code + "msg: " + msg);
                }
            });
        }

        @Override
        public void onLogOffNotify(int cmdType, String userId) {

        }

        @Override
        public void onServerBroadcastMessage(String uid, String msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "onServerBroadCastMsg: uid: " + uid + "msg: " + msg);
                }
            });
        }

        @Override
        public void onAudioRouteChanged(CMCCAudioDevice device) {
            defaultAudioDevice = device;
//            URTCLogUtils.d(TAG,"URTCAudioManager: room change device to "+ defaultAudioDevice);
            if (defaultAudioDevice == CMCCAudioDevice.AUDIO_DEVICE_SPEAKER) {
                mLoudSpkeader.setImageResource(R.mipmap.loudspeaker);
                mSpeakerOn = true;
            } else {
                mSpeakerOn = false;
                mLoudSpkeader.setImageResource(R.mipmap.loudspeaker_disable);
            }
        }

        @Override
        public void onPeerLostConnection(int type, CMCCStreamInfo info) {
            Log.d(TAG, "onPeerLostConnection: type: " + type + "info: " + info);
        }

        @Override
        public void onNetWorkQuality(String userId, CMCCStreamType streamType, CMCCMediaType mediaType, CMCCNetWorkQuality quality) {
            Log.d(TAG, "onNetWorkQuality: userid: " + userId + "streamType: " + streamType + "mediatype : "+ mediaType + " quality: " + quality);
        }

        @Override
        public void onAudioFileFinish() {
            Log.d(TAG, "onAudioFileFinish" );
        }
    };
    private int mSelectPos;

    private void onUserLeave(String uid) {
//        if (mVideoAdapter != null) {
//            mVideoAdapter.removeStreamView(uid + UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
//            mVideoAdapter.removeStreamView(uid + UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN);
//        }
    }

    private void onMediaServerDisconnect() {
        localrenderview.release();
        clearGridItem();
//        UCloudRtcSdkEngine.destory();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_room);
        testT = findViewById(R.id.test_t);
        testB = findViewById(R.id.test_bottom);
        mSeekBar = findViewById(R.id.seek_volume);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                sdkEngine.adjustRecordingSignalVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//        mVideoPlayer = findViewById(R.id.playView);
//        mVideoPlayer.setVideoListener(this);
//        mVideoPlayer.setPath("http://video.zhihuishu.com/zhs_yufa_150820/aidedteaching/COURSE_FOLDER/202002/47dd76d15b5348839fcfa78b104e886e_64.mp3");
//        try {
//            mVideoPlayer.load();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        timeshow = findViewById(R.id.timer);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name),
                Context.MODE_PRIVATE);
        //mCaptureMode = preferences.getInt(CommonUtils.capture_mode, CommonUtils.camera_capture_mode);
        mVideoProfile = preferences.getInt(CommonUtils.videoprofile, CommonUtils.videoprofilesel);
        mRemoteGridView = findViewById(R.id.remoteGridView);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_L);
        } else {
            gridLayoutManager = new GridLayoutManager(this, COL_SIZE_P);
        }
        mRemoteGridView.setLayoutManager(gridLayoutManager);
        mVideoAdapter = new RemoteVideoAdapter(this);
        mVideoAdapter.setRemoveRemoteStreamReceiver(mRemoveRemoteStreamReceiver);
        mRemoteGridView.setAdapter(mVideoAdapter);
        sdkEngine = CMCCRtcEngine.create(eventListener);
//        sdkEngine = UCloudRtcApplication.getInstance().createRtcEngine(eventListener);
        mUserid = getIntent().getStringExtra("user_id");
        mRoomid = getIntent().getStringExtra("room_id");
        mRoomToken = getIntent().getStringExtra("token");
        mAppid = getIntent().getStringExtra("app_id");
        mHangup = findViewById(R.id.button_call_disconnect);
        mSwitchcam = findViewById(R.id.button_call_switch_camera);
        mMuteMic = findViewById(R.id.button_call_toggle_mic);
        mLoudSpkeader = findViewById(R.id.button_call_loundspeaker);
        mMuteCam = findViewById(R.id.button_call_toggle_cam);
        mStreamSelect = findViewById(R.id.stream_select);
        mTextStream = findViewById(R.id.stream_text_view);
        refreshStreamInfoText();
        mOpBtn = findViewById(R.id.opBtn);
        //user can chose the suitable type
//        mOpBtn.setTag(OP_SEND_MSG);
//        mOpBtn.setText("sendmsg");
        mOpBtn.setTag(OP_LOCAL_RECORD);
        mOpBtn.setText("lrecord");
//        mOpBtn.setTag(OP_REMOTE_RECORD);
//        mOpBtn.setText("record");
//        mOpBtn.setTag(OP_MIX);
//        mOpBtn.setText("mix");
        //mOpBtn.setTag(OP_MIX_MANUAL);
        //mOpBtn.setText("mix_manual");
        mAddDelBtn = findViewById(R.id.addDelBtn);
        mAddDelBtn.setText("add_st");
        mAddDelBtn.setVisibility(View.VISIBLE);
        mCheckBoxMirror = findViewById(R.id.cb_mirror);
        mCheckBoxMirror.setChecked(CMCCEnvHelper.isFrontCameraMirror());
        mCheckBoxMirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CMCCEnvHelper.setFrontCameraMirror(isChecked);
            }
        });
        mOpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnOp btnOp = (BtnOp)mOpBtn.getTag();
                switch (btnOp){
                    case OP_SEND_MSG:
                        sdkEngine.messageNotify("hi");
                        break;
                    case OP_LOCAL_RECORD:
                        if(!mLocalRecordStart){
                            Log.d(TAG, " start local record: ");
//                        URTCRecordManager.getInstance().startRecord(UCloudRtcSdkRecordType.U_CLOUD_RTC_SDK_RECORD_TYPE_MP4,System.currentTimeMillis()+"",mLocalRecordListener,1000);
                            URTCRecordManager.getInstance().startRecord(CMCCRecordType.RECORD_TYPE_MP4,"mnt/sdcard/urtc/mp4/"+ System.currentTimeMillis()+".mp4",mLocalRecordListener,1000);
                            mLocalRecordStart = true;
                        }else{
                            Log.d(TAG, " stop local record: ");
                            URTCRecordManager.getInstance().stopRecord();
                            mLocalRecordStart = false;
                        }
                        break;
                    case OP_REMOTE_RECORD:
                        if (!mIsRecording) {
                            mAtomOpStart = true;
                            // 生成录制配置
                            CMCCMixProfile mixProfile = (CMCCMixProfile)CMCCMixProfile.getInstance().assembleRecordMixParamsBuilder()
                                    .type(CMCCMixProfile.MIX_TYPE_RECORD)
                                    //画面模式
                                    .layout(CMCCMixProfile.LAYOUT_AVERAGE_1)
                                    //画面分辨率
                                    .resolution(1280, 720)
                                    //背景色
                                    .bgColor(0, 0, 0)
                                    //画面帧率
                                    .frameRate(15)
                                    //画面码率
                                    .bitRate(1000)
                                    //h264视频编码
                                    .videoCodec(CMCCMixProfile.VIDEO_CODEC_H264)
                                    //编码质量
                                    .qualityLevel(CMCCMixProfile.QUALITY_H264_CB)
                                    //音频编码
                                    .audioCodec(CMCCMixProfile.AUDIO_CODEC_AAC)
                                    //主讲人ID
                                    .mainViewUserId(mUserid)
                                    //主讲人媒体类型
                                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                                    //加流方式手动
                                    .addStreamMode(CMCCMixProfile.ADD_STREAM_MODE_AUTO)
                                    //添加流列表，也可以后续调用MIX_TYPE_UPDATE 动态添加
                                    .addStream(mUserid, MEDIA_TYPE_VIDEO.ordinal())
                                    .build();
                            sdkEngine.startRecord(mixProfile); // 开始录制
                        } else if(!mAtomOpStart){
                            mAtomOpStart = true;
                            sdkEngine.stopRecord();
                        }
                        break;
                    case OP_MIX:
                        if (!mIsMixing) {
                            mAtomOpStart = true;
                            // 生成转推配置
                            CMCCMixProfile mixProfile = (CMCCMixProfile)CMCCMixProfile.getInstance().assembleUpdateMixParamsBuilder()
                                    .type(CMCCMixProfile.MIX_TYPE_RELAY)
                                    //画面模式
                                    .layout(CMCCMixProfile.LAYOUT_CLASS_ROOM_2)
                                    //画面分辨率
                                    .resolution(1280, 720)
                                    //背景色
                                    .bgColor(0, 0, 0)
                                    //画面帧率
                                    .frameRate(15)
                                    //画面码率
                                    .bitRate(1000)
                                    //h264视频编码
                                    .videoCodec(CMCCMixProfile.VIDEO_CODEC_H264)
                                    //编码质量
                                    .qualityLevel(CMCCMixProfile.QUALITY_H264_CB)
                                    //音频编码
                                    .audioCodec(CMCCMixProfile.AUDIO_CODEC_AAC)
                                    //主讲人ID
                                    .mainViewUserId(mUserid)
                                    //主讲人媒体类型
                                    .mainViewMediaType(MEDIA_TYPE_VIDEO.ordinal())
                                    //加流方式手动
                                    .addStreamMode(CMCCMixProfile.ADD_STREAM_MODE_MANUAL)
                                    //添加流列表，也可以后续调用MIX_TYPE_UPDATE 动态添加
                                    .addStream(mUserid, MEDIA_TYPE_VIDEO.ordinal())
                                    //设置转推cdn 的地址
                                    .addPushUrl("rtmp://rtcpush.ugslb.com/rtclive/" + mRoomid)
                                    //关键用户
                                    .keyUser(mUserid)
                                    //流上限
                                    .layoutUserLimit(2)
                                    //房间没流多久结束任务
                                    .taskTimeOut(70)
                                    .build();
                            sdkEngine.startRelay(mixProfile); // 开始转推
                        } else if (!mAtomOpStart) {
                            mAtomOpStart = true;
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put("");
                        }
                        break;
                    case OP_MIX_MANUAL:
                        if (!mIsMixing) {
                            mAtomOpStart = true;
                        } else if (!mAtomOpStart) {
                            mAtomOpStart = true;
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put("");
                            //sdkEngine.stopMix(UCloudRtcSdkMixProfile.MIX_TYPE_BOTH,"rtmp://rtcpush.ugslb.com/rtclive/"+mRoomid);
                        }
                        break;
                }
            }
        });
        //动态增加流或者删除混流
        mAddDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if(mMixAddOrDel){
                   mMixAddOrDel = false;
                   mAddDelBtn.setText("del_st");
                   CMCCStreamInfo info = mVideoAdapter.getStreamInfo(0);
                   Log.d(TAG, "add stream: " + info);
                   JSONArray streams = new JSONArray();
                   JSONObject remote = new JSONObject();
                   try {
                       remote.put("user_id",info.getUId());
                       remote.put("media_type",info.getMediaType().ordinal());
                       streams.put(remote);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
                   //sdkEngine.addMixStream(info.getUId(), info.getMediaType().ordinal());
               }else{
                   mMixAddOrDel = true;
                   mAddDelBtn.setText("add_st");
                   CMCCStreamInfo info = mVideoAdapter.getStreamInfo(0);
                   Log.d(TAG, "del stream: " + info);
                   JSONArray streams = new JSONArray();
                   JSONObject remote = new JSONObject();
                   try {
                       remote.put("user_id",info.getUId());
                       remote.put("media_type",info.getMediaType().ordinal());
                       streams.put(remote);
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }
                   //sdkEngine.delMixStream(info.getUId(), info.getMediaType().ordinal());
               }
            }
        });

        mTextStream.setOnClickListener(new CustomerClickListener() {
            @Override
            protected void onSingleClick() {
                showPopupWindow();
            }

            @Override
            protected void onFastClick() {

            }
        });
        mSteamList = new ArrayList<>();
        mSpinnerPopupWindowScribe = new SteamScribePopupWindow(this, mSteamList);
        mSpinnerPopupWindowScribe.setAnimationStyle(0);
        mSpinnerPopupWindowScribe.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
            }
        });
        ((SteamScribePopupWindow) mSpinnerPopupWindowScribe).setmOnSubScribeListener(mOnSubscribeListener);
        //手动发布
        mPublish = findViewById(R.id.button_call_pub);
        mPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsPublished) {
                    sdkEngine.setClientRole(CMCCClientRole.CLIENT_ROLE_BROADCASTER);
                    List<Integer> results = new ArrayList<>();
                    StringBuffer errorMessage = new StringBuffer();
                    if (mScreenEnable && !mCameraEnable && !mMicEnable) {
                        if (isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        }
                        else {
                            errorMessage.append("设备不支持屏幕捕捉\n");
                            results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                        }
                    }
                    else if (mScreenEnable || mCameraEnable || mMicEnable) {
                        if (mScreenEnable && isScreenCaptureSupport) {
                            results.add(sdkEngine.publish(MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                        }
                        results.add(sdkEngine.publish(MEDIA_TYPE_VIDEO, mCameraEnable, mMicEnable).getErrorCode());
                    }
                    else {
                        errorMessage.append("Camera, Mic or Screen is disable!\n");
                    }
/*                    switch (mCaptureMode) {
                        //音频
                        case CommonUtils.audio_capture_mode:
                            results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            break;
                        //视频
                        case CommonUtils.camera_capture_mode:
                            results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            break;
                        //屏幕捕捉
                        case CommonUtils.screen_capture_mode:
                            if (isScreenCaptureSupport) {
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                            } else {
                                errorMessage.append("设备不支持屏幕捕捉\n");
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            }
                            break;
                        //音频+屏幕捕捉
                        case CommonUtils.screen_Audio_mode:
                            if (isScreenCaptureSupport) {
                                //推一路桌面一路音频,桌面流不需要带音频
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN, false, false).getErrorCode());
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            } else {
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, false, true).getErrorCode());
                            }
                            break;
                        //视频+屏幕捕捉
                        case CommonUtils.multi_capture_mode:
                            if (isScreenCaptureSupport) {
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN, true, false).getErrorCode());
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            } else {
                                results.add(sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true).getErrorCode());
                            }
                            break;
                    }*/

//            List<Integer> errorCodes = results.stream()
//                    .filter(result -> result != 0)
//                    .collect(Collectors.toList());
                    List<Integer> errorCodes = new ArrayList<>();
                    for (Integer result : results) {
                        if (result != 0)
                            errorCodes.add(result);
                    }
                    if (!errorCodes.isEmpty()) {
                        for (Integer errorCode : errorCodes) {
                            if (errorCode != NET_ERR_CODE_OK.ordinal())
                                errorMessage.append("UCLOUD_RTC_SDK_ERROR_CODE:" + errorCode + "\n");
                        }
                    }
                    if (errorMessage.length() > 0)
                        ToastUtils.shortShow(RoomActivity.this, errorMessage.toString());
                    else {
                        ToastUtils.shortShow(RoomActivity.this, "发布");
                    }
                } else {
                    sdkEngine.unPublish(mPublishMediaType);
                }
            }
        });
        mHangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callHangUp();
            }
        });

        mSwitchcam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        mMuteMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleMic();
            }
        });

        mLoudSpkeader.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 onLoudSpeaker(!mSpeakerOn);
                                             }
                                         }
        );

        mMuteCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleCamera();
            }
        });

        title = findViewById(R.id.text_room);
        title.setText("roomid: " + mRoomid);
        //title.setText("roomid: "+mRoomid+"\nuid: "+ mUserid);

        localrenderview = findViewById(R.id.localview);
//        localrenderview.init(true, new int[]{R.mipmap.video_open, R.mipmap.loudspeaker, R.mipmap.video_close, R.mipmap.loudspeaker_disable, R.drawable.publish_layer}, mOnRemoteOpTrigger, new int[]{R.id.remote_video, R.id.remote_audio});
//        localrenderview.init(true);
        localrenderview.init();
        localrenderview.setZOrderMediaOverlay(false);
        localrenderview.setMirror(true);
        localprocess = findViewById(R.id.processlocal);
        isScreenCaptureSupport = CMCCEnvHelper.isSupportScreenCapture();
        mCameraEnable = preferences.getBoolean(CommonUtils.CAMERA_ENABLE, CommonUtils.CAMERA_ON);
        mMicEnable = preferences.getBoolean(CommonUtils.MIC_ENABLE, CommonUtils.MIC_ON);
        mScreenEnable = preferences.getBoolean(CommonUtils.SCREEN_ENABLE, CommonUtils.SCREEN_OFF);
//        Log.d(TAG, " mCaptureMode " + mCaptureMode);
        Log.d(TAG, " Camera enable is: " + mCameraEnable + " Mic enable is: " + mMicEnable + " ScreenShare enable is: " + mScreenEnable);
        if (!mScreenEnable && !mCameraEnable && mMicEnable) {
            sdkEngine.setAudioOnlyMode(true);
        }
        else {
            sdkEngine.setAudioOnlyMode(false);
        }
        sdkEngine.configLocalCameraPublish(mCameraEnable);
        sdkEngine.configLocalAudioPublish(mMicEnable);
        if (isScreenCaptureSupport) {
            sdkEngine.configLocalScreenPublish(mScreenEnable);
        }
        else {
            sdkEngine.configLocalScreenPublish(false);
        }
/*            switch (mCaptureMode) {
            case CommonUtils.audio_capture_mode:
                sdkEngine.setAudioOnlyMode(true);
                sdkEngine.configLocalCameraPublish(false);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.camera_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                sdkEngine.configLocalCameraPublish(true);
                sdkEngine.configLocalAudioPublish(true);
                sdkEngine.configLocalScreenPublish(false);
                break;
            case CommonUtils.screen_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(false);
                } else {
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                    sdkEngine.configLocalScreenPublish(false);
                }
                break;
            case CommonUtils.screen_Audio_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(false);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
            case CommonUtils.multi_capture_mode:
                sdkEngine.setAudioOnlyMode(false);
                if (isScreenCaptureSupport) {
                    sdkEngine.configLocalScreenPublish(true);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                } else {
                    sdkEngine.configLocalScreenPublish(false);
                    sdkEngine.configLocalCameraPublish(true);
                    sdkEngine.configLocalAudioPublish(true);
                }
                break;
        }*/

        defaultAudioDevice = sdkEngine.getDefaultAudioDevice();
//        URTCLogUtils.d(TAG,"URTCAudioManager audio device room with: "+defaultAudioDevice);
        if (defaultAudioDevice == CMCCAudioDevice.AUDIO_DEVICE_SPEAKER) {
            mLoudSpkeader.setImageResource(R.mipmap.loudspeaker);
            mSpeakerOn = true;
        } else {
            mSpeakerOn = false;
            mLoudSpkeader.setImageResource(R.mipmap.loudspeaker_disable);
        }
        int role = preferences.getInt(CommonUtils.SDK_STREAM_ROLE, CMCCClientRole.CLIENT_ROLE_BROADCASTER.ordinal());
        mRole = CMCCClientRole.valueOf(role);
        sdkEngine.setClientRole(mRole);
        int classType = preferences.getInt(CommonUtils.SDK_CLASS_TYPE, CMCCChannelProfile.CHANNEL_PROFILE_COMMUNICATION.ordinal());
        mClass = CMCCChannelProfile.valueOf(classType);
        sdkEngine.setChannelProfile(mClass);
        mPublishMode = preferences.getInt(CommonUtils.PUBLISH_MODE, CommonUtils.AUTO_MODE);
        sdkEngine.setAutoPublish(mPublishMode == CommonUtils.AUTO_MODE ? true : false);
        mScribeMode = preferences.getInt(CommonUtils.SUBSCRIBE_MODE, CommonUtils.AUTO_MODE);
        if (mScribeMode == CommonUtils.AUTO_MODE) {
            mStreamSelect.setVisibility(View.GONE);
        } else {
            mStreamSelect.setVisibility(View.VISIBLE);
        }
        sdkEngine.setAutoSubscribe(mScribeMode == CommonUtils.AUTO_MODE ? true : false);
        //设置sdk 外部扩展模式及其采集的帧率，同时sdk内部会自动调整初始码率和最小码率
        //扩展模式只支持720p的分辨率及以下，若要自定义更高分辨率，请联系Ucloud商务定制，否则sdk会抛出异常，终止运行。
//        sdkEngine.setVideoProfile(UCloudRtcSdkVideoProfile.UCLOUD_RTC_SDK_VIDEO_PROFILE_EXTEND.extendParams(30,640,480));
        sdkEngine.setVideoEncoderConfiguration(CMCCVideoProfile.matchValue(mVideoProfile));

        initButtonSize();
        CMCCAuthInfo info = new CMCCAuthInfo();
        info.setAppId(mAppid);
        info.setToken(mRoomToken);
        info.setRoomId(mRoomid);
        info.setUId(mUserid);
        Log.d(TAG, " roomtoken = " + mRoomToken);
        //普通摄像头捕获方式，与扩展模式二选一
        CMCCEnvHelper.setCaptureMode(
                CMCCCaptureMode.CAPTURE_MODE_LOCAL);
        //rgb数据捕获，与普通捕获模式二选一
//        UCloudRtcSdkEnv.setCaptureMode(
//                UCloudRtcSdkCaptureMode.UCLOUD_RTC_CAPTURE_MODE_EXTEND);
//        TimerTask timerTask = new TimerTask() {
//            @Override
//            public void run() {
//                try{
//                    RGBSourceData sourceData;
//                    Bitmap bitmap = null;
//                    int type;
//                    if(mPFlag){
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                        bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.img1_640,options);
//                        type = UcloudRTCDataProvider.RGBA_TO_I420;
//                    }
//                    else{
//                        BitmapFactory.Options options = new BitmapFactory.Options();
//                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//                        bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.img_640,options);
//                        type = UcloudRTCDataProvider.RGBA_TO_I420;
//                    }
//                    mPFlag = !mPFlag;
////                            if(++mPictureFlag >50)
////                                mPictureFlag = 0;
//                    if(bitmap != null){
//                        sourceData = new RGBSourceData(bitmap,bitmap.getWidth(),bitmap.getHeight(),type);
//                        //add rgbdata
//                        mQueue.put(sourceData);
////                                Log.d(TAG, "create bitmap: " + bitmap + "count :" + memoryCount.incrementAndGet());
//                    }
////                            }
////                        }
//
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        };

        Runnable imgTask = new Runnable() {
            @Override
            public void run() {
                    while(startCreateImg){
                        try{
                            RGBSourceData sourceData;
                            Bitmap bitmap = null;
                            int type;
                            if(mPFlag){
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.pic_1080_1,options);
                                type = CMCCDataProvider.RGBA_TO_I420;
                            }
                            else{
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.pic_1080_2,options);
                                type = CMCCDataProvider.RGBA_TO_I420;
                            }
                            mPFlag = !mPFlag;
//                            if(++mPictureFlag >50)
//                                mPictureFlag = 0;
                            if(bitmap != null){
                                sourceData = new RGBSourceData(bitmap,bitmap.getWidth(),bitmap.getHeight(),type);
                                //add rgbdata
                                mQueue.put(sourceData);
//                                Log.d(TAG, "create bitmap: " + bitmap + "count :" + memoryCount.incrementAndGet());
                            }
//                            }
//                        }

                             Thread.sleep((int)(Math.random()*20));
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        //可以添加nv21 的数据,请根据实际情况拿到bytebuffer的数据,图像宽高
//                        try {
//                            ByteBuffer byteBuffer = null;
//                            NVSourceData nvSourceData = new NVSourceData(byteBuffer,1280,720,UcloudRTCDataProvider.NV21);
//                            mQueueNV.put(nvSourceData);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                    }
                    //这里在回收一遍 防止队列不阻塞了在destroy以后又产生了bitmap没回收
                    while(mQueue.size() != 0 ){
                        RGBSourceData rgbSourceData = mQueue.poll();
                        if(rgbSourceData != null){
                            recycleBitmap(rgbSourceData.getSrcData());
                            rgbSourceData.srcData = null;
                            rgbSourceData = null;
                        }
                    }
            }
        };

        if(CMCCEnvHelper.getCaptureMode() == CMCCCaptureMode.CAPTURE_MODE_EXTEND &&
                (mRole == CMCCClientRole.CLIENT_ROLE_BROADCASTER ||
                        mRole == CMCCClientRole.CLIENT_ROLE_PUBLISHER)){

            mCreateImgThread = new Thread(imgTask);
            mCreateImgThread.setName("create picture");
            mCreateImgThread.start();
//            mTimerCreateImg.scheduleAtFixedRate(timerTask,0,10);
            CMCCRtcEngine.onRGBCaptureResult(mCMCCDataProvider);
        }
        sdkEngine.joinChannel(info);
        initRecordManager();
    }

    private void recycleBitmap(Bitmap bitmap){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
//            Log.d(TAG, "recycleBitmap: " + bitmap + "count: "+ (memoryCount.decrementAndGet()));
        }
    }

    private CMCCScreenShot mCMCCScreenShot = new CMCCScreenShot() {
        @Override
        public void onReceiveRGBAData(ByteBuffer rgbBuffer, int width, int height) {
            final Bitmap bitmap = Bitmap.createBitmap(width * 1, height * 1, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(rgbBuffer);
            String name = "/mnt/sdcard/urtcscreen_"+System.currentTimeMillis() +".jpg";
            File file = new File(name);
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "screen shoot : " + name);
            ToastUtils.shortShow(RoomActivity.this,"screen shoot : " + name);
        }
    };

    private void addScreenShotCallBack(View view){
        if(view instanceof CMCCSurfaceViewGroup){
            ((CMCCSurfaceViewGroup)view).setScreenShotBack(mCMCCScreenShot);
        }else if(view instanceof CMCCSurfaceViewRenderer){
            ((CMCCSurfaceViewRenderer)view).setScreenShotBack(mCMCCScreenShot);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
//        boolean hasSwap = false;
//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//            URTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
//            View videoView = info.getmRenderview();
//            UCloudRtcSdkStreamInfo videoViewStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag();
//            UCloudRtcSdkStreamInfo videoViewSwapStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag(R.id.swap_info);
//            if (videoView != null && videoViewStreamInfo != null) {
//                if (videoViewSwapStreamInfo != null) {
//                    //恢复交换后的小窗本地视频
//                    sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), videoView,null,null);
//                    //恢复交换后的大窗远程视频
//                    sdkEngine.startRemoteView(videoViewStreamInfo, localrenderview,null,null);
//                    hasSwap = true;
//                } else {
//                    sdkEngine.startRemoteView(videoViewStreamInfo, videoView,null,null);
//                }
//            }
//        }
//        if (!hasSwap) {
//            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
////            sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true);
//        } if (!hasSwap) {
//            sdkEngine.startPreview(mLocalStreamInfo.getMediaType(), localrenderview,null,null);
////            sdkEngine.publish(UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO, true, true);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "on Stop");
        if(mIsPublished){
//            Intent service = new Intent(this, UCloudRtcForeGroundService.class);
//            startService(service);
            sdkEngine.disableAudio();
            sdkEngine.enableLocalVideo(false);
        }

//        for (String key : mVideoAdapter.getStreamViews().keySet()) {
//            URTCVideoViewInfo info = mVideoAdapter.getStreamViews().get(key);
//            View videoView = info.getmRenderview();
//            UCloudRtcSdkStreamInfo videoViewStreamInfo = (UCloudRtcSdkStreamInfo) videoView.getTag();
//            if (videoView != null && videoViewStreamInfo != null) {
//                sdkEngine.stopRemoteView(videoViewStreamInfo);
//            }
//        }
//        if (mLocalStreamInfo != null)
//            sdkEngine.stopPreview(mLocalStreamInfo.getMediaType());
    }


    //    SteamScribePopupWindow.OnSpinnerItemClickListener mOnSubscribe = new SteamScribePopupWindow.OnSpinnerItemClickListener() {
//        @Override
//        public void onItemClick(int pos) {
//            mSelectPos = pos;
//            mTextStream.setText(pos);
//            mSpinnerPopupWindowScribe.dismiss();
//        }
//    };

    //手动订阅
    SteamScribePopupWindow.OnSubscribeListener mOnSubscribeListener = new SteamScribePopupWindow.OnSubscribeListener() {
        @Override
        public void onSubscribe(List<CMCCStreamInfo> dataInfo) {
            for (CMCCStreamInfo streamInfo : dataInfo) {
                CMCCErrorCode result = sdkEngine.subscribe(streamInfo);
                if (result.ordinal() != NET_ERR_CODE_OK.ordinal()) {
                    ToastUtils.shortShow(RoomActivity.this, "UCLOUD_RTC_SDK_ERROR_CODE:" + result.getErrorCode());
                }
            }
            mSpinnerPopupWindowScribe.dismiss();
        }
    };

    private void showPopupWindow() {
        if (!mSpinnerPopupWindowScribe.isShowing()) {
            mSpinnerPopupWindowScribe.setWidth(mTextStream.getWidth());
            mSpinnerPopupWindowScribe.showAsDropDown(mTextStream);
        }
    }

    private void initButtonSize() {
        int screenWidth = UiHelper.getScreenPixWidth(this);
        int leftRightMargin = UiHelper.dipToPx(this, 30 * 2);
        int gap = UiHelper.dipToPx(this, 8);
        int buttonSize;
        if (mPublishMode == CommonUtils.AUTO_MODE) {
            buttonSize = (screenWidth - leftRightMargin - gap * 4) / 5;
            mPublish.setVisibility(View.GONE);
        } else {
            buttonSize = (screenWidth - leftRightMargin - gap * 5) / 6;
            mPublish.setVisibility(View.VISIBLE);
            setButtonSize(mPublish, buttonSize);
        }
        setButtonSize(mHangup, buttonSize);
        setButtonSize(mLoudSpkeader, buttonSize);
        setButtonSize(mSwitchcam, buttonSize);
        setButtonSize(mMuteCam, buttonSize);
        setButtonSize(mMuteMic, buttonSize);
    }

    private void setButtonSize(View button, int buttonSize) {
        button.getLayoutParams().width = buttonSize;
        button.getLayoutParams().height = buttonSize;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Intent service = new Intent(this, UCloudRtcForeGroundService.class);
//        stopService(service);
        sdkEngine.enableAudio();
        sdkEngine.enableLocalVideo(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "activity destory");
        super.onDestroy();
        localrenderview.release();
        clearGridItem();
        mVideoAdapter.setRemoveRemoteStreamReceiver(null);
        mCMCCDataProvider.releaseBuffer();
        mCMCCDataProvider = null;
        mDataReceiver.releaseBuffer();
        mDataReceiver = null;
        if(CMCCEnvHelper.getCaptureMode() == CMCCCaptureMode.CAPTURE_MODE_EXTEND &&
                (mRole == CMCCClientRole.CLIENT_ROLE_BROADCASTER ||
                        mRole == CMCCClientRole.CLIENT_ROLE_PUBLISHER)) {
            startCreateImg = false;
            //这里回收一遍
            while(mQueue.size() != 0 ){
                RGBSourceData rgbSourceData = mQueue.poll();
                if(rgbSourceData != null){
                    recycleBitmap(rgbSourceData.getSrcData());
                    rgbSourceData.srcData = null;
                    rgbSourceData = null;
                }

            }
        }
//        UCloudRtcSdkEngine.destory();
//        if(mVideoPlayer != null ){
//            mVideoPlayer.stop();
//        }
        System.gc();
    }

    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private void callHangUp() {
        int ret = sdkEngine.leaveChannel().ordinal();
//        if (ret != NET_ERR_CODE_OK.ordinal()) {
            Intent intent = new Intent(RoomActivity.this, ConnectActivity.class);
            onMediaServerDisconnect();
            startActivity(intent);
            finish();
//        }
    }

    boolean mSwitchCam = false;

    private void switchCamera() {
        sdkEngine.switchCamera();
        ToastUtils.shortShow(this, "切换摄像头");
//        mSwitchcam.setImageResource(mSwitchCam ? R.mipmap.camera_switch_front :
//                R.mipmap.camera_switch_end);
        mSwitchCam = !mSwitchCam;
    }

    boolean mMuteMicBool = false;

    private boolean onToggleMic() {
        sdkEngine.muteLocalAudioStream(!mMuteMicBool);
        if (!mMuteMicBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭麦克风");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开麦克风");
        }
        return false;
    }

    boolean mMuteCamBool = false;

    private boolean onToggleCamera() {
/*        if (mCaptureMode == CommonUtils.camera_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        } else if (mCaptureMode == CommonUtils.screen_capture_mode) {
            if (isScreenCaptureSupport) {
                sdkEngine.muteLocalVideo(!mMuteCamBool, UCloudRtcSdkMediaType.UCLOUD_RTC_SDK_MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
            }
        } else if (mCaptureMode == CommonUtils.multi_capture_mode) {
            sdkEngine.muteLocalVideo(!mMuteCamBool, UCLOUD_RTC_SDK_MEDIA_TYPE_VIDEO);
        }*/
        if (mScreenEnable || mCameraEnable) {
            if (isScreenCaptureSupport && !mCameraEnable) {
                sdkEngine.muteLocalVideoStream(!mMuteCamBool, CMCCMediaType.MEDIA_TYPE_SCREEN);
            } else {
                sdkEngine.muteLocalVideoStream(!mMuteCamBool, MEDIA_TYPE_VIDEO);
            }
        }
        if (!mMuteCamBool) {
            ToastUtils.shortShow(RoomActivity.this, "关闭摄像头");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开摄像头");
        }
        return false;
    }

    private void onMuteCamResult(boolean mute) {
        mMuteCamBool = mute;
        mMuteCam.setImageResource(mute ? R.mipmap.video_close : R.mipmap.video_open);
        if (localrenderview.getTag(R.id.swap_info) != null) {
            CMCCStreamInfo remoteInfo = (CMCCStreamInfo) localrenderview.getTag(R.id.swap_info);
            String mkey = remoteInfo.getUId() + remoteInfo.getMediaType().toString();
            View view = mRemoteGridView.getChildAt(mVideoAdapter.getPositionByKey(mkey));
            if (mute) {
                view.setVisibility(View.INVISIBLE);
            } else {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if (mute) {
//                localrenderview.refresh();
                localrenderview.setVisibility(View.INVISIBLE);
            } else {
                localrenderview.setVisibility(View.VISIBLE);
            }
        }

    }

    private void onMuteMicResult(boolean mute) {
        mMuteMicBool = mute;
        mMuteMic.setImageResource(mute ? R.mipmap.microphone_disable : R.mipmap.microphone);
    }

    boolean mSpeakerOn = true;
    CMCCAudioDevice defaultAudioDevice;

    private void onLoudSpeaker(boolean enable) {
        if (mSpeakerOn) {
            ToastUtils.shortShow(RoomActivity.this, "关闭喇叭");
        } else {
            ToastUtils.shortShow(RoomActivity.this, "打开喇叭");
        }
        mSpeakerOn = !mSpeakerOn;
        sdkEngine.setEnableSpeakerphone(enable);
        mLoudSpkeader.setImageResource(enable ? R.mipmap.loudspeaker : R.mipmap.loudspeaker_disable);
    }

    private void clearGridItem() {
        mVideoAdapter.clearAll();
        mVideoAdapter.notifyDataSetChanged();
    }

    private void startTimeShow() {
        timeshow.setBase(SystemClock.elapsedRealtime());
        timeshow.start();
    }

    private void stopTimeShow() {
        timeshow.stop();
    }

    //初始化视频
    public static void initRecordManager() {
        // 设置拍摄视频缓存路径
//        File dcim = Environment
//                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        URTCRecordManager.init("");
        Log.d(TAG, "initRecordManager: cache path:" + URTCRecordManager.getVideoCachePath());
    }


}

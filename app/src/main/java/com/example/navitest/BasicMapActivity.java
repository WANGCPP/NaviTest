package com.example.navitest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviException;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.autonavi.tbt.TrafficFacilityInfo;


import java.util.ArrayList;
import java.util.List;

public class BasicMapActivity extends Activity implements AMapNaviViewListener, AMapNaviListener {

    protected AMapNaviView mAMapNaviView;
    protected AMapNavi mAMapNavi;
    //终点经纬度设置**********
    protected NaviLatLng mEndLatlng = new NaviLatLng(38.890082, 121.530153);
    //起点经纬度设置**********
    protected NaviLatLng mStartLatlng = new NaviLatLng(38.851089, 121.498100);
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();
    protected List<NaviLatLng> mWayPointList;
    //中间点经纬度设置********
    protected NaviLatLng wayPoint = new NaviLatLng(39.935041, 116.447901);

    private static final String TAG = "AFTtsDemo_Activity";

    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_map);


        intent = getIntent();
        double lati = intent.getDoubleExtra("latitude", 38.890082);
        double longi = intent.getDoubleExtra("longitude", 121.530153);
        mEndLatlng = new NaviLatLng(lati, longi);
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view);
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);


        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNavi.addAMapNaviListener(this);

        //设置模拟车速
        mAMapNavi.setEmulatorNaviSpeed(100);
        //添加起始地点
        sList.add(mStartLatlng);
        //添加终止地点
        eList.add(mEndLatlng);


    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //必须在Activity销毁的同时销毁导航对象
        mAMapNaviView.onDestroy();
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();

        Log.d(TAG, "onDestroy");

    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mAMapNaviView.onPause();
    }


    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mAMapNaviView.onResume();
    }


    @Override
    public void onCalculateRouteSuccess() {            //路径计算成功时回调
        // TODO Auto-generated method stub
//      如果根据获取的导航路线来自定义绘制
        RouteOverLay routeOverlay = new RouteOverLay(mAMapNaviView.getMap(), mAMapNavi.getNaviPath(), this);
        routeOverlay.setTrafficLine(false);
        try {
            routeOverlay.setWidth(30);
        } catch (AMapNaviException e) {
            //宽度须>0
            e.printStackTrace();
        }
        int color[] = new int[10];
        color[0] = Color.BLACK;
        color[1] = Color.RED;
        color[2] = Color.BLUE;
        color[3] = Color.YELLOW;
        color[4] = Color.GRAY;
        routeOverlay.addToMap(color, mAMapNavi.getNaviPath().getWayPointIndex());
//      routeOverlay.addToMap();
        //开始导航
        mAMapNavi.startNavi(AMapNavi.EmulatorNaviMode);
    }


    @Override
    public void onInitNaviSuccess() {                            //导航初始化成功后回调
        // TODO Auto-generated method stub
        /**
         * 方法:
         *   int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute);
         * 参数:
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         * 说明:
         *      以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         * 注意:
         *      不走高速与高速优先不能同时为true
         *      高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //驾车路径计算
        mAMapNavi.calculateDriveRoute(sList, eList, null, strategy);
    }


    @Override
    public void onLocationChange(AMapNaviLocation arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    @Deprecated
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    @Deprecated
    public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void hideCross() {
        // TODO Auto-generated method stub

    }


    @Override
    public void hideLaneInfo() {
        // TODO Auto-generated method stub

    }


    @Override
    public void notifyParallelRoad(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onArriveDestination() {
        // TODO Auto-generated method stub


    }


    @Override
    public void onArrivedWayPoint(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onCalculateMultipleRoutesSuccess(int[] arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onCalculateRouteFailure(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onEndEmulatorNavi() {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), "结束模拟导航", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onGetNavigationText(int arg0, String arg1) {
        // TODO Auto-generated method stub
        Toast.makeText(getApplicationContext(), arg1, Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onGpsOpenStatus(boolean arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onInitNaviFailure() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNaviInfoUpdate(NaviInfo arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    @Deprecated
    public void onNaviInfoUpdated(AMapNaviInfo arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onPlayRing(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onReCalculateRouteForTrafficJam() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onReCalculateRouteForYaw() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onStartNavi(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onTrafficStatusUpdate() {
        // TODO Auto-generated method stub

    }


    @Override
    public void showCross(AMapNaviCross arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
        // TODO Auto-generated method stub

    }


    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void updateAimlessModeStatistics(AimLessModeStat arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onLockMap(boolean arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public boolean onNaviBackClick() {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void onNaviCancel() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNaviMapMode(int arg0) {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNaviSetting() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNaviTurnClick() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNaviViewLoaded() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onNextRoadClick() {
        // TODO Auto-generated method stub

    }


    @Override
    public void onScanViewButtonClick() {
        // TODO Auto-generated method stub

    }




}

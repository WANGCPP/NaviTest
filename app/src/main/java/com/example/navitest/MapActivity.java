package com.example.navitest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.cloud.CloudSearch;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.example.navitest.SearchListFragment.ItemTouchListener;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;





public class MapActivity extends Activity implements LocationSource, AMapLocationListener, View.OnClickListener, OnPoiSearchListener {

    MapView mMapView = null;
    MyLocationStyle myLocationStyle;
    RadioGroup mGPSModeGroup;

    AMap aMap;
    //定位需要的声明
    private AMapLocationClient mLocationClient = null;//定位发起端
    private AMapLocationClientOption mLocationOption = null;//定位参数
    private LocationSource.OnLocationChangedListener mListener = null;//定位监听器
    private UiSettings mUiSettings;
    private LatLng markerLatLng1;//当前位置
    private LatLng markerLatLng2;

    //标识，用于判断是否只显示一次定位信息和用户重新定位
    private boolean isFirstLoc = true;

    /**************************搜索相关定义*******************/
    private CloudSearch mCloudSearch;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private List<PoiItem> poiList = new ArrayList<PoiItem>();

    /********************popupWindow相关定义*****************/

    PopupWindow popupWin;
    Button cancelpopupButton, confirmpopupButton;
    TextView titlepopupTextView, respopupTextView, pricepopupTextView, distancepopupTextView;

    PopupWindow popupSearchWindow = null;
    Button searchButton;
    EditText searchEditText;
    String searchString;

    ImageButton imageButton;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (msg.what == 1) {

                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                SearchListFragment searchFragment = new SearchListFragment();
                searchFragment.setResultList(resultList);
                searchFragment.setOnItemTouchListener(new ItemTouchListener() {

                    @Override
                    public void itemClick(int arg) {
                        // TODO Auto-generated method stub
                        Log.d("MapActivity", "itemClick");
                        LatLng markerLatLng = null;
                        PoiItem p1 = poiList.get(arg);
                        if (p1.getTitle() != null) {
                            markerLatLng = new LatLng(p1.getLatLonPoint().getLatitude(), p1.getLatLonPoint().getLongitude());
                            markPosition(markerLatLng, p1.getTitle(), "目标地点");
                            mLatLng = markerLatLng;
                        } else {
                            Toast.makeText(getApplicationContext(), "数据为空", Toast.LENGTH_SHORT).show();
                        }
                        //设置缩放级别
                        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                        //将地图移到定位点
                        aMap.moveCamera(CameraUpdateFactory.changeLatLng(markerLatLng));
                    }
                });
                transaction.replace(R.id.framelayout_searchlist, searchFragment);
                transaction.commit();
            }

        }

    };


    private double pLongitude;
    private double pLatitude;

    /****************缓冲进度条******************************/
    private ProgressDialog progressDialog;

    /****************geo搜索相关定义****************************/
    private GeocodeSearch geocodeSearch;

    private int searchTypeFlag = 0;//0:搜索指定地点  ；  1：搜索附近停车场
    private LatLng mLatLng = null;

    /*****************搜索ListView相关定义********************/

    ArrayList<ResultItem> resultList = new ArrayList<ResultItem>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ActionBar actionbar = getActionBar();
        if (actionbar != null) {
            actionbar.hide();
        }

        imageButton = (ImageButton) findViewById(R.id.image_btn);
        imageButton.setOnClickListener(this);

        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);
        initLocation();

    }

    private void initLocation() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(this);

        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(10000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        if (null != mLocationClient) {                //销毁定位对象
            mLocationClient.onDestroy();
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
        locationStyleInit();
        uiSettingInit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {        //定位结果回调
        // TODO Auto-generated method stub
        if (amapLocation != null) {//判断是否定位成功，若amapLocation非空，则成功获得定位数据
            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                Toast.makeText(getApplicationContext(), "获取定位信息", Toast.LENGTH_SHORT).show();
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                amapLocation.getLatitude();//获取纬度
                amapLocation.getLongitude();//获取经度
                amapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(amapLocation.getTime());
                df.format(date);//定位时间
                amapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                amapLocation.getCountry();//国家信息
                amapLocation.getProvince();//省信息
                amapLocation.getCity();//城市信息
                amapLocation.getDistrict();//城区信息
                amapLocation.getStreet();//街道信息
                amapLocation.getStreetNum();//街道门牌号信息
                amapLocation.getCityCode();//城市编码
                amapLocation.getAdCode();//地区编码

                if (isFirstLoc == true) {
                    //如果不设置标志位，此时再拖动地图时，它会不断将地图移动到当前的位置
                    isFirstLoc = false;
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(amapLocation.getCountry() + "|" + amapLocation.getProvince() + "|" + amapLocation.getCity() + "|" + amapLocation.getDistrict() + "|" + amapLocation.getStreet());
                    Toast.makeText(getApplicationContext(), buffer.toString() + "|" + isFirstLoc, Toast.LENGTH_SHORT).show();
                    //设置缩放级别
                    aMap.moveCamera(CameraUpdateFactory.zoomTo(12));
                    LatLng latLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                    //将地图移到定位点
                    aMap.moveCamera(CameraUpdateFactory.changeLatLng(latLng));
                    //点击定位按钮，能将地图的中心移动定位点
                    mListener.onLocationChanged(amapLocation);
                    //获取定位信息
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败 " + "-" + amapLocation.getErrorInfo(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        // TODO Auto-generated method stub
        mListener = onLocationChangedListener;

    }

    @Override
    public void deactivate() {
        // TODO Auto-generated method stub
        mListener = null;
    }

    public void locationStyleInit() {
        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(10000);
        myLocationStyle.strokeColor(R.color.darkgreen);
        myLocationStyle.radiusFillColor(R.color.greenyellow);
        myLocationStyle.strokeWidth(40000);
    }

    public void uiSettingInit() {
        mUiSettings = aMap.getUiSettings();                //获取UI设定实例

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.setMyLocationEnabled(true);//设置是否显示定位蓝点，true为显示
        aMap.setOnInfoWindowClickListener(infoWindowClickListener);

        mUiSettings.setMyLocationButtonEnabled(true);
    }

    public void markPosition(LatLng _latLng, String _title, String _snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(_latLng);
        markerOptions.title(_title);
        markerOptions.snippet(_snippet);
        markerOptions.draggable(false);
        final Marker marker = aMap.addMarker(markerOptions);
    }

    AMap.OnInfoWindowClickListener infoWindowClickListener = new AMap.OnInfoWindowClickListener() {

        @Override
        public void onInfoWindowClick(Marker m) {
            // TODO Auto-generated method stub
            if (searchTypeFlag == 0) {
                searchFuntion();
                searchTypeFlag = 1;
            } else if (searchTypeFlag == 1) {
                if (popupWin != null) {
                    popupWin.dismiss();
                    String title = m.getTitle();
                    String remainspace = "***";
                    String price = "***";
                    String distance = "***";
                    double mLongitude = m.getPosition().longitude;
                    double mLatitude = m.getPosition().latitude;

                    createPopupWin(title, remainspace, price, distance, mLongitude, mLatitude);
                } else {
                    String title = m.getTitle();
                    String remainspace = "***";
                    String price = "***";
                    String distance = "***";
                    double mLongitude = m.getPosition().longitude;
                    double mLatitude = m.getPosition().latitude;

                    createPopupWin(title, remainspace, price, distance, mLongitude, mLatitude);
                }

            }
        }
    };

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.top_btn:
                Toast.makeText(getApplicationContext(), "点击搜索", Toast.LENGTH_SHORT).show();
                searchString = searchEditText.getText().toString();
                searchPosition(searchString);
                popupSearchWindow.dismiss();
                break;
            case R.id.cancel_popup_button:
                popupWin.dismiss();
                break;
            case R.id.confirm_popup_button:
                popupWin.dismiss();
                Toast.makeText(getApplicationContext(), "Longitude" + pLongitude, Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Latitude" + pLatitude, Toast.LENGTH_SHORT).show();
                Intent intent1 = new Intent(MapActivity.this, BasicMapActivity.class);
                intent1.putExtra("longitude", pLongitude);
                intent1.putExtra("latitude", pLatitude);
                startActivity(intent1);

                break;
            case R.id.image_btn:
                createSearchPopupWin();
                break;


            default:
                break;
        }
    }

    public void searchFuntion() {
        query = new PoiSearch.Query("停车场", null, "大连");//大连市辖区、公共停车场
        //query = new PoiSearch.Query("", "150900", "210200");//大连市辖区、公共停车场
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);
        query.setCityLimit(true);

        poiSearch = new PoiSearch(getApplicationContext(), query);
        poiSearch.setOnPoiSearchListener(this);


        poiSearch.setBound(new SearchBound(new LatLonPoint(mLatLng.latitude, mLatLng.longitude), 1000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();//发送请求
    }

    /***********************poi接口回调实现******************************************************/
    @Override
    public void onPoiItemSearched(PoiItem arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPoiSearched(PoiResult result, int code) {
        // TODO Auto-generated method stub
        if (code == 1000) {
            //Toast.makeText(getApplicationContext(), "POI接口回调" + "|" + "成功", Toast.LENGTH_SHORT).show();
            poiList = result.getPois();
            //Toast.makeText(getApplicationContext(), "长度为" + poiList.size(), Toast.LENGTH_SHORT).show();
            if (poiList.size() == 0) {
                Toast.makeText(getApplicationContext(), "建议" + result.getSearchSuggestionKeywords(), Toast.LENGTH_SHORT).show();
            }
            if (searchTypeFlag == 0) {//搜索指定地点

                Toast.makeText(getApplicationContext(), "开始为listview赋值", Toast.LENGTH_SHORT).show();
                for (int i = 0; i < poiList.size(); i++) {
                    PoiItem poiItem = poiList.get(i);
                    ResultItem resultItem = new ResultItem(poiItem.getTitle());
                    resultList.add(resultItem);
                }
                mHandler.sendEmptyMessage(1);


            } else if (searchTypeFlag == 1) {
                for (int i = 0; i < poiList.size(); i++) {
                    PoiItem p1 = poiList.get(i);
                    if (p1.getTitle() != null) {
                        LatLng markerLatLng = new LatLng(p1.getLatLonPoint().getLatitude(), p1.getLatLonPoint().getLongitude());
                        markPosition(markerLatLng, p1.getTitle(), "剩余车位");
                    } else {
                        Toast.makeText(getApplicationContext(), "数据为空", Toast.LENGTH_SHORT).show();
                    }
                }
                //设置缩放级别
                aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                //将地图移到定位点
                aMap.moveCamera(CameraUpdateFactory.changeLatLng(mLatLng));
            }
        } else {
            Toast.makeText(getApplicationContext(), "POI接口回调" + "|" + "失败", Toast.LENGTH_SHORT).show();
        }

    }

    public void createPopupWin(String _title, String _remainspace, String _price, String _distance, double _longitude, double _latitude) {
        WindowManager manager = (WindowManager) getSystemService(MainActivity.WINDOW_SERVICE);//获取窗口管理器，系统服务
        int width = manager.getDefaultDisplay().getWidth();
        View popupView = LayoutInflater.from(MapActivity.this).inflate(R.layout.popup_layout, null);

        popupWin = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //popupWin.setOutsideTouchable(true);
        popupWin.setFocusable(true);

        cancelpopupButton = (Button) popupView.findViewById(R.id.cancel_popup_button);//注册popupwindow中的按键监听
        cancelpopupButton.setOnClickListener(this);

        confirmpopupButton = (Button) popupView.findViewById(R.id.confirm_popup_button);//注册popupwindow中的按键监听
        confirmpopupButton.setOnClickListener(this);

        titlepopupTextView = (TextView) popupView.findViewById(R.id.pname_popup_textview);
        titlepopupTextView.setText("停车场名称：" + " " + _title);

        respopupTextView = (TextView) popupView.findViewById(R.id.remainingspace_popup_textview);
        respopupTextView.setText("剩余车位：" + " " + _remainspace);

        pricepopupTextView = (TextView) popupView.findViewById(R.id.price_popup_textview);
        pricepopupTextView.setText("价格：" + " " + _remainspace);

        distancepopupTextView = (TextView) popupView.findViewById(R.id.distance_popup_textview);
        distancepopupTextView.setText("距离：" + " " + _remainspace);


        View parentView = LayoutInflater.from(MapActivity.this).inflate(R.layout.activity_main, null);
        popupWin.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);

        pLatitude = _latitude;
        pLongitude = _longitude;
    }

    public void createSearchPopupWin() {
        WindowManager manager = (WindowManager) getSystemService(MainActivity.WINDOW_SERVICE);//获取窗口管理器，系统服务
        int width = manager.getDefaultDisplay().getWidth();
        View popupView = LayoutInflater.from(MapActivity.this).inflate(R.layout.top_popup_layout, null);

        popupSearchWindow = new PopupWindow(popupView, width, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        //popupWin.setOutsideTouchable(true);
        popupSearchWindow.setFocusable(true);

        searchButton = (Button) popupView.findViewById(R.id.top_btn);
        searchButton.setOnClickListener(this);

        searchEditText = (EditText) popupView.findViewById(R.id.top_edittext);

        View parentView = LayoutInflater.from(MapActivity.this).inflate(R.layout.activity_main, null);
        popupSearchWindow.showAtLocation(parentView, Gravity.TOP, 0, 50);


    }

    public void searchPosition(String _keywords) {
        query = new PoiSearch.Query(_keywords, null, "大连");//大连市辖区、公共停车场
        //query = new PoiSearch.Query("", "150900", "210200");//大连市辖区、公共停车场
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，选用POI搜索类型时建议填写类型代码，码表可以参考下方（而非文字）
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(10);// 设置每页最多返回多少条poiitem
        query.setPageNum(0);
        query.setCityLimit(true);

        poiSearch = new PoiSearch(getApplicationContext(), query);
        poiSearch.setOnPoiSearchListener(this);


        //poiSearch.setBound(new SearchBound(new LatLonPoint(38.890082,121.530153), 1000));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();//发送请求

    }

//	@Override
//	public void itemTouch(int arg) {
//		// TODO Auto-generated method stub
//		Toast.makeText(getApplicationContext(), "itemTouch", Toast.LENGTH_SHORT).show();
//		LatLng markerLatLng = null;
//		PoiItem p1 = poiList.get(arg);
//		if(p1.getTitle()!=null){
//			markerLatLng = new LatLng(p1.getLatLonPoint().getLatitude(), p1.getLatLonPoint().getLongitude());
//			markPosition(markerLatLng, p1.getTitle(), "目标地点");
//			mLatLng = markerLatLng;
//		}else{
//			Toast.makeText(getApplicationContext(), "数据为空", Toast.LENGTH_SHORT).show();
//		}
//        //设置缩放级别
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
//        //将地图移到定位点
//        aMap.moveCamera(CameraUpdateFactory.changeLatLng(markerLatLng));		
//		
//	}
//	

}

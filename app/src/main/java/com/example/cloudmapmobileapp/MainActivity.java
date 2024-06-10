package com.example.BuddyCloudMapApp;
import android.Manifest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.location.PoiRegion;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BM3DModel;
import com.baidu.mapapi.map.BM3DModelOptions;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;

    public LocationClient mLocationClient;
    public String city;
    protected double latitude ;   //纬度信息
    protected double longitude;    //获取经度信息
    protected float radius ;    //获取定位精度，默认值为0.0f

    protected String coorType ; //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

    private PoiSearch mPoiSearch=null;

//    public class MyLocationListener extends BDAbstractLocationListener {
//
//        @Override
//        public void onReceiveLocation(BDLocation location) {
//
//        }
//    }
    private String copyFileFromAssets(String assetDir, String fileName) {
        AssetManager assetManager = getAssets();
        String internalStoragePath = getFilesDir().getAbsolutePath();
        File file = new File(internalStoragePath + File.separator + assetDir);

        if (!file.exists()) {
            file.mkdirs();
        }

        try (InputStream in = assetManager.open(assetDir + "/" + fileName)) {
            File outFile = new File(file, fileName);
            try (OutputStream out = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
            }
            return outFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    private void performSearch() {


        // 获取搜索框中的文本
        EditText searchBox = findViewById(R.id.search_box);
        String query = searchBox.getText().toString();

        // 设置搜索参数
//        PoiCitySearchOption searchOption = new PoiCitySearchOption()
//                .city(city) // TODO: 设置搜索城市
//                .keyword(query) // 设置搜索关键字
//                .pageNum(0); // TODO: 如果需要分页，设置页码
        PoiNearbySearchOption searchOption=new PoiNearbySearchOption()
                .location(new LatLng(latitude,longitude))
                .radius(1000)
                .keyword(query)
                .pageNum(0);
        // 开始搜索
        mPoiSearch.searchNearby(searchOption);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocationClient.setAgreePrivacy(true);
        // 在这里同意隐私政策
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);
        // 初始化SDK
        SDKInitializer.initialize(this);
        // 使用布局文件
        setContentView(R.layout.activity_main);
        // 布局容器的ID
        //获取地图控件引用
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        //设置地图模式为卫星地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        //假人口热力图
//        mBaiduMap.setBaiduHeatMapEnabled(true);
        //地图缩放 比例尺
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mBaiduMap.setMyLocationEnabled(true);

//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//        }

        // 定位初始化
        try {
            mLocationClient = new LocationClient(getApplicationContext());
        } catch (Exception e) {
            System.out.println("出错");
            throw new RuntimeException(e);
        }

        // 通过LocationClientOption设置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开GPS
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000); // 设置发起定位请求的间隔

        ////可选，设置定位模式，默认高精度
        ////LocationMode.Hight_Accuracy：高精度；
        ////LocationMode. Battery_Saving：低功耗；
        ////LocationMode. Device_Sensors：仅使用设备；
        ////LocationMode.Fuzzy_Locating, 模糊定位模式；v9.2.8版本开始支持，可以降低API的调用频率，但同时也会降低定位精度；
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选，首次定位时可以选择定位的返回是准确性优先还是速度优先，默认为速度优先
        //可以搭配setOnceLocation(Boolean isOnceLocation)单次定位接口使用，当设置为单次定位时，setFirstLocType接口中设置的类型即为单次定位使用的类型
        //FirstLocType.SPEED_IN_FIRST_LOC:速度优先，首次定位时会降低定位准确性，提升定位速度；
        //FirstLocType.ACCUARACY_IN_FIRST_LOC:准确性优先，首次定位时会降低速度，提升定位准确性；
        option.setFirstLocType(LocationClientOption.FirstLocType.ACCURACY_IN_FIRST_LOC);

        //可选，设置是否使用卫星定位，默认false
        //使用高精度和仅用设备两种定位模式的，参数必须设置为true
        option.setOpenGnss(true);
        //可选，设置是否当卫星定位有效时按照1S/1次频率输出卫星定位结果，默认false
        option.setLocationNotify(true);
        ////可选，是否需要位置描述信息，默认为不需要，即参数为false
        ////如果开发者需要获得当前点的位置信息，此处必须为true
        option.setIsNeedLocationDescribe(true);

        ////可选，是否需要周边POI信息，默认为不需要，即参数为false
        ////如果开发者需要获得周边POI信息，此处必须为true
        option.setIsNeedLocationPoiList(true);
        option.setIsNeedAddress(true);

        //重点！！！！！！！！！！！！！！！！！！！！
        // 注册定位监听器
        //注册LocationListener监听器
        if(mLocationClient != null) {
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            mLocationClient.setLocOption(option);
            // 注册定位监听器
            mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation location) {
                    //mapView 销毁后不在处理新接收的位置
                    if (location == null || mMapView == null){
                        return;
                    }
                    latitude=location.getLatitude();
                    longitude=location.getLongitude();
                    radius=location.getRadius();
                    coorType=location.getCoorType();
                    city=location.getCity();
                    String locationDescribe=location.getLocationDescribe();//获取位置描述信息

                    TextView locationTextView = findViewById(R.id.location_text_view);
                    String locationTxtView="现在的位置在："+locationDescribe;
                    locationTextView.setText(locationTxtView);

                    int errorCode = location.getLocType();
                    //获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明

                    MyLocationData locData = new MyLocationData.Builder()
                            .accuracy(location.getRadius())
                            // 此处设置开发者获取到的方向信息，顺时针0-360
                            .direction(location.getDirection()).latitude(location.getLatitude())
                            .longitude(location.getLongitude()).build();
                    mBaiduMap.setMyLocationData(locData);
                    LatLng latLng = new LatLng(latitude, longitude);
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);

                    mBaiduMap.animateMapStatus(mapStatusUpdate);
                    Log.i("latitude", String.valueOf(latitude));
                    Log.i("longitude",String.valueOf(longitude));


                }
            });

        // 开始定位 重点！！！！！！！！！！！！
        mLocationClient.start();

        }


        OnGetPoiSearchResultListener poiSearchListener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {

                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    mBaiduMap.clear();

                    //创建PoiOverlay对象
                    PoiOverlay poiOverlay = new PoiOverlay(mBaiduMap);

                    //设置Poi检索数据
                    poiOverlay.setData(poiResult);

                    //将poiOverlay添加至地图并缩放至合适级别
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                }


                //显示搜索结果
                List<PoiInfo> poiList = poiResult.getAllPoi();
                PoiAdapter adapter = new PoiAdapter(MainActivity.this, R.layout.poi_item, poiList);
                ListView listView = findViewById(R.id.searchResult);
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                //当滑动到底部时加载更多搜索结果
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            // 判断是否滚动到底部
                            if (view.getLastVisiblePosition() == view.getCount() - 1) {
                                //加载更多
                                int curPage = poiResult.getCurrentPageNum();
                                int totalPage = poiResult.getTotalPageNum();
                                if (curPage < totalPage) {
                                    poiResult.setCurrentPageNum(curPage + 1);

                                    // 获取搜索框中的文本
                                    EditText searchBox = findViewById(R.id.search_box);
                                    String query = searchBox.getText().toString();
//                                    mPoiSearch.searchInCity(new PoiCitySearchOption()
//                                            .city(city)
//                                            .keyword(query)
//                                            .pageNum(curPage + 1));
                                    PoiNearbySearchOption PagesearchOption=new PoiNearbySearchOption()
                                            .location(new LatLng(latitude,longitude))
                                            .radius(1000)
                                            .keyword(query)
                                            .pageNum(curPage+1);
                                    mPoiSearch.searchNearby(PagesearchOption);



                                }
                            }
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });



            }


            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }


            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
                // 处理室内POI搜索结果
            }
        };
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiSearchListener);

        View searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 执行POI搜索的代码
                performSearch();
            }
        });


//        String modelPath = copyFileFromAssets("/data/IronMan", "IronMan.obj");
//
//        BM3DModelOptions bm3DModelOptions = new BM3DModelOptions();
//// 设置模型文件路径（必填）
////        bm3DModelOptions.setModelPath("../../../data/IronMan/");
//        bm3DModelOptions.setModelPath(modelPath);
//// 设置模型文件名(必填)
//        bm3DModelOptions.setModelName("IronMan");
//        bm3DModelOptions.setScale(50.0f);
//// 设置经纬度（必填）
//        bm3DModelOptions.setPosition(new LatLng(latitude,longitude));
//        BM3DModel mBM3DModel = (BM3DModel) mBaiduMap.addOverlay(bm3DModelOptions);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            ListView listView = findViewById(R.id.searchResult);
            int left = listView.getLeft(), top = listView.getTop(), right = left + listView.getWidth(), bottom = top + listView.getHeight();
            if (ev.getX() < left || ev.getX() > right || ev.getY() < top || ev.getY() > bottom)//点击搜索结果列表之外区域，隐藏搜索结果列表
                listView.setVisibility(View.GONE);
        }
        return super.dispatchTouchEvent(ev);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLocationClient != null) {
            mLocationClient.stop();
        }
        mBaiduMap.setMyLocationEnabled(false);
//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();



    }
}
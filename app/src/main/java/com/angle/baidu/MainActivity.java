package com.angle.baidu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.angle.baidu.databinding.ActivityMainBinding;
import com.angle.baidu.util.PoiOverlay;
import com.angle.baidu.util.WalkingRouteOverlay;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private BaiduMap baiduMap;
    private LocationClient mLocationClient;
    private LatLng latLng;

    private BDLocation mLocation;
    private PoiSearch mPoiSearch;
    private LatLng startPt;
    private LatLng endPt;
    private WalkNaviLaunchParam mParam;

    private RoutePlanSearch mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        baiduMap = binding.mapview.getMap();
        initPermission();
        initLocation();
        initView();

    }

    private void initPermission() {
        String[] pers = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
        ActivityCompat.requestPermissions(this, pers, 100);

       /* if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            String[] mp = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, mp, 123);
        }*/
    }


    private void initView() {
        /*
         *定位
         * 1.调用initLocation（）方法设置初始化定位
         * 2. 创建MyLocationListener 继承 BDAbstractLocationListener 得到mLocation
         * 3.创建locationToMyPosition()方法并且监听*/
        binding.btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationToMyPosition();
            }
        });

        binding.btnMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addMarker();
            }
        });
        binding.btnPio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pio();
            }
        });
//        导航
        binding.bntGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guide();
            }
        });
        binding.bntPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                path();
            }
        });
        binding.bntSerach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serach();
            }
        });
    }

    private void serach() {
        SuggestionSearch mSuggestionSearch = SuggestionSearch.newInstance();
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                //处理sug检索结果
            }
        };

        mSuggestionSearch.setOnGetSuggestionResultListener(listener);

        mSuggestionSearch.requestSuggestion(new SuggestionSearchOption()
                .city("宁夏")
                .keyword("肯"));

        mSuggestionSearch.destroy();
    }

    private void path() {
        mSearch = RoutePlanSearch.newInstance();
        OnGetRoutePlanResultListener listener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
                //创建WalkingRouteOverlay实例
                WalkingRouteOverlay overlay = new WalkingRouteOverlay(baiduMap);
                if (walkingRouteResult.getRouteLines().size() > 0) {
                    //获取路径规划数据,(以返回的第一条数据为例)
                    //为WalkingRouteOverlay实例设置路径数据
                    overlay.setData(walkingRouteResult.getRouteLines().get(0));
                    //在地图上绘制WalkingRouteOverlay
                    overlay.addToMap();
                }
            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        };
        mSearch.setOnGetRoutePlanResultListener(listener);

        PlanNode stNode = PlanNode.withCityNameAndPlaceName("北京", "西二旗地铁站");
        PlanNode enNode = PlanNode.withCityNameAndPlaceName("北京", "百度科技园");

        mSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(stNode)
                .to(enNode));

    }

    private void guide() {
        WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {

            @Override
            public void engineInitSuccess() {
                //引擎初始化成功的回调
                routeWalkPlanWithParam();
            }

            @Override
            public void engineInitFail() {
                Log.e("TAG", "BaiduMapActivity engineInitFail()");
                //引擎初始化失败的回调
            }
        });
    }


    private void routeWalkPlanWithParam() {

        //起终点位置
        startPt = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        endPt = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        Toast.makeText(this, mLocation.getLatitude() + mLocation.getLatitude() + "", Toast.LENGTH_SHORT).show();
        //构造WalkNaviLaunchParam
        mParam = new WalkNaviLaunchParam().stPt(startPt).endPt(endPt);

        //发起算路
        WalkNavigateHelper.getInstance().routePlanWithParams(mParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                //开始算路的回调
            }

            @Override
            public void onRoutePlanSuccess() {
                //算路成功
                //跳转至诱导页面
                Intent intent = new Intent(MainActivity.this, WNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError walkRoutePlanError) {
                //算路失败的回调
            }
        });

    }


    private void initLocation() {
        baiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocationClient = new LocationClient(this);


        //通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);

        //设置locationClientOption
        mLocationClient.setLocOption(option);

        //注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        //开启地图定位图层
        mLocationClient.start();
    }

    private void locationToMyPosition() {
        //如果已经定位了,只需要将地图界面移动到用户所在位置即可
        //改变地图手势的中心点（地图的中心点）
        //mLocation是定位时获取到的用户位置信息对象
        latLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());

        //改变地图手势的中心点
        baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(latLng));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        binding.mapview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        binding.mapview.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        binding.mapview.onDestroy();
    }

    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {

            //mapView 销毁后不在处理新接收的位置
            if (location == null || binding.mapview == null) {
                return;
            }

            mLocation = location;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            baiduMap.setMyLocationData(locData);
        }
    }

    //  标记

    /**
     * 覆盖物
     */
    private void addMarker() {
        //获取当前地图屏幕中心点的坐标
        LatLng target = baiduMap.getMapStatus().target;
        //定义marker覆盖物坐标  经纬度
        LatLng latLng = new LatLng(target.latitude, target.longitude);
        //构建marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        //构建 MarkerOption , 用于在地图上显示 marker
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(bitmap);
        //地图上添加options并显示
        baiduMap.addOverlay(options);
    }

    /*
     * pio检索
     * 1.创建util工具包 创建：PoiOverlay，OverlayManager
     * 2.创建监听pio方法
     * */
    private void pio() {
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener listener = new OnGetPoiSearchResultListener() {
            @Override
            public void onGetPoiResult(PoiResult poiResult) {
                if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    baiduMap.clear();

                    //创建PoiOverlay对象
                    PoiOverlay poiOverlay = new PoiOverlay(baiduMap);

                    //设置Poi检索数据
                    poiOverlay.setData(poiResult);

                    //将poiOverlay添加至地图并缩放至合适级别
                    poiOverlay.addToMap();
                    poiOverlay.zoomToSpan();
                }
            }

            @Override
            public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

            }

            @Override
            public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

            }

            //废弃
            @Override
            public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

            }
        };

        mPoiSearch.setOnGetPoiSearchResultListener(listener);

        //城市内检索
//        mPoiSearch.searchInCity(new PoiCitySearchOption()
//                .city("北京") //必填
//                .keyword("美食") //必填
//                .pageNum(10));

        //城市周边检索
//        mPoiSearch.searchNearby(new PoiNearbySearchOption()
//                .location(new LatLng(39.915446, 116.403869))
//                .radius(10000)
//                .keyword("餐厅")
//                .pageNum(10));

        /**
         * 设置矩形检索区域
         */
        LatLngBounds searchBounds = new LatLngBounds.Builder()
                .include(new LatLng(39.92235, 116.380338))
                .include(new LatLng(39.947246, 116.414977))
                .build();
        //在searchBounds区域内检索餐厅
        mPoiSearch.searchInBound(new PoiBoundSearchOption()
                .bound(searchBounds)
                .keyword("餐厅"));
    }
}

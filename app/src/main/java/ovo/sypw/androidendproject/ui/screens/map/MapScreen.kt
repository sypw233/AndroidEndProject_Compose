package ovo.sypw.androidendproject.ui.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener
import com.baidu.mapapi.search.poi.PoiDetailResult
import com.baidu.mapapi.search.poi.PoiDetailSearchResult
import com.baidu.mapapi.search.poi.PoiIndoorResult
import com.baidu.mapapi.search.poi.PoiNearbySearchOption
import com.baidu.mapapi.search.poi.PoiResult
import com.baidu.mapapi.search.poi.PoiSearch

/**
 * 地图页面
 * 功能：POI 搜索、标记点展示、定位
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // 搜索关键词
    var searchKeyword by remember { mutableStateOf("") }

    // POI 搜索结果
    var poiList by remember { mutableStateOf<List<PoiInfo>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 选中的 POI
    var selectedPoi by remember { mutableStateOf<PoiInfo?>(null) }

    // 当前位置 (默认北京)
    var currentLocation by remember { mutableStateOf(LatLng(39.915, 116.404)) }
    var isLocationReady by remember { mutableStateOf(false) }

    // 权限状态
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasLocationPermission = granted
        if (granted) {
            Toast.makeText(context, "定位权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "定位权限被拒绝，将使用默认位置", Toast.LENGTH_SHORT).show()
        }
    }

    // 地图视图
    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            onResume()
        }
    }

    val baiduMap = remember { mapView.map }

    // 定位客户端
    val locationClient = remember {
        try {
            LocationClient(context.applicationContext).apply {
                val option = LocationClientOption().apply {
                    setIsNeedAddress(true)
                    setOpenGps(true)
                    setCoorType("bd09ll")
                    setScanSpan(0) // 单次定位
                }
                locOption = option
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "创建定位客户端失败", e)
            null
        }
    }

    // POI 搜索
    val poiSearch = remember { PoiSearch.newInstance() }

    // 定位监听器
    val locationListener = remember {
        object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation?) {
                if (location == null) {
                    Log.w("MapScreen", "定位失败：location is null")
                    return
                }
                
                if (location.locType == BDLocation.TypeGpsLocation || 
                    location.locType == BDLocation.TypeNetWorkLocation ||
                    location.locType == BDLocation.TypeOffLineLocation) {
                    
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("MapScreen", "定位成功: lat=$lat, lng=$lng, addr=${location.addrStr}")
                    
                    currentLocation = LatLng(lat, lng)
                    isLocationReady = true
                    
                    // 更新地图上的位置标记
                    val locData = MyLocationData.Builder()
                        .accuracy(location.radius)
                        .direction(location.direction)
                        .latitude(lat)
                        .longitude(lng)
                        .build()
                    baiduMap.setMyLocationData(locData)
                    
                    // 移动到当前位置
                    baiduMap.animateMapStatus(
                        MapStatusUpdateFactory.newLatLngZoom(currentLocation, 15f)
                    )
                } else {
                    Log.w("MapScreen", "定位失败: locType=${location.locType}")
                    Toast.makeText(context, "定位失败，使用默认位置", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 设置 POI 搜索监听器
    LaunchedEffect(poiSearch) {
        poiSearch.setOnGetPoiSearchResultListener(object : OnGetPoiSearchResultListener {
            override fun onGetPoiResult(result: PoiResult?) {
                isSearching = false
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(context, "未找到相关地点", Toast.LENGTH_SHORT).show()
                    poiList = emptyList()
                    return
                }

                val pois = result.allPoi ?: emptyList()
                poiList = pois.filter { it.location != null }

                // 清除旧标记并添加新标记
                baiduMap.clear()
                poiList.forEachIndexed { index, poi ->
                    poi.location?.let { location ->
                        val marker = MarkerOptions()
                            .position(location)
                            .title(poi.name)
                            .icon(
                                BitmapDescriptorFactory.fromResource(
                                    android.R.drawable.ic_menu_mylocation
                                )
                            )
                        val bundle = Bundle().apply { putInt("index", index) }
                        marker.extraInfo(bundle)
                        baiduMap.addOverlay(marker)
                    }
                }

                // 缩放到搜索结果
                if (poiList.isNotEmpty()) {
                    poiList.firstOrNull()?.location?.let { firstLocation ->
                        baiduMap.animateMapStatus(
                            MapStatusUpdateFactory.newLatLngZoom(firstLocation, 15f)
                        )
                    }
                }
            }

            override fun onGetPoiDetailResult(result: PoiDetailResult?) {}
            override fun onGetPoiDetailResult(result: PoiDetailSearchResult?) {}
            override fun onGetPoiIndoorResult(result: PoiIndoorResult?) {}
        })
    }

    // 标记点点击监听
    LaunchedEffect(baiduMap) {
        baiduMap.setOnMarkerClickListener { marker ->
            val index = marker.extraInfo?.getInt("index") ?: return@setOnMarkerClickListener false
            if (index in poiList.indices) {
                selectedPoi = poiList[index]
            }
            true
        }
    }

    // 初始化地图和定位
    LaunchedEffect(Unit) {
        baiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
        baiduMap.isMyLocationEnabled = true
        baiduMap.animateMapStatus(
            MapStatusUpdateFactory.newLatLngZoom(currentLocation, 15f)
        )

        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 权限获取后启动定位
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && locationClient != null) {
            try {
                locationClient.registerLocationListener(locationListener)
                locationClient.start()
                Log.d("MapScreen", "定位服务已启动")
            } catch (e: Exception) {
                Log.e("MapScreen", "启动定位服务失败", e)
            }
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            try {
                locationClient?.let {
                    it.unRegisterLocationListener(locationListener)
                    it.stop()
                }
                baiduMap.isMyLocationEnabled = false
                baiduMap.clear()
                poiSearch.destroy()
                mapView.onPause()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 执行 POI 搜索
    fun searchPoi(keyword: String) {
        if (keyword.isBlank()) {
            Toast.makeText(context, "请输入搜索关键词", Toast.LENGTH_SHORT).show()
            return
        }
        // 清除焦点
        focusManager.clearFocus()
        
        isSearching = true
        selectedPoi = null
        poiSearch.searchNearby(
            PoiNearbySearchOption()
                .location(currentLocation)
                .radius(10000)
                .keyword(keyword)
        )
    }

    // 快捷搜索分类
    val quickSearchCategories = listOf("餐厅", "酒店", "银行", "医院", "超市", "加油站", "停车场")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("附近地图") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission && locationClient != null) {
                        locationClient.start()
                        Toast.makeText(context, "正在获取位置...", Toast.LENGTH_SHORT).show()
                    } else {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "定位到当前位置")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 地图
            AndroidView(
                factory = { mapView },
                modifier = Modifier.fillMaxSize()
            )

            // 搜索栏和结果区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // 搜索输入框
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchKeyword,
                        onValueChange = { searchKeyword = it },
                        placeholder = { Text("搜索附近地点...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else if (searchKeyword.isNotBlank()) {
                                IconButton(onClick = { searchPoi(searchKeyword) }) {
                                    Icon(Icons.Default.Search, contentDescription = "搜索")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(24.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 快捷搜索分类
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickSearchCategories) { category ->
                        QuickSearchChip(
                            text = category,
                            onClick = {
                                searchKeyword = category
                                searchPoi(category)
                            }
                        )
                    }
                }

                // 搜索结果列表（显示在搜索框下方）
                if (poiList.isNotEmpty() && selectedPoi == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 250.dp)
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(poiList.take(10)) { poi ->
                                PoiSearchResultItem(
                                    poi = poi,
                                    onClick = {
                                        selectedPoi = poi
                                        poi.location?.let {
                                            baiduMap.animateMapStatus(
                                                MapStatusUpdateFactory.newLatLngZoom(it, 17f)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // POI 详情卡片
            selectedPoi?.let { poi ->
                PoiDetailCard(
                    poi = poi,
                    onDismiss = { selectedPoi = null },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 72.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickSearchChip(
    text: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun PoiSearchResultItem(
    poi: PoiInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = poi.name ?: "未知地点",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!poi.address.isNullOrBlank()) {
                Text(
                    text = poi.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PoiDetailCard(
    poi: PoiInfo,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDismiss() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = poi.name ?: "未知地点",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 地址
            if (!poi.address.isNullOrBlank()) {
                Text(
                    text = "📍 ${poi.address}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 电话
            if (!poi.phoneNum.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📞 ${poi.phoneNum}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "点击关闭",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

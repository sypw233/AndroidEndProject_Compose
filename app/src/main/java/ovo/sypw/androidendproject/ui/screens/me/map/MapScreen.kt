package ovo.sypw.androidendproject.ui.screens.me.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Close
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
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.services.core.LatLonPoint
import com.amap.api.services.core.PoiItemV2
import com.amap.api.services.poisearch.PoiResultV2
import com.amap.api.services.poisearch.PoiSearchV2

/**
 * 地图页面 - 使用高德地图
 * 功能：POI 搜索、标记点展示、定位
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // 搜索面板显示状态
    var showSearchPanel by remember { mutableStateOf(false) }

    // 搜索关键词
    var searchKeyword by remember { mutableStateOf("") }

    // POI 搜索结果
    var poiList by remember { mutableStateOf<List<PoiItemV2>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // 选中的 POI
    var selectedPoi by remember { mutableStateOf<PoiItemV2?>(null) }

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
            onCreate(Bundle())
        }
    }
    val aMap = remember { mapView.map }

    // 是否是首次定位
    var isFirstLocate by remember { mutableStateOf(true) }

    // 定位客户端
    val locationClient = remember {
        try {
            AMapLocationClient.updatePrivacyShow(context, true, true)
            AMapLocationClient.updatePrivacyAgree(context, true)
            AMapLocationClient(context.applicationContext).apply {
                val option = AMapLocationClientOption().apply {
                    locationPurpose = AMapLocationClientOption.AMapLocationPurpose.SignIn
                    locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    // 修改为连续定位
                    isOnceLocation = false
                    interval = 2000 // 2秒定位一次
                    isNeedAddress = true
                }
                setLocationOption(option)
            }
        } catch (e: Exception) {
            Log.e("MapScreen", "创建定位客户端失败", e)
            null
        }
    }

    // 定位数据源 (LocationSource)
    val locationSource = remember {
        object : com.amap.api.maps.LocationSource {
            var listener: com.amap.api.maps.LocationSource.OnLocationChangedListener? = null
            override fun activate(l: com.amap.api.maps.LocationSource.OnLocationChangedListener?) {
                listener = l
            }

            override fun deactivate() {
                listener = null
            }
        }
    }

    // 定位监听器
    val locationListener = remember {
        AMapLocationListener { location ->
            if (location != null) {
                if (location.errorCode == 0) {
                    // 1. 将定位数据传递给地图 SDK
                    locationSource.listener?.onLocationChanged(location)

                    // 2. 更新应用内状态
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("MapScreen", "定位成功: lat=$lat, lng=$lng, addr=${location.address}")

                    currentLocation = LatLng(lat, lng)
                    isLocationReady = true

                    // 3. 仅首次定位时自动移动视角
                    if (isFirstLocate) {
                        aMap.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(currentLocation, 15f)
                        )
                        isFirstLocate = false
                    }
                } else {
                    Log.w(
                        "MapScreen",
                        "定位失败: errorCode=${location.errorCode}, errorInfo=${location.errorInfo}"
                    )
                    if (isFirstLocate) {
                        Toast.makeText(context, "定位失败：${location.errorInfo}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    // 设置地图定位样式
    LaunchedEffect(aMap) {
        aMap.setLocationSource(locationSource) // 设置定位源
        aMap.isMyLocationEnabled = true
        // LOCATION_TYPE_SHOW: 只定位，不移动地图。视角移动完全由代码 (moveCamera/animateCamera) 控制
        aMap.myLocationStyle = MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW)
            interval(2000)
        }
        aMap.uiSettings.isMyLocationButtonEnabled = false
        // 初始可不移动，等待 callback 处理 isFirstLocate
    }

    // 权限获取后启动定位
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && locationClient != null) {
            try {
                locationClient.setLocationListener(locationListener)
                locationClient.startLocation()
                Log.d("MapScreen", "定位服务已启动")
            } catch (e: Exception) {
                Log.e("MapScreen", "启动定位服务失败", e)
            }
        }
    }

    // 请求权限
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 打开搜索面板时自动聚焦
    LaunchedEffect(showSearchPanel) {
        if (showSearchPanel) {
            try {
                focusRequester.requestFocus()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            try {
                locationClient?.stopLocation()
                locationClient?.onDestroy()
                aMap.isMyLocationEnabled = false
                aMap.clear()
                mapView.onDestroy()
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
        focusManager.clearFocus()
        isSearching = true
        selectedPoi = null

        try {
            val query = PoiSearchV2.Query(keyword, "", "")
            query.pageSize = 20
            query.pageNum = 0

            val poiSearch = PoiSearchV2(context, query)
            poiSearch.bound = PoiSearchV2.SearchBound(
                LatLonPoint(
                    currentLocation.latitude,
                    currentLocation.longitude
                ),
                10000
            )
            poiSearch.setOnPoiSearchListener(object : PoiSearchV2.OnPoiSearchListener {
                override fun onPoiSearched(result: PoiResultV2?, rCode: Int) {
                    isSearching = false
                    if (rCode == 1000 && result != null) {
                        val pois = result.pois ?: emptyList()
                        poiList = pois.filter { it.latLonPoint != null }

                        aMap.clear()
                        poiList.forEachIndexed { index, poi ->
                            poi.latLonPoint?.let { point ->
                                val marker = MarkerOptions()
                                    .position(LatLng(point.latitude, point.longitude))
                                    .title(poi.title)
                                    .snippet(poi.snippet)
                                val addedMarker = aMap.addMarker(marker)
                                addedMarker.`object` = index
                            }
                        }

                        if (poiList.isNotEmpty()) {
                            poiList.firstOrNull()?.latLonPoint?.let { firstPoint ->
                                aMap.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(
                                        LatLng(firstPoint.latitude, firstPoint.longitude),
                                        15f
                                    )
                                )
                            }
                        }
                    } else {
                        Toast.makeText(context, "未找到相关地点", Toast.LENGTH_SHORT).show()
                        poiList = emptyList()
                    }
                }

                override fun onPoiItemSearched(poiItem: PoiItemV2?, rCode: Int) {}
            })
            poiSearch.searchPOIAsyn()
        } catch (e: Exception) {
            isSearching = false
            Log.e("MapScreen", "POI搜索失败", e)
            Toast.makeText(context, "搜索失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 标记点击监听
    LaunchedEffect(aMap) {
        aMap.setOnMarkerClickListener { marker ->
            val index = marker.`object` as? Int ?: return@setOnMarkerClickListener false
            if (index in poiList.indices) {
                selectedPoi = poiList[index]
                showSearchPanel = false
            }
            true
        }
    }

    // 快捷搜索分类
    val quickSearchCategories =
        listOf("餐厅", "厕所", "酒店", "银行", "医院", "超市", "加油站", "停车场")

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
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.onResume()
                }
            )

            // FAB 按钮组
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 搜索按钮
                FloatingActionButton(
                    onClick = { showSearchPanel = !showSearchPanel },
                    containerColor = if (showSearchPanel)
                        MaterialTheme.colorScheme.secondaryContainer
                    else
                        MaterialTheme.colorScheme.secondary
                ) {
                    Icon(
                        if (showSearchPanel) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = if (showSearchPanel) "关闭搜索" else "搜索"
                    )
                }

                // 定位按钮
                FloatingActionButton(
                    onClick = {
                        if (hasLocationPermission && locationClient != null) {
                            if (!locationClient.isStarted) {
                                locationClient.startLocation()
                            }
                            // 手动点击定位，强制移动视角
                            if (isLocationReady) {
                                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                                Toast.makeText(context, "已回到当前位置", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "正在获取位置...", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "定位到当前位置")
                }
            }

            // 搜索面板（从顶部滑入）
            AnimatedVisibility(
                visible = showSearchPanel,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // 搜索输入框
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
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

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

                        // 搜索结果列表
                        if (poiList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "搜索结果 (${poiList.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(poiList.take(15)) { poi ->
                                    PoiSearchResultItem(
                                        poi = poi,
                                        onClick = {
                                            selectedPoi = poi
                                            showSearchPanel = false
                                            poi.latLonPoint?.let { point ->
                                                aMap.animateCamera(
                                                    CameraUpdateFactory.newLatLngZoom(
                                                        LatLng(point.latitude, point.longitude),
                                                        17f
                                                    )
                                                )
                                            }
                                        }
                                    )
                                }
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
                        .padding(bottom = 80.dp)
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
    poi: PoiItemV2,
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
                text = poi.title ?: "未知地点",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!poi.snippet.isNullOrBlank()) {
                Text(
                    text = poi.snippet,
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
    poi: PoiItemV2,
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
                    text = poi.title ?: "未知地点",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 地址
            if (!poi.snippet.isNullOrBlank()) {
                Text(
                    text = "${poi.snippet}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

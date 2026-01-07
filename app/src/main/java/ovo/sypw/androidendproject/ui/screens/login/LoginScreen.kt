package ovo.sypw.androidendproject.ui.screens.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ovo.sypw.androidendproject.ui.activity.FirebaseUILoginActivity

/**
 * 登录页面
 * 使用 FirebaseUI 进行登录，启动 FirebaseUILoginActivity
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    
    // 创建 Activity 结果启动器
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            onLoginSuccess()
        } else {
            onBack()
        }
    }
    
    // 自动启动 FirebaseUI 登录 Activity
    LaunchedEffect(Unit) {
        launcher.launch(FirebaseUILoginActivity.createIntent(context))
    }
    
    // 显示加载指示器
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

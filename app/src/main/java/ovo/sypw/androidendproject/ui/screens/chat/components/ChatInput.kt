package ovo.sypw.androidendproject.ui.screens.chat.components

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

/**
 * 聊天输入组件
 */
@Composable
fun ChatInput(
    onSendMessage: (String) -> Unit,
    onImageSelected: (Uri?) -> Unit,
    onThinkingToggle: () -> Unit,
    isLoading: Boolean,
    thinkingEnabled: Boolean,
    pendingImageBase64: String?,
    onClearImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // 图片选择按钮
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = "添加图片",
                    tint = if (pendingImageBase64 != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 输入区域
            Box(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("发送消息...") },
                    maxLines = 4,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() || pendingImageBase64 != null) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    leadingIcon = if (pendingImageBase64 != null) {
                        {
                            // 预览待发送的图片
                            Box(modifier = Modifier.padding(start = 8.dp)) {
                                val bitmap = remember(pendingImageBase64) {
                                    try {
                                        val bytes =
                                            Base64.decode(pendingImageBase64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        null
                                    }
                                }
                                bitmap?.let {
                                    Box {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "待发送图片",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        // 删除按钮
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "移除图片",
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(16.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.error,
                                                    CircleShape
                                                )
                                                .clickable { onClearImage() }
                                                .padding(2.dp),
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    } else null
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 思考模式切换
            FilterChip(
                selected = thinkingEnabled,
                onClick = onThinkingToggle,
                label = {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "思考模式",
                        modifier = Modifier.size(20.dp)
                    )
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(4.dp))

            // 发送按钮
            IconButton(
                onClick = {
                    if (inputText.isNotBlank() || pendingImageBase64 != null) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                },
                enabled = !isLoading && (inputText.isNotBlank() || pendingImageBase64 != null)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (!isLoading && (inputText.isNotBlank() || pendingImageBase64 != null))
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

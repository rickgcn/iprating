package com.rickg.iprating

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager

@Composable
fun MainUI(MainActivity: ComponentActivity) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("IP检测") },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(MainActivity, SettingsActivity::class.java)
                            MainActivity.startActivity(intent)
                        }
                    ) {
                        Icon(Icons.Filled.Settings, "Settings", tint = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            MainUIView(innerPadding)
        }
    )
}

@Composable
fun MainUIView(innerPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LocalContext.current)
        // Add input field
        var inputText by remember { mutableStateOf("") }
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(text = "输入IP...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        // Add button
        var isLoading by remember { mutableStateOf(false) }
        var buttonText by remember { mutableStateOf("检测IP !") }
        var resultText by remember { mutableStateOf("") }
        var resultInfo by remember { mutableStateOf(IpInfoIpInfo()) }

        Button(
            onClick = {
                isLoading = true
                buttonText = ""
                IpInfoGetIpInfo(inputText, sharedPreferences.getString("ipinfotoken", "")) { result ->
                    result.fold(
                        { ipError ->
                            print(ipError.message)
                            resultText = "错误: ${ipError.message}"
                            isLoading = false
                            buttonText = "检测IP !"
                        },
                        { ipInfoIpInfo ->
                            resultInfo = ipInfoIpInfo
//                            resultText = "IP: ${ipInfoIpInfo.ip}\n" +
//                                    "主机名: ${ipInfoIpInfo.hostname}\n" +
//                                    "泛播地址: ${if (ipInfoIpInfo.anycast) "是" else "否"}\n" +
//                                    "IP所在城市: ${ipInfoIpInfo.city}\n" +
//                                    "地区: ${ipInfoIpInfo.region}\n" +
//                                    "国家: ${ipInfoIpInfo.country}\n" +
//                                    "经纬度: ${ipInfoIpInfo.loc}\n" +
//                                    "网络服务提供商 (ASN): ${ipInfoIpInfo.org}\n" +
//                                    "邮编: ${ipInfoIpInfo.postal}\n" +
//                                    "时区: ${ipInfoIpInfo.timezone}\n" +
//                                    "Bogon地址: ${if (ipInfoIpInfo.bogon) "是" else "否"}\n"
                            isLoading = false
                            buttonText = "检测IP !"
                        }
                    )
                }
            },
            modifier = Modifier
                .size(130.dp, 70.dp)
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(25.dp),
                    color = Color.White
                )
            } else {
                Text(buttonText)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item { 
                IpInfoCard(title = "IP", subTitle = resultInfo.ip)
            }
            item {
                IpInfoCard(title = "主机名", subTitle = resultInfo.hostname)
            }
            item {
                IpInfoCard(title = "泛播地址", subTitle = if (resultInfo.anycast) "是" else "否")
            }
            item {
                IpInfoCard(title = "城市", subTitle = resultInfo.city)
            }
            item {
                IpInfoCard(title = "地区", subTitle = resultInfo.region)
            }
            item {
                IpInfoCard(title = "国家", subTitle = resultInfo.country)
            }
            item {
                IpInfoCard(title = "经纬度", subTitle = resultInfo.loc)
            }
            item {
                IpInfoCard(title = "网络服务提供商 (ASN)", subTitle = resultInfo.org)
            }
            item {
                IpInfoCard(title = "邮编", subTitle = resultInfo.postal)
            }
            item {
                IpInfoCard(title = "时区", subTitle = resultInfo.timezone)
            }
            item {
                IpInfoCard(title = "Bogon地址", subTitle = if (resultInfo.bogon) "是" else "否")
            }
        }
    }
}

@Composable
fun IpInfoCard(title: String, subTitle: String?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(vertical = 8.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClick = {}
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(title, fontFamily = FontFamily.Monospace, fontSize = 20.sp, modifier = Modifier.padding(16.dp))
                Spacer(Modifier.weight(1f))
                Text(subTitle.toString(), fontFamily = FontFamily.Monospace, fontSize = 15.sp, modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .wrapContentWidth(Alignment.End)
                )
            }
        }
    }
}
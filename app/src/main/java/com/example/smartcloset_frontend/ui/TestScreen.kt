package com.example.smartcloset_frontend.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import com.example.smartcloset_frontend.data.GenerateOutfitData
import com.example.smartcloset_frontend.data.GenerateOutfitWithWeather
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.data.LocationData
import com.example.smartcloset_frontend.ui.dialogs.*
import com.example.smartcloset_frontend.viewmodel.ClothesDetailViewModel
import com.example.smartcloset_frontend.viewmodel.GenerateOutfitViewModel
import com.example.smartcloset_frontend.viewmodel.GenerateOutfitWithWeatherViewModel
import com.example.smartcloset_frontend.viewmodel.GetWeatherViewModel
import com.example.smartcloset_frontend.viewmodel.ItemViewModel
import com.example.smartcloset_frontend.viewmodel.ProfileEditViewModel
import com.example.smartcloset_frontend.viewmodel.SearchViewModel
import java.io.File

// 保存されている画像枚数を数えるユーティリティ関数
fun countItemImages(context: Context): Int {
    val dir = File(context.filesDir, "ItemImgs")
    if (!dir.exists()) return 0

    // jpg / png などの画像だけを数える
    val files = dir.listFiles { file ->
        file.extension.lowercase() in listOf("jpg", "jpeg", "png", "webp")
    }

    return files?.size ?: 0
}
// 保存されている画像をすべて削除するユーティリティ関数
fun deleteAllItemImages(context: Context): Int {
    val dir = File(context.filesDir, "ItemImgs")
    if (!dir.exists()) return 0

    val files = dir.listFiles() ?: return 0

    var deletedCount = 0
    for (file in files) {
        if (file.isFile && file.delete()) {
            deletedCount++
        }
    }
    return deletedCount
}


@Composable
fun TestScreen(navController: NavHostController) {
    val getWeatherViewModel: GetWeatherViewModel = viewModel()
    val weather by getWeatherViewModel.weatherData.collectAsState()
    val ItemViewModel: ItemViewModel = viewModel()
    val closeDetailViewModel: ClothesDetailViewModel = viewModel()
    val generateOutfitViewModel: GenerateOutfitViewModel = viewModel()
    val generateOutfitWithWeatherViewModel: GenerateOutfitWithWeatherViewModel = viewModel()
    val profileEditViewModel: ProfileEditViewModel = viewModel()
    val searchViewModel : SearchViewModel = viewModel()


    val context = LocalContext.current

    // ダイアログ表示用フラグ
    var showSuccess by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TestSection(title = "リクエストテスト用") {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                onClick = {
                    val data = JudgeRequestData(
                        planItemId = 1,
                        vote = "good"
                    )

                    ItemViewModel.sendJudge(data)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("good bad判定/judgement　planItemId = 1,vote = good")
            }
                Button(
                        onClick = {
                            val data = JudgeRequestData(
                                planItemId = 2,
                                vote = "bad"
                            )

                            ItemViewModel.sendJudge(data)
                        },
                modifier = Modifier.fillMaxWidth()
                ) {
                Text("good bad判定/judgement　planItemId = 2,vote = bad")
            }
                Button(
                onClick = {
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("お気に入り登録リクエスト送信/favorite_item　まだできてない")
            }
                Button(
                onClick = {
                    searchViewModel.sendSearchData(
                        query = "夏服",
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("検索 query = \"夏服\" /search")
            }
                Button(
                    onClick = {
                        profileEditViewModel.updateProfile(
                            name="テストユーザー",
                            gender="女性",
                            height="165",
                            weight="45",
                            personalColor = "サマー",
                            skeleton = "小柄"
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("プロフ更新/update_profile")
                }
                Button(
                    onClick = {
                        generateOutfitWithWeatherViewModel.generateOutfitWithWeather(
                            GenerateOutfitWithWeather(
                                userId = 1,
                                plan = "友達とカフェでおしゃべり",
                                weather = "晴れ"
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("コーデ作成/generate_outfit_with_weather ")
                }
                Button(
                    onClick = {
                        generateOutfitViewModel.generateOutfit(
                            GenerateOutfitData(
                                userId = 1,
                                selfieId=1,
                                topsId=1,
                                bottomsId=1,
                                othersId=null,
                                others2Id=null

                        ))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("！！！！課金注意！！！！コーデ作成（人込み）/generate_outfit")
                }
                Button(
                onClick = {
                    ItemViewModel.loadItems(
                        userId = 1
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("アイテム一覧取得リクエスト送信/get_item")
            }
                Button(
                        onClick = {
                            closeDetailViewModel.loadDetail(
                                itemId = 1
                            )
                        },
                modifier = Modifier.fillMaxWidth()
                ) {
                Text("アイテム詳細取得リクエスト送信(id=1)/get_item_detail")
            }
                Button(
                    onClick = {
                        val location = LocationData(lon = 139.767125,
                            lat = 35.681236,

                        )
                        getWeatherViewModel.fetchWeather(
                            location
                        )
                        Toast.makeText(
                            context,
                            "天気情報取得リクエストを送信しました(東京駅)",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("天気情報取得リクエスト送信 (東京駅)/get_weather")
                }
                weather?.let { data ->
                    Text(text = "取得した天気情報: ${data}, 気温: ${data.tempC}°C")
            }
            }
        }
        // ナビゲーションセクション
        TestSection(
            title = "ナビゲーション",
            content = {
                Button(
                    onClick = {
                        navController.navigate("clothes_detail")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("服詳細画面へ移動")
                }
            }
        )

        // ダイアログテストセクション
        TestSection(
            title = "ダイアログテスト",
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showSuccess = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Success Dialog")
                    }
                    Button(
                        onClick = { showError = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Error Dialog")
                    }
                    Button(
                        onClick = { showDelete = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show Delete Dialog")
                    }
                }
            }
        )
    }

    // 成功ダイアログ
    if (showSuccess) {
        ProfileUpdateSuccessDialog(onDismiss = { showSuccess = false })
    }

    // 失敗ダイアログ
    if (showError) {
        ProfileUpdateErrorDialog(onDismiss = { showError = false })
    }

    // 削除確認ダイアログ
    if (showDelete) {
        ProfileDeleteConfirmDialog(
            onConfirm = {
                // TODO: 削除処理をここに書く
                showDelete = false
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
fun TestSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}


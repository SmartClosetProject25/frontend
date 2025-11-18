package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.smartcloset_frontend.data.repository.PasswordResetRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordRequestScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var sending by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val repository = remember { PasswordResetRepository() }

    fun validate(): Boolean {
        if (!email.contains("@")) { error = "正しいメールアドレスを入力してください。"; return false }
        error = null; return true
    }

    fun onSend() {
        if (!validate()) return
        scope.launch {
            sending = true
            error = null
            try {
                val response = repository.requestPasswordReset(email)
                if (response.isSuccessful) {
                    navController.navigate("forgot_email_sent") {
                        popUpTo("forgot") { inclusive = false }
                        launchSingleTop = true
                    }
                } else {
                    error = "メール送信に失敗しました。再度お試しください。"
                }
            } catch (e: Exception) {
                error = "ネットワークエラー: ${e.message}"
            } finally {
                sending = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            Text(
                text = "パスワード再設定",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(60.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                placeholder = { Text("メールアドレス", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onSend() },
                enabled = !sending,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                if (sending) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("メール送信", color = Color.White, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Text(text = "ログインに戻る ", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(text = "ログイン", color = Color.Black, fontSize = 14.sp)
                }
            }
        }
    }
}




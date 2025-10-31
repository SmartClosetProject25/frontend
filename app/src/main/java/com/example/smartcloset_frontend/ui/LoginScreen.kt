package com.example.smartcloset_frontend.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

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
            
            // メインタイトル
            Text(
                text = "ログイン",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // メールアドレス入力フィールド
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    showError = false
                },
                placeholder = {
                    Text(
                        text = "メールアドレス",
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "メールアイコン",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // パスワード入力フィールド
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    showError = false
                },
                placeholder = {
                    Text(
                        text = "パスワード",
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "パスワードアイコン",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    cursorColor = Color.Black
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            // エラーメッセージ
            if (showError) {
                Text(
                    text = "認証情報が異なっています。",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // ログインボタン
            Button(
                onClick = {
                    if (email.isNotEmpty() && password.isNotEmpty()) {
                        // サンプル認証: sampleuser@mail.com / password のみ通す
                        // ↓
                        // 空白のみ通す
                        if (email == "" && password == "") {
                            onLoginClick(email, password)
                        } else {
                            showError = true
                        }
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "ログイン",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 新規登録リンク
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "初めてご利用の方 ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onRegisterClick
                ) {
                Text(
                    text = "新規登録",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                }
            }
            
            // パスワード再設定リンク
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "パスワードをお忘れの方 ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onForgotPasswordClick
                ) {
                Text(
                    text = "再設定",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                }
            }
        }
    }
}




httpリクエスト処理順
/network/ApiService.kt
 >>送信リクエストを定義(/update_prof等)
/data/repository/exRepository.kt
 >>http通信を行う
/viewmodel/exViewModel.kt
 >>UIに関係するデータと機能を管理（ここでexRepositoryを呼び出して使用）
　　通信エラーもここでハンドリング

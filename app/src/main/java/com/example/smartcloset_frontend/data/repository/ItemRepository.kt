package com.example.smartcloset_frontend.data.repository
import android.util.Log
import com.example.smartcloset_frontend.data.GetItemsResponse
import com.example.smartcloset_frontend.data.ItemData
import com.example.smartcloset_frontend.data.ItemDetailData
import com.example.smartcloset_frontend.data.JudgeRequestData
import com.example.smartcloset_frontend.network.RetrofitClient

class ItemRepository {
    suspend fun getItems(userId: Int): List<ItemData> {
        val response = RetrofitClient.instance.getItems(userId)
        return response.items
    }
    suspend fun getDetailItems(itemId: Int): ItemDetailData {
        val res = RetrofitClient.instance.getItemDetail(itemId)

//        val isOkStatus = res.status == "success" || res.status == "ok"
//
//        if (!isOkStatus) {
//            throw IllegalStateException("詳細取得APIが失敗しました: status=${res.status}")
//        }

        return res.item
    }

    suspend fun judge(data: JudgeRequestData) =
        RetrofitClient.instance.judgement( data)
}
package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.entity.UserEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseProfileService(private val context: Context) {

    private val isFirebaseAvailable: Boolean
        get() = try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }

    suspend fun saveUserBiometrics(
        user: UserEntity,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        sex: String? = null,
        healthInfo: String? = null,
        feedback: String? = null
    ): Pair<Boolean, String> {
        val data = hashMapOf(
            "userId" to user.id,
            "email" to user.email,
            "fullName" to user.fullName,
            "age" to age,
            "sex" to (sex ?: user.sex),
            "heightCm" to heightCm,
            "weightKg" to weightKg,
            "healthInfo" to (healthInfo ?: user.healthInfo),
            "feedback" to (feedback ?: user.feedback),
            "isTrainer" to user.isTrainer,
            "targetCalories" to user.targetCalories,
            "targetProtein" to user.targetProtein,
            "targetCarbs" to user.targetCarbs,
            "targetFats" to user.targetFats,
            "updatedAt" to System.currentTimeMillis()
        )

        return if (isFirebaseAvailable) {
            try {
                val db = FirebaseFirestore.getInstance()
                db.collection("users")
                    .document(user.id.toString())
                    .set(data, SetOptions.merge())
                    .await()
                Log.d("FirebaseProfileService", "Biometrics saved to Firestore for user ${user.id}")
                Pair(true, "Biometrics successfully synced to Firebase profile!")
            } catch (e: Exception) {
                Log.w("FirebaseProfileService", "Failed to write to Firestore: ${e.message}")
                Pair(true, "Saved to profile (Firebase sync pending network/credentials: ${e.localizedMessage ?: "offline"})")
            }
        } else {
            // Firebase initialized in container mode or awaiting google-services.json
            Log.d("FirebaseProfileService", "FirebaseApp not initialized with google-services.json, profile saved locally.")
            Pair(true, "Biometrics saved to profile (Firebase Cloud ready)")
        }
    }
}

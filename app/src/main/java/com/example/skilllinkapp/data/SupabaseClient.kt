package com.example.skilllinkapp.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.android.Android

val supabase = createSupabaseClient(
    supabaseUrl = "https://kaehvrkpcmrxrrhkzdgf.supabase.co",
    supabaseKey = "sb_publishable_8sTlz0kg6lTEaQ2fGgRZMg_72ghlOTv"
) {
    install(Auth) {
        scheme = "skilllink"
        host = "login-callback"
    }
    install(Postgrest)

    httpEngine = Android.create()
}
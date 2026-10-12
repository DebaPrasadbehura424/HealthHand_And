package com.example.healthhand.database

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeCallbackId
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client= createSupabaseClient(
        supabaseUrl = "https://imqfazsnixzgioxmrgsh.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImltcWZhenNuaXh6Z2lveG1yZ3NoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA5MTUxNjksImV4cCI6MjEwNjQ5MTE2OX0.pfp_aGYWZ_JsrQ_inAVxDO_PpYA27ngbrJD7apr6iDw"
    ){
        install(Postgrest)
        install(Auth)
        install(Storage)
        install(Realtime)

    }

}

//val instruments = SupabaseClient.client.from("instruments").select().decodeList<Instrument>()
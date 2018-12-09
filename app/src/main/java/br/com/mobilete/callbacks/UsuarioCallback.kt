package br.com.mobilete.callbacks

import com.google.firebase.auth.FirebaseUser

interface UsuarioCallback {
    fun onCallbackUsuarioDao()
    fun onCallbackUploadFoto(fotoUri: String)
    fun onError(men: String)
}
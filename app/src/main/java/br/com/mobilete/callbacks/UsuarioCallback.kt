package br.com.mobilete.callbacks

interface UsuarioCallback {
    fun onCallbackUsuarioDao()
    fun onCallbackUploadFoto(fotoUri: String)
    fun onError(men: String)
}
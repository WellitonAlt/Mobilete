package br.com.mobilete.callbacks

interface AnuncioCallback {
    fun onCallbackAnuncioDao()
    fun onCallbackUploadFoto(fotoUri: String)
    fun onError(men: String)
}
package br.com.mobilete.callbacks

import br.com.mobilete.entities.Anuncio

interface AnuncioCallback {
    fun onCallbackAnuncioDao()
    fun onCallbackAnuncios(anuncios: MutableList<Anuncio>)
    fun onCallbackUploadFoto(fotoUri: String)
    fun onError(men: String)

}
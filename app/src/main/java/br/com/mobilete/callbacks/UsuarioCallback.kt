package br.com.mobilete.callbacks

import br.com.mobilete.entities.Usuario

interface UsuarioCallback {
    fun onCallbackUsuarioDao()
    fun onCallbackgetUsuario(usuario: Usuario)
    fun onCallbackUploadFoto(fotoUri: String)
    fun onError(men: String)
}
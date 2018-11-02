package br.com.mobilete

import android.content.Context

class Preferencias(context: Context) {

    companion object {
        const val PREFERENCIAS: String = "Preferencias Usuario"
        const val PRE_NOME: String = "Nome"
        const val PRE_EMAIL: String = "Email"
        const val PRE_TELEFONE: String = "Telefone"
        const val PRE_FOTO: String = "Foto"
        const val PRE_PROVIDER: String = "Provider"
    }

    val preferencias = context.getSharedPreferences(PREFERENCIAS, 0) //MODE_PRIVATE

    fun getNome() : String?{
        return preferencias.getString(PRE_NOME, "Nome do Usuario")
    }


    fun setNome(nome: String) {
        val editor = preferencias.edit()
        editor.putString(PRE_NOME, nome)
        editor.apply()
    }

    fun getEmail() : String?{
        return preferencias.getString(PRE_EMAIL, "email@dominio.com")
    }

    fun setEmail(email: String) {
        val editor = preferencias.edit()
        editor.putString(PRE_EMAIL, email)
        editor.apply()
    }

    fun getTelefone() : String?{
        return preferencias.getString(PRE_TELEFONE, "999999999")
    }

    fun setTelefone(telefone: String) {
        val editor = preferencias.edit()
        editor.putString(PRE_TELEFONE, telefone)
        editor.apply()
    }

    fun getFoto() : String?{
        return preferencias.getString(PRE_FOTO, "Foto")
    }

    fun setFoto(foto: String) {
        val editor = preferencias.edit()
        editor.putString(PRE_FOTO, foto)
        editor.apply()
    }

    fun getProvider() : String?{
        return preferencias.getString(PRE_PROVIDER, "Firebase")
    }

    fun setProvider(provider: String) {
        val editor = preferencias.edit()
        editor.putString(PRE_PROVIDER, provider)
        editor.apply()
    }

    fun setUsuario(usuario: Usuario){
        setNome(usuario.nome)
        setEmail(usuario.email)
        setTelefone(usuario.telefone)
        setFoto(usuario.foto)
    }
}
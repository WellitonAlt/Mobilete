package br.com.mobilete.utils

import android.content.Context
import br.com.mobilete.entities.Usuario

class Preferencias(context: Context) {

    companion object {
        private const val PREFERENCIAS: String = "Preferencias Usuario"
        private const val PRE_NOME: String = "Nome"
        private const val PRE_EMAIL: String = "Email"
        private const val PRE_TELEFONE: String = "Telefone"
        private const val PRE_FOTO: String = "Foto"
        private const val PRE_PROVIDER: String = "Provider"
    }

    private val preferencias = context.getSharedPreferences(PREFERENCIAS, 0)

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
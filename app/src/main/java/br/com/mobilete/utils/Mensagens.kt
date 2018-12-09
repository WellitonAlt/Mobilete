package br.com.mobilete.utils

import android.content.Context
import android.widget.EditText
import android.widget.Toast

object Mensagens {

    fun mensagem(context: Context, mensagem: String){
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }

    fun mensagemFocus(context: Context, mensagem: String, editText: EditText){
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
        editText.error = mensagem
        editText.requestFocus()
    }
}
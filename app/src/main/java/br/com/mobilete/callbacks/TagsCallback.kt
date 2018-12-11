package br.com.mobilete.callbacks

import br.com.mobilete.entities.Tag

interface TagsCallback {
    fun onCallbackTagsDao()
    fun onCallbackTags(tags: MutableList<Tag>)
    fun onError(men: String)
}
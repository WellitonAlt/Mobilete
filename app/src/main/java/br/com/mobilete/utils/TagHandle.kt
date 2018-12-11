package br.com.mobilete.utils

object TagHandle {

    fun tagAdd(tags: String, item: String) : String {
        if (tags == "") {
            return item
        }
        return "$tags,$item"
    }

    fun tagRemove(tags: String, item: String) : String {
        val aux: List<String> = tags.split(",")
        var aux2 = ""
        for (i in aux) {
            if (i != item) {
                aux2 = tagAdd(aux2, i)
            }
        }
        return aux2
    }

    fun tagSort(tags: String) : String{
        val aux: List<String> = tags.split(",")
        var aux2 = ""
        for (i in  aux.sortedDescending()) {
            aux2 = tagAdd(aux2, i)
        }
        return aux2
    }
}
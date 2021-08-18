package com.sundbean.raise.models

data class Group(var id: String? = null, var name: String? = null, var photoUrl: String? = null, var memberNum: Int? = null) {

    fun setuid (id : String?) {
        this.id = id
    }
}

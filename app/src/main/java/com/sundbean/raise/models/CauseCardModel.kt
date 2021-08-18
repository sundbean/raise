package com.sundbean.raise.models

data class Cause(var id: String? = null, var name: String? = null, var photoUrl: String? = null) {

    fun setUid (id : String?) {
        this.id = id
    }
}
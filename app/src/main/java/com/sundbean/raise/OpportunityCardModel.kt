package com.sundbean.raise

import com.google.firebase.firestore.DocumentReference

data class Opportunity(var uid: String? = null, var type: String? = null, var date: String? = null, var time: String? = null, var name: String? = null, var photoUrl: String? = null) {}
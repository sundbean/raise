package com.sundbean.raise

import com.google.firebase.firestore.DocumentReference

data class Opportunity(var uid: String? = null, var oppType: String? = null, var date: String? = null, var startTime: Map<String, Int?>? = null, var endTime: Map<String, Int?>? = null, var name: String? = null, var photoUrl: String? = null, var rsvpNum: Int? = null) {
}

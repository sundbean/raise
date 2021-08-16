package com.sundbean.raise

import android.content.Context
import android.content.res.Resources

fun Context.resIdByName(resIdName: String?, resType: String): Int {
    resIdName?.let {
        return resources.getIdentifier(it, resType, packageName)
    }
    throw Resources.NotFoundException()
}

fun convertStateNameToAbbreviation(stateName : String?): String? {
    val states: MutableMap<String, String> = HashMap()
    states["Alabama"] = "AL"
    states["Alaska"] = "AK"
    states["Alberta"] = "AB"
    states["American Samoa"] = "AS"
    states["Arizona"] = "AZ"
    states["Arkansas"] = "AR"
    states["Armed Forces (AE)"] = "AE"
    states["Armed Forces Americas"] = "AA"
    states["Armed Forces Pacific"] = "AP"
    states["British Columbia"] = "BC"
    states["California"] = "CA"
    states["Colorado"] = "CO"
    states["Connecticut"] = "CT"
    states["Delaware"] = "DE"
    states["District Of Columbia"] = "DC"
    states["Florida"] = "FL"
    states["Georgia"] = "GA"
    states["Guam"] = "GU"
    states["Hawaii"] = "HI"
    states["Idaho"] = "ID"
    states["Illinois"] = "IL"
    states["Indiana"] = "IN"
    states["Iowa"] = "IA"
    states["Kansas"] = "KS"
    states["Kentucky"] = "KY"
    states["Louisiana"] = "LA"
    states["Maine"] = "ME"
    states["Manitoba"] = "MB"
    states["Maryland"] = "MD"
    states["Massachusetts"] = "MA"
    states["Michigan"] = "MI"
    states["Minnesota"] = "MN"
    states["Mississippi"] = "MS"
    states["Missouri"] = "MO"
    states["Montana"] = "MT"
    states["Nebraska"] = "NE"
    states["Nevada"] = "NV"
    states["New Brunswick"] = "NB"
    states["New Hampshire"] = "NH"
    states["New Jersey"] = "NJ"
    states["New Mexico"] = "NM"
    states["New York"] = "NY"
    states["Newfoundland"] = "NF"
    states["North Carolina"] = "NC"
    states["North Dakota"] = "ND"
    states["Northwest Territories"] = "NT"
    states["Nova Scotia"] = "NS"
    states["Nunavut"] = "NU"
    states["Ohio"] = "OH"
    states["Oklahoma"] = "OK"
    states["Ontario"] = "ON"
    states["Oregon"] = "OR"
    states["Pennsylvania"] = "PA"
    states["Prince Edward Island"] = "PE"
    states["Puerto Rico"] = "PR"
    states["Quebec"] = "QC"
    states["Rhode Island"] = "RI"
    states["Saskatchewan"] = "SK"
    states["South Carolina"] = "SC"
    states["South Dakota"] = "SD"
    states["Tennessee"] = "TN"
    states["Texas"] = "TX"
    states["Utah"] = "UT"
    states["Vermont"] = "VT"
    states["Virgin Islands"] = "VI"
    states["Virginia"] = "VA"
    states["Washington"] = "WA"
    states["West Virginia"] = "WV"
    states["Wisconsin"] = "WI"
    states["Wyoming"] = "WY"
    states["Yukon Territory"] = "YT"

    return states[stateName]
}
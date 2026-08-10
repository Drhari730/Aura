package com.mhealth.aura.data.location

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class IndiaLocationRepository(context: Context) {
    private val districtsByState: Map<String, List<String>>
    private val citiesByState: Map<String, List<String>>

    init {
        val districtRoot = JSONObject(
            context.assets.open("india_states_districts.json").bufferedReader().use { it.readText() }
        )
        val districtMap = linkedMapOf<String, List<String>>()
        val states = districtRoot.getJSONArray("states")
        for (index in 0 until states.length()) {
            val item = states.getJSONObject(index)
            val state = normalizeState(item.getString("state"))
            districtMap[state] = item.getJSONArray("districts").toStringList()
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .sorted()
        }

        val cities = JSONArray(
            context.assets.open("india_cities.json").bufferedReader().use { it.readText() }
        )
        val cityMap = linkedMapOf<String, MutableSet<String>>()
        for (index in 0 until cities.length()) {
            val item = cities.getJSONObject(index)
            val state = normalizeState(item.getString("state"))
            cityMap.getOrPut(state) { linkedSetOf() }.add(item.getString("name").trim())
        }

        districtsByState = districtMap.toSortedMap()
        citiesByState = cityMap.mapValues { (_, values) -> values.filter(String::isNotBlank).sorted() }
    }

    fun states(): List<String> = (districtsByState.keys + citiesByState.keys).distinct().sorted()

    fun districts(state: String): List<String> = districtsByState[normalizeState(state)].orEmpty()

    fun cities(state: String, district: String = ""): List<String> {
        val cities = citiesByState[normalizeState(state)].orEmpty()
        if (district.isBlank()) return cities
        val districtCity = cities.firstOrNull { it.equals(district, ignoreCase = true) }
        return if (districtCity == null) cities else listOf(districtCity) + cities.filterNot {
            it.equals(districtCity, ignoreCase = true)
        }
    }

    private fun normalizeState(value: String): String = when (value.trim()) {
        "Andaman and Nicobar Islands" -> "Andaman & Nicobar Islands"
        "Dadra and Nagar Haveli (UT)", "Daman and Diu (UT)" ->
            "Dadra & Nagar Haveli and Daman & Diu"
        "Delhi", "Delhi (NCT)" -> "Delhi"
        "Jammu and Kashmir" -> "Jammu & Kashmir"
        "Orissa" -> "Odisha"
        "Pondicherry", "Puducherry (UT)" -> "Puducherry"
        "Uttaranchal" -> "Uttarakhand"
        else -> value
            .replace(" (UT)", "")
            .trim()
    }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).map { getString(it) }
}

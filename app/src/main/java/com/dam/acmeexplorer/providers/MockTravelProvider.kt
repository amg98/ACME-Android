package com.dam.acmeexplorer.providers

import com.dam.acmeexplorer.models.Travel
import java.util.*

private val brokenTravel = Travel("0", "???", listOf("https://d1nhio0ox7pgb.cloudfront.net/_img/g_collection_png/standard/512x512/link_broken.png"), Calendar.getInstance().time, Calendar.getInstance().time, 0, "???")
private val allTravels = listOf(
    Travel("1", "Madrid", listOf("https://picsum.photos/id/237/200/300"), GregorianCalendar(2021, 4, 20).time, GregorianCalendar(2021, 5, 26).time, 1840, "Palacio Real"),
    Travel("2", "París", listOf("https://picsum.photos/id/238/200/300"), GregorianCalendar(2021, 5, 5).time, GregorianCalendar(2021, 5, 10).time, 2000, "Disneyland"),
    Travel("3", "Londres", listOf("https://picsum.photos/id/239/200/300"), GregorianCalendar(2021, 6, 10).time, GregorianCalendar(2021, 6, 15).time, 1720, "Big Ben"),
    Travel("4", "Pekín", listOf("https://picsum.photos/id/240/200/300"), GregorianCalendar(2021, 7, 15).time, GregorianCalendar(2021, 7, 20).time, 1820, "Muralla china"),
    Travel("5", "New York", listOf("https://picsum.photos/id/241/200/300"), GregorianCalendar(2021, 8, 20).time, GregorianCalendar(2021, 8, 25).time, 1990, "Torre de la libertad"),
    Travel("6", "Tokio", listOf("https://picsum.photos/id/242/200/300"), GregorianCalendar(2021, 9, 25).time, GregorianCalendar(2021, 9, 30).time, 2500, "Monte Fuji"),
    Travel("7", "Berlín", listOf("https://picsum.photos/id/243/200/300"), GregorianCalendar(2021, 10, 30).time, GregorianCalendar(2021, 11, 15).time, 3100, "Aeropuerto"),
    Travel("8", "Helsinki", listOf("https://picsum.photos/id/244/200/300"), GregorianCalendar(2021, 11, 1).time, GregorianCalendar(2021, 11, 10).time, 2345, "Frontera"),
    Travel("9", "Denver", listOf("https://picsum.photos/id/200/200/300"), GregorianCalendar(2021, 12, 5).time, GregorianCalendar(2021, 12, 14).time, 2015, "Playa"),
    Travel("10", "Lisboa", listOf("https://picsum.photos/id/201/200/300"), GregorianCalendar(2021, 1, 10).time, GregorianCalendar(2022, 1, 23).time, 1530, "Faro"),
)

class MockTravelProvider : TravelProvider {

    override suspend fun getTravels(): List<Travel>? = allTravels

    override suspend fun getTravels(startDate: Date, endDate: Date, minPrice: Int, maxPrice: Int): List<Travel>? {
        return allTravels.filter { it.startDate > startDate && it.endDate < endDate && it.price >= minPrice && it.price <= maxPrice }
    }

    override suspend fun getTravels(travels: Set<String>): List<Travel>? {
        return travels.map { travelID -> allTravels.find { it.id == travelID } ?: brokenTravel }
    }
}

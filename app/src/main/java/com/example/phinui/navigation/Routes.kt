package com.example.phinui.ui.navigation

object Routes {
    // where the system should take the user to
    const val HOME = "home"
    const val MESSAGES = "messages"
    const val PROFILE = "profile"
    const val EVENTS = "events"
    const val CALENDAR = "calendar"
    const val ADD_EVENT = "add_event"

    const val MAP = "map"

    const val VENDING_STOCK = "vending_stock"
    const val MAP_WITH_PIN =
        "map?pinId={pinId}&pinName={pinName}&pinCategory={pinCategory}&pinLatitude={pinLatitude}&pinLongitude={pinLongitude}&pinBuilding={pinBuilding}&pinDescription={pinDescription}"
    const val SCHEDULE = "schedule"
    const val REGISTER = "register"
    const val LOGIN = "login"
    const val USERLIST = "userList"
    const val FRIENDS = "friends"
    const val PEOPLE = "people"
    const val SETTINGS = "settings"
    fun mapRouteWithPin(
        pinId: String,
        pinName: String,
        pinCategory: String,
        pinLatitude: Double,
        pinLongitude: Double,
        pinBuilding: String,
        pinDescription: String
    ): String {
        return "map?" +
                "pinId=${java.net.URLEncoder.encode(pinId, "UTF-8")}" +
                "&pinName=${java.net.URLEncoder.encode(pinName, "UTF-8")}" +
                "&pinCategory=${java.net.URLEncoder.encode(pinCategory, "UTF-8")}" +
                "&pinLatitude=$pinLatitude" +
                "&pinLongitude=$pinLongitude" +
                "&pinBuilding=${java.net.URLEncoder.encode(pinBuilding, "UTF-8")}" +
                "&pinDescription=${java.net.URLEncoder.encode(pinDescription, "UTF-8")}"
    }
}
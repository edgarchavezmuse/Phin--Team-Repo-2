package com.example.phinui.data.events

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "rss", strict = false)
data class RssFeed @JvmOverloads constructor(
    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
data class Channel @JvmOverloads constructor(
    @field:ElementList(name = "item", inline = true)
    var items: List<EventItem>? = null
)

@Root(name = "item", strict = false)
data class EventItem @JvmOverloads constructor(
    @field:Element(name = "title")
    var title: String = "",

    @field:Element(name = "link")
    var link: String = "",

    @field:Element(name = "pubDate")
    var pubDate: String = "",

    @field:Element(name = "description", required = false)
    var description: String = ""
)
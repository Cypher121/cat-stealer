package coffee.cypher.catstealer.extensions

import com.kotlindiscord.kord.extensions.checks.anyGuild
import com.kotlindiscord.kord.extensions.checks.inChannel
import com.kotlindiscord.kord.extensions.checks.isNotBot
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.env
import com.kotlindiscord.kord.extensions.utils.respond
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.createEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.Image
import io.ktor.client.*
import kotlinx.coroutines.flow.toList

private val STEALER_CHANNEL = Snowflake(env("STEALER_CHANNEL"))
private val EMOTE_REGEX = Regex("<(?<anim>a)?:(?<name>\\w+):(?<id>\\d{18})>")

class CatStealerExtension : Extension() {
    override val name = "cat-stealer"

    private val client = HttpClient()

    override suspend fun setup() {
        event<MessageCreateEvent> {
            check {
                isNotBot()
                anyGuild()
                inChannel(STEALER_CHANNEL)
            }

            action {
                val guild = event.getGuildOrNull()!!
                val guildEmojis = guild.emojis.toList()

                val response = parseEmotes(event.message.content).filter { emote ->
                    guildEmojis.none { it.name == emote.name }
                }.map { emote ->
                    guild.createEmoji(emote.name, emote.getImage(client)) {}
                }.joinToString(separator = "") {
                    "<${if (it.isAnimated) "a" else ""}:${it.name}:${it.id}>"
                }

                event.message.respond(response)
            }
        }
    }

    private fun parseEmotes(text: String): List<Emote> {
        return EMOTE_REGEX.findAll(text).map {
            Emote(
                Snowflake(it.groups["id"]!!.value),
                it.groups["name"]!!.value,
                it.groups["anim"] != null
            )
        }.toList()
    }

    data class Emote(
        val id: Snowflake,
        val name: String,
        val isAnimated: Boolean
    ) {
        private val url = let {
            val ext = if (isAnimated)
                "gif"
            else
                "webp"

            "https://cdn.discordapp.com/emojis/$id.$ext"
        }

        suspend fun getImage(client: HttpClient): Image {
            val image = Image.fromUrl(client, url)

            if (image.data.size <= 262_144) {
                return image
            }

            return Image.fromUrl(client, "$url?size=96&quality=lossless")
        }
    }
}
